package fi.aalto.cs.apluscourses.utils;

import fi.aalto.cs.apluscourses.model.UnexpectedResponseException;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CoursesClient {

  /**
   * Downloads a JSON text from the given URl and returns it in a {@link ByteArrayInputStream}.
   * @throws IOException                 If an error (e.g. network error) occurs while downloading
   *                                     the file.
   * @throws UnexpectedResponseException If the response isn't as expected (i.e. a status code other
   *                                     than 2xx or different Content-Type header).
   */
  @NotNull
  public static ByteArrayInputStream fetchJson(@NotNull URL url)
      throws IOException, UnexpectedResponseException {
    return fetch(url, "application/json",
        entity -> new ByteArrayInputStream(EntityUtils.toByteArray(entity)));
  }

  /**
   * Downloads a ZIP archive from the given URL and copies it to the given file.
   * @throws IOException                 If an error (e.g. network error) occurs while downloading
   *                                     the file.
   * @throws UnexpectedResponseException If the response isn't as expected (i.e. a status code other
   *                                     than 2xx or different Content-Type header).
   */
  public static void fetchZip(@NotNull URL url, @NotNull File file)
      throws IOException, UnexpectedResponseException {
    fetch(url, "application/zip", entity -> {
      FileUtils.copyInputStreamToFile(entity.getContent(), file);
      return null; // The return value gets ignored here
    });
  }

  /**
   * Makes a GET request to the given URL and returns the value of the Last-Modified header of the
   * response. Note that some servers don't add the Last-Modified header to the response.
   * @return A string containing the value of the Last-Modified header.
   * @throws IOException                 If an error occurs in the execution of the request.
   * @throws UnexpectedResponseException If the status code of the response isn't 2xx, or if the
   *                                     response doesn't contain the Last-Modified header
   */
  @NotNull
  String getLastModified(@NotNull URL url) throws IOException, UnexpectedResponseException {
    HttpGet request = new HttpGet(url.toString());
    try (CloseableHttpClient client = HttpClients.createDefault();
         CloseableHttpResponse response = client.execute(request)) {
      requireSuccessStatusCode(response);
      Header lastModified = response.getLastHeader("Last-Modified");
      if (lastModified == null) {
        throw new UnexpectedResponseException(response, "Response is missing Last-Modified header",
            null);
      }
      return lastModified.getValue();
    }
  }

  /**
   * Makes a GET request to the given URL and returns the response body.
   * @param url              The URL to which the GET request is made.
   * @param expectedMimeType The expected value of the Content-Type header of the response, or null
   *                         if no checking of the MIME type should be done.
   * @param mapper           A {@link EntityMapper} that converts the {@link HttpEntity} containing
   *                         the response body to the desired format.
   * @return The result of {@code mapper.map(response)}, where response is a {@link HttpEntity}
   *         containing the response body.
   * @throws IOException                 If an issue occurs while making the request, which includes
   *                                     cases such as an unknown host.
   * @throws UnexpectedResponseException If the status of the response isn't 2xx, if the
   *                                     Content-Type header doesn't match the expected value, or if
   *                                     the response is missing a body.
   */
  public static <T> T fetch(@NotNull URL url,
                            @Nullable String expectedMimeType,
                            @NotNull EntityMapper<T> mapper)
      throws IOException, UnexpectedResponseException {
    HttpGet request = new HttpGet(url.toString());
    if (expectedMimeType != null) {
      request.addHeader("Expect", expectedMimeType);
    }
    return getResponseBody(request, expectedMimeType, mapper);
  }

  /**
   * A functional interface for that maps a {@link HttpEntity} to a desired result.
   */
  @FunctionalInterface
  public interface EntityMapper<T> {
    T map(@NotNull HttpEntity entity) throws IOException;
  }

  /**
   * Executes the given request, performs some checks on the response and returns the result of
   * passing the response body to the given mapper.
   * @param expectedContentType The expected value of the Content-Type header of the response, or
   *                            {@code null} if the header shouldn't be checked.
   * @throws IOException                 If an error occurs in the execution of the request.
   * @throws UnexpectedResponseException If the response isn't as expected (i.e. unexpeted status
   *                                     code or no body in the response).
   */
  private static <T> T getResponseBody(@NotNull HttpUriRequest request,
                                       @Nullable String expectedContentType,
                                       @NotNull EntityMapper<T> mapper)
      throws IOException, UnexpectedResponseException {
    try (CloseableHttpClient client = HttpClients.createDefault();
         CloseableHttpResponse response = client.execute(request)) {
      requireSuccessStatusCode(response);
      requireContentType(response, expectedContentType);
      HttpEntity entity = response.getEntity();
      if (entity == null) {
        throw new UnexpectedResponseException(response, "Response is missing body", null);
      }
      return mapper.map(entity);
    }
  }

  /**
   * Throws {@link UnexpectedResponseException} if the given response status code isn't 2xx,
   * otherwise does nothing.
   * @param response The HTTP response from which the status code is checked.
   */
  @NotNull
  private static void requireSuccessStatusCode(@NotNull HttpResponse response)
      throws UnexpectedResponseException {
    StatusLine statusLine = response.getStatusLine();
    int statusCode = statusLine.getStatusCode();
    if (statusCode < 200 || statusCode >= 300) {
      throw new UnexpectedResponseException(response, "Status code doesn't indicate success", null);
    }
  }

  /**
   * Throws {@link UnexpectedResponseException} if the given response is missing the
   * Content-Type header, or if the value is not equal to the given content type string.
   * @param response The response from which the header is checked.
   * @param expected The expected content type of the response. If the parameter is null, then this
   *                 method does nothing.
   */
  private static void requireContentType(@NotNull HttpResponse response, @Nullable String expected)
      throws UnexpectedResponseException {
    if (expected == null) {
      return;
    }

    Header contentType = response.getLastHeader("Content-Type");
    if (contentType == null || !contentType.getValue().equals(expected)) {
      throw new UnexpectedResponseException(response, "Unexpected Content-Type header", null);
    }
  }

}
