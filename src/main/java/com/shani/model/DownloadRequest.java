package com.shani.model;

/**
 * Represents a single request to download a file.
 * <p>
 * This model captures the input required by the download manager without
 * including any download, networking, or file handling behavior.
 * </p>
 */
public final class DownloadRequest {

    private final String downloadUrl;
    private final String fileName;
    private final String outputDirectory;
    private final int threadCount;

    /**
     * Creates a new download request.
     *
     * @param downloadUrl the source URL for the file to download
     * @param fileName the target file name
     * @param outputDirectory the directory where the file should be stored
     * @param threadCount the number of threads to use later when processing the download
     * @throws IllegalArgumentException if any argument is invalid
     */
    public DownloadRequest(String downloadUrl, String fileName, String outputDirectory, int threadCount) {
        if (downloadUrl == null || downloadUrl.isBlank()) {
            throw new IllegalArgumentException("downloadUrl cannot be null or blank");
        }
        if (fileName == null || fileName.isBlank()) {
            throw new IllegalArgumentException("fileName cannot be null or blank");
        }
        if (outputDirectory == null || outputDirectory.isBlank()) {
            throw new IllegalArgumentException("outputDirectory cannot be null or blank");
        }
        if (threadCount <= 0) {
            throw new IllegalArgumentException("threadCount must be greater than zero");
        }

        this.downloadUrl = downloadUrl;
        this.fileName = fileName;
        this.outputDirectory = outputDirectory;
        this.threadCount = threadCount;
    }

    /**
     * Returns the source URL for the download.
     *
     * @return the download URL
     */
    public String getDownloadUrl() {
        return downloadUrl;
    }

    /**
     * Returns the target file name.
     *
     * @return the file name
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * Returns the directory where the file should be stored.
     *
     * @return the output directory
     */
    public String getOutputDirectory() {
        return outputDirectory;
    }

    /**
     * Returns the number of threads intended for later download processing.
     *
     * @return the thread count
     */
    public int getThreadCount() {
        return threadCount;
    }
}
