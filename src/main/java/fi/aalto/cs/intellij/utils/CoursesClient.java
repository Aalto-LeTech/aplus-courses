package fi.aalto.cs.intellij.utils;

import fi.aalto.cs.intellij.common.UnexpectedResponseBodyException;
import fi.aalto.cs.intellij.common.UnexpectedResponseException;
import fi.aalto.cs.intellij.common.UnexpectedResponseHeadersException;
import fi.aalto.cs.intellij.common.UnexpectedResponseStatusException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;

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
   * A functional interface for that maps a {@link HttpEntity} to a desired result.
   * @param <T> The type of the result of {@link EntityMapper#map}.
   */
  @FunctionalInterface
  public interface EntityMapper<T> {
    T map(@NotNull HttpEntity entity);
  }

  /**
   * Fetches content from the given URL and returns the result.
   * @param url              The URL to which the GET request is made.
   * @param expectedMimeType The expected value of the Content-Type header of the response, or null
   *                         if no checking of the MIME type should be done.
   * @param mapper           A {@link EntityMapper} that converts the {@link HttpEntity} containing
   *                         the response body to the desired format.
   * @param <T> The result type of the {@code mapper}.
   * @return The result of {@code mapper.map(response)}, where response is a {@link HttpEntity}
   *         containing the response body.
   * @throws IOException                 If an issue occurs while making the request, which includes
   *                                     cases such as an unknown host.
   * @throws UnexpectedResponseException Throws an instance of {@link
   *                                     UnexpectedResponseStatusException} if the status of the
   *                                     response isn't 2xx, an instance of {@link
   *                                     UnexpectedResponseHeadersException} if the Content-Type
   *                                     header doesn't match the expected value, or an instance of
   *                                     {@link UnexpectedResponseBodyException} if the response is
   *                                     missing a body.
   */
  @NotNull
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
   * Makes a GET request to the given URL and returns the response body.
   * <p>
   * Note: as this method returns a {@link ByteArrayInputStream}, the entire body of the HTTP
   * response is read into memory, meaning that this method should be avoided for larger files.
   * </p>
   * @param url              The URL to which the request is made.
   * @param expectedMimeType The expected value of the Content-Type header of the response, or null
   *                         if no checking of the MIME type should be done.
   * @return A {@link ByteArrayInputStream} containing the response body.
   */
  @NotNull
  public static ByteArrayInputStream fetch(@NotNull URL url, @Nullable String expectedMimeType)
      throws IOException, UnexpectedResponseException {
    return fetch(url, expectedMimeType, toByteArrayInputStream);
  }

  /**
   * Equivalent to {@code fetch(url, null)}.
   */
  @NotNull
  public static ByteArrayInputStream fetch(@NotNull URL url)
      throws IOException, UnexpectedResponseException {
    return fetch(url, null);
  }

  /**
   * Equivalent to {@code fetch(url, CourseClient.JSON_MIME_TYPE)}.
   */
  @NotNull
  public static ByteArrayInputStream fetchJson(@NotNull URL url)
      throws IOException, UnexpectedResponseException {
    return fetch(url, JSON_MIME_TYPE);
  }

  /**
   * Equivalent to {@code fetch(url, CourseClient.ZIP_MIME_TYPE}.
   */
  @NotNull
  public static ByteArrayInputStream fetchZip(@NotNull URL url)
      throws IOException, UnexpectedResponseException {
    return fetch(url, ZIP_MIME_TYPE);
  }

  /** A constant for the MIME type of JSON text, that is, "application/json". */
  public static final String JSON_MIME_TYPE = "application/json";
  /** A constant for the MIME type of ZIP archives, that is, "application/zip". */
  public static final String ZIP_MIME_TYPE = "application/zip";

  private static final EntityMapper<ByteArrayInputStream> toByteArrayInputStream = entity -> {
    try {
      return new ByteArrayInputStream(EntityUtils.toByteArray(entity));
    } catch (IOException ex) {
      throw new UncheckedIOException(ex);
    }
  };

  /**
   * Executes the given request, performs some checks on the response and returns the result of
   * passing the response body to the given mapper.
   * @param request             The HTTP request to be executed.
   * @param expectedContentType The expected value of the Content-Type header of the response, or
   *                            {@code null} if the header shouldn't be checked.
   * @param mapper              A {@link EntityMapper mapping }
   * @param <T> The type of the result returned.
   * @return The result of {@code mapper.map(entity)}, where {@code entity} is a {@link HttpEntity}
   *         containing the body of the response.
   * @throws IOException                 If an error occurs in the execution of the request.
   * @throws UnexpectedResponseException Throws an instance of {@link
   *                                     UnexpectedResponseStatusException} if the status of the
   *                                     response isn't 2xx, an instance of {@link
   *                                     UnexpectedResponseHeadersException} if the Content-Type
   *                                     header doesn't match the expected value, or an instance of
   *                                     {@link UnexpectedResponseBodyException} if the response is
   *                                     missing a body.
   */
  @NotNull
  public static <T> T getResponseBody(@NotNull HttpUriRequest request,
                                      @Nullable String expectedContentType,
                                      @NotNull EntityMapper<T> mapper)
      throws IOException, UnexpectedResponseException {
    try (CloseableHttpClient client = HttpClients.createDefault();
         CloseableHttpResponse response = client.execute(request)) {
      requireSuccessStatusCode(response);
      requireContentType(response, expectedContentType);
      HttpEntity entity = response.getEntity();
      if (entity == null) {
        throw new UnexpectedResponseBodyException("Response is missing body", null);
      }
      return mapper.map(entity);
    }
  }

  /**
   * Makes a GET request to the given URL and returns the value of the Last-Modified header of the
   * response. Note that some servers don't add the Last-Modified header to the response.
   * @param url The URL to which the request is made.
   * @return A string containing the value of the Last-Modified header.
   * @throws IOException                 If an error occurs in the execution of the request.
   * @throws UnexpectedResponseException If the status code of the response isn't 2xx (in which case
   *                                     the thrown exception is an instance of {@link
   *                                     UnexpectedResponseStatusException}, or if the the response
   *                                     doesn't contain the Last-Modified header (in which case the
   *                                     thrown exception is an instance of {@link
   *                                     UnexpectedResponseHeadersException}).
   */
  @NotNull
  String getLastModified(@NotNull URL url) throws IOException, UnexpectedResponseException {
    HttpGet request = new HttpGet(url.toString());
    try (CloseableHttpClient client = HttpClients.createDefault();
         CloseableHttpResponse response = client.execute(request)) {
      requireSuccessStatusCode(response);
      Header lastModified = response.getLastHeader("Last-Modified");
      if (lastModified == null) {
        throw new UnexpectedResponseHeadersException("Response is missing Last-Modified header",
            null);
      }
      return lastModified.getValue();
    }
  }



  /**
   * Throws {@link UnexpectedResponseStatusException} if the given response status code isn't 2xx,
   * otherwise does nothing.
   * @param response The HTTP response from which the status code is checked.
   * @throws UnexpectedResponseStatusException If the status code of the response doesn't indicate
   *                                           success.
   */
  @NotNull
  private static void requireSuccessStatusCode(@NotNull HttpResponse response)
      throws UnexpectedResponseStatusException {
    StatusLine statusLine = response.getStatusLine();
    int statusCode = statusLine.getStatusCode();
    if (statusCode < 200 || statusCode >= 300) {
      throw new UnexpectedResponseStatusException(statusLine, null);
    }
  }

  /**
   * Throws {@link UnexpectedResponseHeadersException} if the given response is missing the
   * Content-Type header, or if the value is not equal to the given content type string.
   * @param response The response from which the header is checked.
   * @param expected The expected content type of the response. If the parameter is null, then this
   *                 method does nothing.
   * @throws UnexpectedResponseHeadersException If the Content-Type header of the response doesn't
   *                                            match the expected value.
   */
  private static void requireContentType(@NotNull HttpResponse response, @Nullable String expected)
      throws UnexpectedResponseHeadersException {
    if (expected == null) {
      return;
    }

    Header contentType = response.getLastHeader("Content-Type");
    if (contentType == null || !contentType.getValue().equals(expected)) {
      throw new UnexpectedResponseHeadersException("Unexpected Content-Type header", null);
    }
  }

}
