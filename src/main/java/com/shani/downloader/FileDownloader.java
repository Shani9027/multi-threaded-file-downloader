package com.shani.downloader;

import com.shani.model.DownloadRequest;
import com.shani.model.DownloadStatus;
import com.shani.model.DownloadTask;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 * Represents the foundation of a single-file download engine.
 * <p>
 * This class holds the task context for a future download workflow without
 * performing networking, file writing, chunk coordination, or progress
 * tracking yet.
 * </p>
 */
public class FileDownloader {

    private final DownloadTask task;

    /**
     * Creates a new file downloader for the given download task.
     *
     * @param task the task to associate with this downloader
     * @throws IllegalArgumentException if task is null
     */
    public FileDownloader(DownloadTask task) {
        if (task == null) {
            throw new IllegalArgumentException("task cannot be null");
        }
        this.task = task;
    }

    /**
     * Returns the download task associated with this downloader.
     *
     * @return the current download task
     */
    public DownloadTask getTask() {
        return task;
    }

    /**
     * Downloads the file described by the configured task to the target location.
     * <p>
     * The source URL is validated by {@link HttpDownloadClient}, the destination
     * path is resolved from the request output directory and file name, and the
     * response body is copied to disk using buffered streams.
     * </p>
     *
     * @throws IllegalStateException if the download cannot be completed
     */
    public void download() {
        DownloadRequest request = task.getRequest();
        Path destinationPath = resolveDestinationPath(request);

        HttpDownloadClient client = new HttpDownloadClient(request.getDownloadUrl());
        HttpURLConnection connection = null;

        try {
            task.setStatus(DownloadStatus.IN_PROGRESS);
            connection = client.openConnection();

            int responseCode = connection.getResponseCode();
            if (responseCode < HttpURLConnection.HTTP_OK || responseCode >= HttpURLConnection.HTTP_MULT_CHOICE) {
                throw new IOException("Unexpected HTTP response code " + responseCode + " for URL " + request.getDownloadUrl());
            }

            Path parentDirectory = destinationPath.getParent();
            if (parentDirectory != null) {
                Files.createDirectories(parentDirectory);
            }

            try (InputStream inputStream = new BufferedInputStream(connection.getInputStream());
                 OutputStream outputStream = new BufferedOutputStream(Files.newOutputStream(
                         destinationPath,
                         StandardOpenOption.CREATE,
                         StandardOpenOption.TRUNCATE_EXISTING,
                         StandardOpenOption.WRITE))) {
                byte[] buffer = new byte[8_192];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            }

            task.setStatus(DownloadStatus.COMPLETED);
        } catch (IOException | RuntimeException exception) {
            task.setStatus(DownloadStatus.FAILED);
            throw new IllegalStateException(
                    "Failed to download file from " + request.getDownloadUrl() + " to " + destinationPath,
                    exception);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private static Path resolveDestinationPath(DownloadRequest request) {
        if (request.getDownloadUrl() == null || request.getDownloadUrl().isBlank()) {
            throw new IllegalArgumentException("downloadUrl cannot be null or blank");
        }
        if (request.getFileName() == null || request.getFileName().isBlank()) {
            throw new IllegalArgumentException("fileName cannot be null or blank");
        }
        if (request.getOutputDirectory() == null || request.getOutputDirectory().isBlank()) {
            throw new IllegalArgumentException("outputDirectory cannot be null or blank");
        }

        try {
            Path outputDirectory = Paths.get(request.getOutputDirectory()).normalize();
            Path destinationPath = outputDirectory.resolve(request.getFileName()).normalize();

            if (!destinationPath.startsWith(outputDirectory)) {
                throw new IllegalArgumentException("fileName must resolve inside the outputDirectory");
            }

            return destinationPath;
        } catch (InvalidPathException exception) {
            throw new IllegalArgumentException("outputDirectory or fileName is not a valid path", exception);
        }
    }
}