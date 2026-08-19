package fi.aalto.cs.apluscourses.model.component

import com.intellij.platform.util.progress.reportSequentialProgress
import com.intellij.openapi.application.edtWriteAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.OrderRootType
import com.intellij.openapi.roots.impl.libraries.LibraryEx.ModifiableModelEx
import com.intellij.openapi.roots.libraries.PersistentLibraryKind
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.util.application
import fi.aalto.cs.apluscourses.MyBundle
import fi.aalto.cs.apluscourses.services.course.CourseSetupStatus
import fi.aalto.cs.apluscourses.services.course.SetupStepState
import fi.aalto.cs.apluscourses.utils.CoursesLogger
import org.jetbrains.annotations.NonNls
import org.jetbrains.plugins.scala.project.ScalaLanguageLevel
import org.jetbrains.plugins.scala.project.ScalaLibraryProperties
import org.jetbrains.plugins.scala.project.ScalaLibraryPropertiesState
import org.jetbrains.plugins.scala.project.ScalaLibraryType
import java.io.File
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Path
import java.util.jar.JarInputStream
import java.util.jar.Manifest
import kotlin.io.path.isDirectory
import fi.aalto.cs.apluscourses.utils.formatFileSize

class ScalaSdk(private val scalaVersion: String, project: Project) : Library(scalaVersion, project) {
    @NonNls
    private val versionNumber = scalaVersion.substringAfter("scala-sdk-")
    private val libPath = Path.of(project.basePath!!, ".libs")
    private val sdkPath = libPath.resolve("scala3-$versionNumber")

    /**
     * Each download runs inside its own reporter step. A step of `reportSequentialProgress` can
     * be taken only once. A download with no step of its own reports into a reporter that is
     * already complete.
     */
    override suspend fun downloadAndInstall(updating: Boolean, trackSetup: Boolean): Unit =
        reportSequentialProgress { reporter ->
            status = Status.LOADING
            CoursesLogger.info("Downloading Scala SDK $scalaVersion")
            val zipUrl =
                "https://github.com/lampepfl/dotty/releases/download/$versionNumber/scala3-$versionNumber.zip"
            val path = libPath
            val setup = CourseSetupStatus.getInstance(project).takeIf { trackSetup }
            val step = CourseSetupStatus.moduleStep(scalaVersion)
            setup?.report(
                step,
                MyBundle.message("ui.setup.scalaSdk", versionNumber),
                SetupStepState.RUNNING
            )
            reporter.nextStep(SDK_ZIP_DONE) {
                // Only the distribution zip reports bytes. It is much larger than the three jars.
                downloadAndUnzipZip(zipUrl, path) { copied, total ->
                    setup?.update(
                        step,
                        SetupStepState.RUNNING,
                        MyBundle.message(
                            "ui.setup.ofTotal",
                            formatFileSize(copied),
                            formatFileSize(total)
                        ),
                        detailIsMeasurement = true
                    )
                }
            }

            val libraryTable = libraryTable(project).modifiableModel
            libraryTable.getLibraryByName(scalaVersion)?.let {
                application.runWriteAction {
                    libraryTable.removeLibrary(it)
                }
            }
            val kind: PersistentLibraryKind<ScalaLibraryProperties> = ScalaLibraryType.`Kind$`.`MODULE$`
            val library = libraryTable.createLibrary(name, kind)

            val libDir = sdkPath.resolve("lib")
            val m2Dir = sdkPath.resolve("maven2")
            val scala3Ver = versionNumber

            val scala38Plus = isScala38Plus(scala3Ver)

            val fileTreeWalk = sdkPath.toFile().walkTopDown()

            val jars =
                if (scala38Plus)
                // read the classpath from the JARs' manifests
                    sequenceOf(SCALA_JAR, WITH_COMPILER_JAR)
                        .map(libDir::resolve)
                        .flatMap(::getClassPathFromJar)
                        .map(FileUtil::toSystemDependentName)
                        .map(libDir::resolve)
                        .map(Path::normalize)
                        .map(Path::toString)
                else
                // just take (almost) everything
                    fileTreeWalk.filter { it.extension == "jar" && it.nameWithoutExtension != SCALA_CLI }
                        .map(File::toString)
                        .map(FileUtil::toSystemDependentName)

            val fileUrlProtocol = LocalFileSystem.getInstance().protocol

            val compilerClasspath = jars
                .map { VirtualFileManager.constructUrl(fileUrlProtocol, it) }
                .toList()
                .toTypedArray()

            val replClasspath = if (scala38Plus) compilerClasspath else emptyArray()

            edtWriteAction {
                val libraryModel = library.modifiableModel

                // HACK: this is the only way to access properties that I am aware of
                val libraryEx = libraryModel as ModifiableModelEx
                val properties = libraryEx.properties
                val newState = ScalaLibraryPropertiesState(
                    ScalaLanguageLevel.findByVersion(versionNumber).get(),
                    compilerClasspath,
                    emptyArray<String>(),
                    null,
                    replClasspath
                )
                properties.loadState(newState)
                libraryEx.properties = properties

                libraryModel.commit()
                val newLibraryModel = library.modifiableModel

                sequenceOf(SCALA3_LIB_PREFIX, SCALA2_LIB_PREFIX)
                    .map { root -> fileTreeWalk.find { it.name.startsWith(root) && it.extension == "jar" }!! }
                    .map(VfsUtil::getUrlForLibraryRoot)
                    .forEach { newLibraryModel.addRoot(it, OrderRootType.CLASSES) }

                newLibraryModel.commit()
                libraryTable.commit()
                VirtualFileManager.getInstance().syncRefresh()
            }

            @NonNls
            val notFound = "scala-library 2.x jar not found under $libDir or $m2Dir for Scala $scala3Ver"
            val stdlibVer: String = if (scala38Plus) {
                scala3Ver
            } else {
                findStdlibUnder(libDir)
                    ?: findStdlibUnder(m2Dir)
                    ?: error(notFound)
            }

            val scala3LibSources = libDir.resolve("scala3-library_3-$scala3Ver-sources.jar")
            reporter.nextStep(SCALA3_SOURCES_DONE) {
                downloadFile(
                    "https://repo1.maven.org/maven2/org/scala-lang/scala3-library_3/$scala3Ver/scala3-library_3-$scala3Ver-sources.jar",
                    scala3LibSources
                )
            }

            val scalaStdlibSources = libDir.resolve("scala-library-$stdlibVer-sources.jar")
            reporter.nextStep(STDLIB_SOURCES_DONE) {
                downloadFile(
                    "https://repo1.maven.org/maven2/org/scala-lang/scala-library/$stdlibVer/scala-library-$stdlibVer-sources.jar",
                    scalaStdlibSources
                )
            }


            // Attach sources jars to the library
            edtWriteAction {
                val libraryModel = library.modifiableModel
                fun addSourcesJar(path: Path) {
                    if (Files.exists(path)) {
                        libraryModel.addRoot(VfsUtil.getUrlForLibraryRoot(path.toFile()), OrderRootType.SOURCES)
                    }
                }
                addSourcesJar(scala3LibSources)
                addSourcesJar(scalaStdlibSources)
                libraryModel.commit()
                VirtualFileManager.getInstance().syncRefresh()
            }

            setup?.update(step, SetupStepState.DONE)
            status = Status.LOADED
        }

