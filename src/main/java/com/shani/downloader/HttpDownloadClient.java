package com.shani.downloader;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * Responsible for preparing HTTP connections for file download workflows.
 * <p>
 * This component validates and retains the source URL, but it does not read
 * response data, write files, or coordinate multithreaded download behavior.
 * </p>
 */
public final class HttpDownloadClient {

    private final URL downloadUrl;

    /**
     * Creates a new HTTP download client for the given download URL.
     *
     * @param downloadUrl the source URL to use for future HTTP connections
     * @throws IllegalArgumentException if the URL is null, blank, malformed, or not HTTP-based
     */
    public HttpDownloadClient(String downloadUrl) {
        if (downloadUrl == null || downloadUrl.isBlank()) {
            throw new IllegalArgumentException("downloadUrl cannot be null or blank");
        }

        this.downloadUrl = parseAndValidateUrl(downloadUrl);
    }

    /**
     * Returns the validated source URL.
     *
     * @return the download URL used by this client
     */
    public URL getDownloadUrl() {
        return downloadUrl;
    }

    /**
     * Opens a configured HTTP connection for the validated source URL.
     * <p>
     * The returned connection is ready for later use by higher-level download
     * orchestration, but no data is read here.
     * </p>
     *
     * @return an initialized HTTP connection
     * @throws IOException if the connection cannot be created
     */
    public HttpURLConnection openConnection() throws IOException {
        HttpURLConnection connection = (HttpURLConnection) downloadUrl.openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(10_000);
        connection.setReadTimeout(10_000);
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
}