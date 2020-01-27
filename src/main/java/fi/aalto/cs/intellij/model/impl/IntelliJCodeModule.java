package fi.aalto.cs.intellij.model.impl;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.util.io.FileUtilRt;
import net.lingala.zip4j.ZipFile;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;

public class IntelliJCodeModule extends BaseCodeModule<Module> {
  public IntelliJCodeModule(@NotNull IntelliJCourse course,
                            @NotNull String name,
                            @NotNull String url) {
    super(course,
        name,
        url,
        prefix -> FileUtilRt.createTempFile(prefix, null),
        FileUtils::copyURLToFile,
        (zipFile, destinationPath) -> new ZipFile(zipFile).extractAll(destinationPath),
        ModuleManager.getInstance(course.getProject())::loadModule);
  }
}
