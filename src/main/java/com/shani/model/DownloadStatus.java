package com.shani.model;

/**
 * Represents the lifecycle state of a download operation.
 * <p>
 * This enum exists so download-related components can communicate state in a
 * clear, type-safe, and consistent way.
 * </p>
 */
public enum DownloadStatus {

    /**
     * Indicates that the download has been accepted and is waiting to start.
     * This exists to distinguish newly created work from work that is already
     * actively running.
     */
    QUEUED,

    /**
     * Indicates that the download is currently being processed.
     * This exists so progress reporting and orchestration can recognize active work.
     */
    IN_PROGRESS,

    /**
     * Indicates that the download completed successfully.
     * This exists to mark the normal terminal state for a finished download.
     */
    COMPLETED,

    /**
     * Indicates that the download ended due to an error.
     * This exists so failures can be distinguished from successful completion and cancellation.
     */
    FAILED,

    /**
     * Indicates that the download was intentionally stopped before completion.
     * This exists to represent user-initiated or system-initiated cancellation.
     */
    CANCELLED
}
