package com.shani.model;

import java.util.Objects;

/**
 * Immutable metadata describing a downloadable resource as reported by an HTTP server.
 * <p>
 * This model is intentionally limited to validated data only. It is designed to be
 * populated from response headers gathered before a download begins and then passed
 * through orchestration code without carrying any networking, file-system, or business
 * behavior.
 * </p>
 */
public final class DownloadMetadata {

    private static final String DEFAULT_CONTENT_TYPE = "application/octet-stream";

    private final String fileName;
    private final long contentLength;
    private final String contentType;
    private final boolean acceptRanges;
    private final String eTag;
    private final String lastModified;

    /**
     * Creates a new immutable metadata instance.
     *
     * @param fileName the server-reported file name; must not be null or blank
     * @param contentLength the resource length in bytes; must not be negative
     * @param contentType the MIME type reported by the server; defaults to {@code application/octet-stream}
     *                    when null or blank
     * @param acceptRanges whether the server supports ranged requests
     * @param eTag the entity tag reported by the server; may be null
     * @param lastModified the last-modified value reported by the server; may be null
     * @throws IllegalArgumentException if {@code fileName} is blank or {@code contentLength} is negative
     */
    public DownloadMetadata(String fileName, long contentLength, String contentType, boolean acceptRanges, String eTag, String lastModified) {
        if (fileName == null || fileName.isBlank()) {
            throw new IllegalArgumentException("fileName cannot be null or blank");
        }
        if (contentLength < 0L) {
            throw new IllegalArgumentException("contentLength cannot be negative");
        }

        this.fileName = fileName;
        this.contentLength = contentLength;
        this.contentType = normalizeContentType(contentType);
        this.acceptRanges = acceptRanges;
        this.eTag = eTag;
        this.lastModified = lastModified;
    }

    /**
     * Returns the file name associated with this metadata.
     *
     * @return the file name
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * Returns the content length in bytes.
     *
     * @return the content length
     */
    public long getContentLength() {
        return contentLength;
    }

    /**
     * Returns the server-reported content type.
     *
     * @return the content type, never null
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * Indicates whether the server accepts ranged requests.
     *
     * @return {@code true} when ranged requests are supported; otherwise {@code false}
     */
    public boolean isAcceptRanges() {
        return acceptRanges;
    }

    /**
     * Returns the server-reported entity tag, if available.
     *
     * @return the entity tag, or null if not provided
     */
    public String getETag() {
        return eTag;
    }

    /**
     * Returns the server-reported last-modified value, if available.
     *
     * @return the last-modified value, or null if not provided
     */
    public String getLastModified() {
        return lastModified;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        DownloadMetadata that = (DownloadMetadata) other;
        return contentLength == that.contentLength
                && acceptRanges == that.acceptRanges
                && Objects.equals(fileName, that.fileName)
                && Objects.equals(contentType, that.contentType)
                && Objects.equals(eTag, that.eTag)
                && Objects.equals(lastModified, that.lastModified);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fileName, contentLength, contentType, acceptRanges, eTag, lastModified);
    }

    @Override
    public String toString() {
        return "DownloadMetadata{" +
                "fileName='" + fileName + '\'' +
                ", contentLength=" + contentLength +
                ", contentType='" + contentType + '\'' +
                ", acceptRanges=" + acceptRanges +
                ", eTag='" + eTag + '\'' +
                ", lastModified='" + lastModified + '\'' +
                '}';
    }

    private static String normalizeContentType(String contentType) {
        if (contentType == null || contentType.isBlank()) {
            return DEFAULT_CONTENT_TYPE;
        }
        return contentType;
    }
}