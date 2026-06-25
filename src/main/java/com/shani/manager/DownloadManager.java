package com.shani.manager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.shani.model.DownloadTask;

/**
 * Manages download tasks in memory.
 * <p>
 * This class stores download tasks and provides read-only access to the
 * current collection without handling execution, networking, or file I/O.
 * </p>
 */
public class DownloadManager {

    private final List<DownloadTask> tasks;

    /**
     * Creates a new download manager with an empty task collection.
     */
    public DownloadManager() {
        this.tasks = new ArrayList<>();
    }

    /**
     * Adds a download task to the in-memory collection.
     *
     * @param task the task to add
     * @throws IllegalArgumentException if task is null
     */
    public void addTask(DownloadTask task) {
        if (task == null) {
            throw new IllegalArgumentException("task cannot be null");
        }
        tasks.add(task);
    }

    /**
     * Returns all managed download tasks as an unmodifiable view.
     *
     * @return an unmodifiable view of the current task collection
     */
    public List<DownloadTask> getAllTasks() {
        return Collections.unmodifiableList(tasks);
    }

    /**
     * Returns the total number of managed download tasks.
     *
     * @return the current task count
     */
    public int getTaskCount() {
        return tasks.size();
    }
}