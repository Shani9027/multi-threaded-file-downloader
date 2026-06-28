package com.shani.tracker;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Tracks download progress in bytes and percentage.
 * <p>
 * The tracker is intentionally independent of networking, threading, console
 * output, file I/O, and retry behavior so it can be reused by any download
 * workflow that needs to monitor byte-level progress.
 * </p>
 */
public final class ProgressTracker {

    private static final int PERCENTAGE_SCALE = 4;

    private long totalBytes;
    private long downloadedBytes;
    private boolean initialized;

    /**
     * Creates an uninitialized progress tracker.
     * <p>
     * Call {@link #initializeProgress(long)} before reading or updating progress.
     * </p>
     */
    public ProgressTracker() {
        // Intentionally empty.
    }

    /**
     * Initializes the tracker for a download.
     * <p>
     * This resets previously tracked progress and prepares the tracker to accept
     * byte updates for the new download.
     * </p>
     *
     * @param totalBytes the total file size in bytes; must be zero or greater
     * @throws IllegalArgumentException if {@code totalBytes} is negative
     */
    public void initializeProgress(long totalBytes) {
        if (totalBytes < 0L) {
            throw new IllegalArgumentException("totalBytes cannot be negative");
        }

        this.totalBytes = totalBytes;
        this.downloadedBytes = 0L;
        this.initialized = true;
    }

    /**
     * Adds downloaded bytes to the current progress.
     * <p>
     * The supplied value is treated as an incremental update, not an absolute
     * replacement value.
     * </p>
     *
     * @param downloadedBytes the number of newly downloaded bytes; must be zero or greater
     * @throws IllegalStateException if the tracker has not been initialized
     * @throws IllegalArgumentException if {@code downloadedBytes} is negative or would exceed the total size
     */
    public void updateDownloadedBytes(long downloadedBytes) {
        ensureInitialized();
        if (downloadedBytes < 0L) {
            throw new IllegalArgumentException("downloadedBytes cannot be negative");
        }
        if (downloadedBytes == 0L) {
            return;
        }

        long remainingBytes = totalBytes - this.downloadedBytes;
        if (downloadedBytes > remainingBytes) {
            throw new IllegalArgumentException("downloadedBytes cannot exceed the remaining bytes");
        }

        this.downloadedBytes += downloadedBytes;
    }

    /**
     * Returns the total file size in bytes.
     *
     * @return the total file size in bytes
     * @throws IllegalStateException if the tracker has not been initialized
     */
    public long getTotalBytes() {
        ensureInitialized();
        return totalBytes;
    }

    /**
     * Returns the number of bytes downloaded so far.
     *
     * @return the downloaded byte count
     * @throws IllegalStateException if the tracker has not been initialized
     */
    public long getDownloadedBytes() {
        ensureInitialized();
        return downloadedBytes;
    }

    /**
     * Returns the number of bytes still remaining.
     *
     * @return the remaining byte count
     * @throws IllegalStateException if the tracker has not been initialized
     */
    public long getRemainingBytes() {
        ensureInitialized();
        return totalBytes - downloadedBytes;
    }

    /**
     * Returns the download completion percentage.
     * <p>
     * The result is rounded to four decimal places to keep calculations stable
     * while remaining convenient for presentation or downstream processing.
     * </p>
     *
     * @return the completion percentage between 0.0 and 100.0
     * @throws IllegalStateException if the tracker has not been initialized
     */
    public double getDownloadPercentage() {
        ensureInitialized();
        if (totalBytes == 0L) {
            return 100.0d;
        }

        BigDecimal percentage = BigDecimal.valueOf(downloadedBytes)
                .multiply(BigDecimal.valueOf(100L))
                .divide(BigDecimal.valueOf(totalBytes), PERCENTAGE_SCALE, RoundingMode.HALF_UP);
        return percentage.doubleValue();
    }

    private void ensureInitialized() {
        if (!initialized) {
            throw new IllegalStateException("ProgressTracker has not been initialized");
        }
    }
}