    private fun getClassPathFromJar(path: Path): List<String> = getClassPathFromJar(path.toFile())

    private fun getClassPathFromJar(file: File) = file.inputStream().use(::getClassPathFromJar)

    private fun getClassPathFromJar(inputStream: InputStream) = JarInputStream(inputStream).use(::getClassPathFromJar)

    private fun getClassPathFromJar(jarInputStream: JarInputStream) = getClassPathFromJar(jarInputStream.manifest)

    private fun getClassPathFromJar(manifest: Manifest) =
        manifest.mainAttributes.getValue(CLASS_PATH_ATTRIBUTE)
            ?.split(' ')
            ?: emptyList()

    companion object {
        /**
         * Splits a Scala version into its numeric (major, minor, patch) parts, ignoring any
         * qualifier such as `-RC1`. Missing parts are treated as 0.
         */
        internal fun parseVersion(version: String): Triple<Int, Int, Int> =
            version.split('.', '-', '_').mapNotNull { it.toIntOrNull() }.let {
                Triple(it.getOrElse(0) { 0 }, it.getOrElse(1) { 0 }, it.getOrElse(2) { 0 })
            }

        /**
         * Scala 3.8 moved the REPL into a separate artifact and dropped the separate 2.x standard
         * library, so the install differs from 3.8.0 onwards. Compared numerically: a plain string
         * comparison would place "3.10" before "3.8".
         */
        internal fun isScala38Plus(version: String): Boolean {
            val (major, minor, _) = parseVersion(version)
            return major > 3 || (major == 3 && minor >= 8)
        }

        /**
         * How the SDK install bar is divided. The distribution zip is by far the largest of the
         * three downloads.
         */
        private const val SDK_ZIP_DONE = 70
        private const val SCALA3_SOURCES_DONE = 90
        private const val STDLIB_SOURCES_DONE = 100

        /** Maven classifier suffixes that follow the version in a jar file name. */
        @NonNls
        private val CLASSIFIERS = listOf("-sources", "-javadoc")

        @NonNls
        private const val STDLIB_PREFIX = "scala-library-"

        @NonNls
        private const val SCALA3_LIB_PREFIX = "scala3-library"

        @NonNls
        private const val SCALA2_LIB_PREFIX = "scala-library"

        @NonNls
        private const val SCALA_CLI = "scala-cli"

        /** The jars whose manifests carry the full compiler classpath from Scala 3.8 onwards. */
        @NonNls
        private const val SCALA_JAR = "scala.jar"

        @NonNls
        private const val WITH_COMPILER_JAR = "with_compiler.jar"

        @NonNls
        private const val CLASS_PATH_ATTRIBUTE = "Class-Path"


        /**
         * Finds the version of the Scala 2.x standard library jar shipped under [root], if any.
         *
         * Classifier jars are skipped. The same directory also holds the `-sources` jar of that
         * library. A version read from it is "2.13.16-sources", which builds a broken Maven URL.
         */
        internal fun findStdlibUnder(root: Path): String? {
            if (!Files.exists(root) || !root.isDirectory()) return null
            return Files.walk(root).use { stream ->
                stream.filter { Files.isRegularFile(it) }
                    .map { it.fileName.toString() }
                    .filter { it.startsWith(STDLIB_PREFIX) && it.endsWith(".jar") }
                    .map { it.removeSuffix(".jar").substringAfter(STDLIB_PREFIX) }
                    .filter { version ->
                        version.startsWith("2.") && CLASSIFIERS.none { version.endsWith(it) }
                    }
                    .findFirst()
                    .orElse(null)
            }
        }
    }
}
