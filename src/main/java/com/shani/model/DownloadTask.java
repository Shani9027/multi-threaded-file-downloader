package com.shani.model;

/**
 * Represents a single download job with its associated request and status.
 * <p>
 * This model encapsulates the download request and tracks the current state of
 * the download lifecycle. It serves as a domain object that bridges user requests
 * and the download system without including execution logic.
 * </p>
 */
public class DownloadTask {

    private final DownloadRequest request;
    private DownloadStatus status;

    /**
     * Creates a new download task for the given request.
     * <p>
     * The task is initialized with a QUEUED status, indicating that it is
     * waiting to be processed.
     * </p>
     *
     * @param request the download request to be processed
     * @throws IllegalArgumentException if request is null
     */
    public DownloadTask(DownloadRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("request cannot be null");
        }
        this.request = request;
        this.status = DownloadStatus.QUEUED;
    }

    /**
     * Returns the download request associated with this task.
     *
     * @return the download request
     */
    public DownloadRequest getRequest() {
        return request;
    }

    /**
     * Returns the current status of this download task.
     *
     * @return the current download status
     */
    public DownloadStatus getStatus() {
        return status;
    }

    /**
     * Updates the status of this download task to the specified status.
     * <p>
     * This method allows the download system to communicate state transitions
     * as the task moves through its lifecycle (e.g., from QUEUED to IN_PROGRESS
     * to COMPLETED or FAILED).
     * </p>
     *
     * @param newStatus the new status to set
     * @throws IllegalArgumentException if newStatus is null
     */
    public void setStatus(DownloadStatus newStatus) {
        if (newStatus == null) {
            throw new IllegalArgumentException("newStatus cannot be null");
        }
        this.status = newStatus;
    }
}
