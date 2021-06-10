package fi.aalto.cs.apluscourses.utils;

import fi.aalto.cs.apluscourses.model.Authentication;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ParallelDownloader {

  private static class DownloadRequest {
    public final String url;
    public final Authentication authentication;

    private DownloadRequest(String url, Authentication authentication) {
      this.url = url;
      this.authentication = authentication;
    }
  }

  private static class FileDownloadThread extends Thread {
    private final DownloadRequest request;

    public FileDownloadThread(DownloadRequest request) {
      this.request = request;
    }

    @Override
    public void run() {
      try {
        var response = CoursesClient.fetch(new URL(request.url), request.authentication);
        synchronized (completedRequests) {
          completedRequests.put(request.url, response);
          completedRequests.notifyAll();
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  private static class DownloadManagerThread extends Thread {

    private final List<Thread> runningThreads = new ArrayList<>();

    @Override
    public void run() {
      for (;;) {
        synchronized (lock) {
          DownloadRequest request;

          while (runningThreads.size() <= 1 && (request = queuedUrls.poll()) != null) {
            var t = new FileDownloadThread(request);
            t.start();

            runningThreads.add(t);
          }

          runningThreads.removeIf(x -> !x.isAlive());
        }

        try {
          Thread.sleep(25);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }

        if (Thread.interrupted()) {
          return;
        }
      }
    }
  }

  private static final Object lock = new Object();
  private static final Event newResponse = new Event();

  private static final ConcurrentHashMap<String, Object> x = new ConcurrentHashMap<String, Object>();
  private static final Queue<DownloadRequest> queuedUrls = new ArrayDeque<>();
  private static final ConcurrentHashMap<String, ByteArrayInputStream> completedRequests = new ConcurrentHashMap<>();

  private static Thread downloadThread;

  public static void addUrlToQueue(@NotNull String url, @Nullable Authentication authentication) {
    synchronized (lock) {
      System.err.println(url);
      queuedUrls.add(new DownloadRequest(url, authentication));
      x.put(url, 1);

      if (downloadThread == null) {
        downloadThread = new DownloadManagerThread();
        downloadThread.start();
      }
    }
  }

  public static ByteArrayInputStream getResponse(@NotNull String url, @Nullable Authentication authentication) {
    if (x.get(url) == null) {
      try {
        return CoursesClient.fetch(new URL(url), authentication);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    ByteArrayInputStream response = null;

    for (;;) {
      if ((response = completedRequests.get(url)) != null) {
        completedRequests.remove(url);
        return response;
      }

      synchronized (completedRequests) {
        try {
          completedRequests.wait();
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
    }
  }
  
  private ParallelDownloader() {
    
  }
}
