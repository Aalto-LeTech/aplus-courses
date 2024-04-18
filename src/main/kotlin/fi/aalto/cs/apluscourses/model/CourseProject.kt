package fi.aalto.cs.apluscourses.model

import com.intellij.openapi.project.Project
import fi.aalto.cs.apluscourses.dal.PasswordStorage
import fi.aalto.cs.apluscourses.dal.TokenAuthentication
import fi.aalto.cs.apluscourses.dal.TokenStorage
import fi.aalto.cs.apluscourses.intellij.notifications.NetworkErrorNotification
import fi.aalto.cs.apluscourses.intellij.notifications.Notifier
import fi.aalto.cs.apluscourses.model.exercise.ExercisesTree
import fi.aalto.cs.apluscourses.services.exercise.ExercisesUpdaterService
import fi.aalto.cs.apluscourses.utils.Event
import fi.aalto.cs.apluscourses.utils.observable.ObservableProperty
import fi.aalto.cs.apluscourses.utils.observable.ObservableReadWriteProperty
import java.io.IOException
import java.net.URL
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.concurrent.Volatile

/**
 * A `CourseProject` instance contains a [Course] and [Project]. In addition, it
 * contains a [CourseUpdater] that regularly updates the course, and an [Event] that is
 * triggered when an update occurs.
 */
class CourseProject : ExercisesLazyLoader {
    private val notifier: Notifier

    @JvmField
    val course: Course

    @Volatile
    private var exercisesTree: ExercisesTree? = null

    @JvmField
    @Volatile
    var newsTree: NewsTree? = null

    private val hasTriedToReadAuthenticationFromStorage = AtomicBoolean(false)

    @JvmField
    val courseUpdater: ExercisesUpdaterService

    @JvmField
    val exercisesUpdater: ExercisesUpdaterService

    @JvmField
    val project: Project

    @JvmField
    val courseUpdated: Event = Event()

    @JvmField
    val exercisesUpdated: Event = Event()

    @JvmField
    val user: ObservableProperty<User?> = ObservableReadWriteProperty(null)

    @Volatile
    var selectedStudent: Student? = null
        set(selectedStudent) {
            field = selectedStudent
            lazyLoadedGroups.clear()
        }

    private val lazyLoadedGroups: MutableSet<Long> = Collections.synchronizedSet(HashSet())

    /**
     * Construct a course project from the given course, course configuration URL (used for updating),
     * and project.
     */
    constructor(
        course: Course,
        courseUrl: URL,
        project: Project,
        notifier: Notifier
    ) {
        this.notifier = notifier
        this.course = course
        this.project = project
        this.courseUpdater =
            ExercisesUpdaterService.getInstance(project)//CourseUpdater(this, course, project, courseUrl, courseUpdated)
        this.exercisesUpdater = ExercisesUpdaterService.getInstance(project)
    }

    /**
     * Construct a course project from the given course, exercises and course updaters,
     * and project.
     */
    constructor(
        course: Course,
        courseUpdater: ExercisesUpdaterService,
        exercisesUpdater: ExercisesUpdaterService,
        project: Project,
        notifier: Notifier
    ) {
        this.notifier = notifier
        this.course = course
        this.project = project
        this.courseUpdater = courseUpdater
        this.exercisesUpdater = exercisesUpdater
    }

    /**
     * Disposes the course project, which includes stopping background updaters and clearing the
     * authentication. This should be called when a course project is no longer used.
     */
    fun dispose() {
        val myUser = user.get()
        myUser?.authentication?.clear()
        courseUpdater.stop()
        exercisesUpdater.stop()
    }

    /**
     *
     * Sets authentication to the one that is read from the password storage and constructed with
     * the given factory.
     *
     *
     * This method does anything only when it's called the first time for an instance. All the
     * subsequent calls do nothing.
     *
     * @param passwordStorage Password storage.
     * @param factory         Authentication factory.
     */
    fun readAuthenticationFromStorage(
        passwordStorage: TokenStorage?,
        factory: TokenAuthentication.Factory
    ) {
        if ((hasTriedToReadAuthenticationFromStorage.getAndSet(true)) || (user.get() != null)) {
            return
        }
//        Optional.ofNullable(passwordStorage)
//            .map { obj: TokenStorage? -> obj!!.getToken() }
//            .map { token: CharArray? -> factory.create(token) }
//            .ifPresent { authentication: TokenAuthentication? -> this.authentication = authentication }
    }

    /**
     * Removes user from password storage.
     */
    fun removePasswordFromStorage(passwordStorageFactory: PasswordStorage.Factory) {
        val passwordStorage = passwordStorageFactory.create(course.apiUrl)
        passwordStorage?.remove()
    }

    val userName: String
        get() = Optional.ofNullable(user.get()).map { obj: User -> obj.userName }
            .orElse("")

    val userStudentId: String
        get() = Optional.ofNullable(user.get()).map { obj: User -> obj.studentId }
            .orElse("")

    val userAPlusId: Int
        get() = Optional.ofNullable(user.get()).map { obj: User -> obj.id }
            .orElse(0)

    fun getUser(): User? {
        return user.get()
    }

    var exerciseTree: ExercisesTree?
        get() = exercisesTree
        set(exercisesTree) {
            this.exercisesTree = exercisesTree
        }

    var authentication: Authentication?
        get() = user.get()?.authentication
        /**
         * Sets the authentication. Any existing authentication is cleared.
         */
        set(authentication) {
            try {
                val newUser = if (authentication == null
                ) null else course.exerciseDataSource.getUser(authentication)
                val oldUser = user.getAndSet(newUser)
                oldUser?.authentication?.clear()
            } catch (e: IOException) {
                notifier.notify(NetworkErrorNotification(e), project)
            }
        }

    override fun setLazyLoadedGroup(id: Long) {
        if (lazyLoadedGroups.add(id)) {
//            exercisesUpdater.restart()
        }
    }

    override fun isLazyLoadedGroup(id: Long): Boolean {
        return lazyLoadedGroups.contains(id)
    }
}
