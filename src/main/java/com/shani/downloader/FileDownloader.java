package com.shani.downloader;

import com.shani.model.DownloadTask;

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
     * Placeholder for the future file download workflow.
     */
    public void download() {
        // TODO: Implement HTTP connection handling, file writing, chunk downloading, and multithreading here.
    }
}