package com.shani.tracker;

import java.util.Locale;
import java.util.Objects;

/**
 * Renders a console-friendly ASCII progress bar from a {@link ProgressTracker}.
 * <p>
 * This class is intentionally presentation-only: it does not calculate progress,
 * store download state, perform I/O, or manage threading. It reads the current
 * values from a tracker instance and formats them into a single line that can be
 * printed by the caller.
 * </p>
 */
public final class ConsoleProgressBar {

    private static final int DEFAULT_BAR_WIDTH = 30;

    private final ProgressTracker progressTracker;
    private final int barWidth;

    /**
     * Creates a progress bar renderer with the default bar width.
     *
     * @param progressTracker the tracker that supplies current progress values
     * @throws NullPointerException if {@code progressTracker} is {@code null}
     */
    public ConsoleProgressBar(ProgressTracker progressTracker) {
        this(progressTracker, DEFAULT_BAR_WIDTH);
    }

    /**
     * Creates a progress bar renderer with a custom bar width.
     *
     * @param progressTracker the tracker that supplies current progress values
     * @param barWidth the number of characters used for the visual bar; must be greater than zero
     * @throws NullPointerException if {@code progressTracker} is {@code null}
     * @throws IllegalArgumentException if {@code barWidth} is not greater than zero
     */
    public ConsoleProgressBar(ProgressTracker progressTracker, int barWidth) {
        this.progressTracker = Objects.requireNonNull(progressTracker, "progressTracker");
        if (barWidth <= 0) {
            throw new IllegalArgumentException("barWidth must be greater than zero");
        }
        this.barWidth = barWidth;
    }

    /**
     * Renders the current download progress as a single ASCII line.
     * <p>
     * The output includes a bar, completion percentage, downloaded bytes, and
     * total bytes. The method performs no printing; callers decide when and how
     * to display the returned string.
     * </p>
     *
     * @return the formatted progress line
     * @throws IllegalStateException if the tracker has not been initialized
     */
    public String render() {
        long downloadedBytes = progressTracker.getDownloadedBytes();
        long totalBytes = progressTracker.getTotalBytes();
        double percentage = progressTracker.getDownloadPercentage();

        String bar = buildBar(percentage);
        String percentageText = String.format(Locale.ROOT, "%6.2f%%", percentage);

        return bar + " " + percentageText + " | "
                + downloadedBytes + " / " + totalBytes + " bytes";
    }

    private String buildBar(double percentage) {
        int filledCharacters = (int) Math.round((percentage / 100.0d) * barWidth);
        if (filledCharacters < 0) {
            filledCharacters = 0;
        } else if (filledCharacters > barWidth) {
            filledCharacters = barWidth;
        }

        StringBuilder builder = new StringBuilder(barWidth + 2);
        builder.append('[');
        for (int i = 0; i < barWidth; i++) {
            builder.append(i < filledCharacters ? '=' : '-');
        }
        builder.append(']');
        return builder.toString();
    }
}
