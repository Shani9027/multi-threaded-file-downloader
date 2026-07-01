package com.shani.downloader;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import com.shani.model.DownloadMetadata;

/**
 * Retrieves HTTP response metadata for a downloadable resource.
 * <p>
 * This client performs a single HTTP {@code HEAD} request, validates the
 * response, and converts the returned headers into an immutable
 * {@link DownloadMetadata} instance. It does not download content, write files,
 * track progress, or manage retries.
 * </p>
 */
public final class HttpMetadataClient {

    private static final int CONNECT_TIMEOUT_MILLIS = 10_000;
    private static final int READ_TIMEOUT_MILLIS = 10_000;
    private static final String DEFAULT_FILE_NAME = "download.bin";

    private final URL downloadUrl;

    /**
     * Creates a metadata client for the given download URL.
     *
     * @param downloadUrl the source URL to inspect
     * @throws IllegalArgumentException if the URL is null, blank, malformed, or not HTTP-based
     */
    public HttpMetadataClient(String downloadUrl) {
        if (downloadUrl == null || downloadUrl.isBlank()) {
            throw new IllegalArgumentException("downloadUrl cannot be null or blank");
        }

        this.downloadUrl = parseAndValidateUrl(downloadUrl);
    }

    /**
     * Performs a HEAD request and returns the server-reported metadata.
     *
     * @return metadata describing the remote resource
     * @throws IOException if the request fails or the connection cannot be established
     * @throws IllegalStateException if the server response is not successful
     */
    public DownloadMetadata fetchMetadata() throws IOException {
        HttpURLConnection connection = openHeadConnection();
        try {
            int responseCode = connection.getResponseCode();
            if (responseCode < 200 || responseCode >= 300) {
                throw new IllegalStateException("Metadata request failed with HTTP status " + responseCode);
            }

            long contentLength = connection.getHeaderFieldLong("Content-Length", -1L);
            String contentType = connection.getContentType();
            String acceptRangesHeader = connection.getHeaderField("Accept-Ranges");
            boolean acceptRanges = acceptRangesHeader != null && "bytes".equalsIgnoreCase(acceptRangesHeader.trim());
            String eTag = connection.getHeaderField("ETag");
            String lastModified = connection.getHeaderField("Last-Modified");

            return new DownloadMetadata(
                    resolveFileName(downloadUrl),
                    contentLength,
                    contentType,
                    acceptRanges,
                    eTag,
                    lastModified);
        } finally {
            connection.disconnect();
        }
    }

    private HttpURLConnection openHeadConnection() throws IOException {
        HttpURLConnection connection = (HttpURLConnection) downloadUrl.openConnection();
        connection.setRequestMethod("HEAD");
        connection.setConnectTimeout(CONNECT_TIMEOUT_MILLIS);
        connection.setReadTimeout(READ_TIMEOUT_MILLIS);
        connection.setInstanceFollowRedirects(true);
        connection.setUseCaches(false);
        return connection;
    }

    private static URL parseAndValidateUrl(String downloadUrl) {
        final URL url;
        try {
            url = new URI(downloadUrl).toURL();
        } catch (URISyntaxException | IllegalArgumentException exception) {
            throw new IllegalArgumentException("downloadUrl must be a valid URL", exception);
        } catch (IOException exception) {
            throw new IllegalArgumentException("downloadUrl must be a valid URL", exception);
        }

        String protocol = url.getProtocol();
        if (!"http".equalsIgnoreCase(protocol) && !"https".equalsIgnoreCase(protocol)) {
            throw new IllegalArgumentException("downloadUrl must use http or https");
        }

        return url;
    }

    private static String resolveFileName(URL url) {
        String path = url.getPath();
        if (path == null || path.isBlank() || path.endsWith("/")) {
            return DEFAULT_FILE_NAME;
        }

        int lastSlashIndex = path.lastIndexOf('/');
        String fileName = lastSlashIndex >= 0 ? path.substring(lastSlashIndex + 1) : path;
        if (fileName.isBlank()) {
            return DEFAULT_FILE_NAME;
        }

        return fileName;
    }
}