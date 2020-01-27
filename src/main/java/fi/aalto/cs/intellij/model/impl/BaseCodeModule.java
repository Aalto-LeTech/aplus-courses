package fi.aalto.cs.intellij.model.impl;

import fi.aalto.cs.intellij.model.CodeModule;
import fi.aalto.cs.intellij.model.Course;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import org.jetbrains.annotations.NotNull;

public abstract class BaseCodeModule<T> extends CodeModule {

  private final TempFileFactory tempFileFactory;
  private final UrlDownloader urlDownloader;
  private final UnZipper unzipper;
  private final ModuleLoader<T> moduleLoader;

  private volatile T module;

  public BaseCodeModule(@NotNull Course course,
                        @NotNull String name,
                        @NotNull String url,
                        @NotNull TempFileFactory tempFileFactory,
                        @NotNull UrlDownloader urlDownloader,
                        @NotNull UnZipper unzipper,
                        @NotNull ModuleLoader<T> moduleLoader) {
    super(course, name, url);
    this.tempFileFactory = tempFileFactory;
    this.urlDownloader = urlDownloader;
    this.unzipper = unzipper;
    this.moduleLoader = moduleLoader;
  }

  @Override
  protected void installOverride() {
    try {
      File tempFile = tempFileFactory.createTempFile(getName() + ".zip");
      urlDownloader.copyUrlToFile(new URL(getUrl()), tempFile);
      unzipper.unzip(tempFile, getPath());
      module = moduleLoader.loadModule(new File(getPath(), getName() + ".iml").getPath());
    } catch (Exception e) {
      throw new InstallationFailedRuntimeException(e);
    }
  }

  @Override
  public IntelliJCourse getCourse() {
    return (IntelliJCourse) super.getCourse();
  }

  protected T getModule() {
    return module;
  }

  @FunctionalInterface
  interface TempFileFactory {
    File createTempFile(@NotNull String prefix) throws IOException;
  }

  @FunctionalInterface
  interface UrlDownloader {
    void copyUrlToFile(URL source, File destination) throws IOException;
  }

  @FunctionalInterface
  interface UnZipper {
    void unzip(File zipFile, String destinationPath) throws IOException;
  }

  @FunctionalInterface
  interface ModuleLoader<T> {
    T loadModule(@NotNull String filePath) throws Exception;
  }
}
