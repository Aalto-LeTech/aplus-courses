package fi.aalto.cs.apluscourses.model.tutorial;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import org.jetbrains.annotations.NotNull;

public class TutorialSource {
  private final TutorialFactory factory;
  private final UrlDownloader urlDownloader;

  public TutorialSource(@NotNull TutorialFactory factory, UrlDownloader urlDownloader) {
    this.factory = factory;
    this.urlDownloader = urlDownloader;
  }

  public @NotNull Tutorial getTutorialOrDie(@NotNull String url) {
    try {
      return getTutorial(url);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public @NotNull Tutorial getTutorial(@NotNull String url) throws IOException {
    return getTutorial(new URL(url));
  }

  private Tutorial getTutorial(@NotNull URL url) throws IOException {
    return Tutorial.fromStream(urlDownloader.fetch(url), factory);
  }

  @FunctionalInterface
  public interface UrlDownloader {
    @NotNull InputStream fetch(@NotNull URL url) throws IOException;
  }
}
