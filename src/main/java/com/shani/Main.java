package com.shani;

import java.nio.file.Path;
import java.nio.file.Paths;

import com.shani.downloader.FileDownloader;
import com.shani.manager.DownloadManager;
import com.shani.model.DownloadRequest;
import com.shani.model.DownloadTask;

/**
 * Application entry point for the file downloader demo.
 *
 * <p>This class wires the existing download components into a small, runnable
 * example without introducing new architecture or concurrency behavior.</p>
 */
public final class Main {

    /**
     * Prevents instantiation of utility-style entry point class.
     */
    private Main() {
    }

    /**
     * Starts the application runtime.
     *
     * @param args command-line arguments passed by the runtime
     */
    public static void main(final String[] args) {
        printWelcomeBanner();

        DownloadManager downloadManager = new DownloadManager();
        Path outputDirectory = Paths.get(System.getProperty("java.io.tmpdir"), "multi-threaded-file-downloader-demo");

        DownloadRequest request = new DownloadRequest(
                "https://example.com/",
                "example.html",
                outputDirectory.toString(),
                1);
        DownloadTask task = new DownloadTask(request);
        downloadManager.addTask(task);

        try {
            FileDownloader fileDownloader = new FileDownloader(task);
            fileDownloader.download();

            System.out.println("Registered tasks: " + downloadManager.getTaskCount());
            System.out.println("Download status: " + task.getStatus());
            System.out.println("Download completed successfully.");
        } catch (IllegalArgumentException | IllegalStateException exception) {
            System.out.println("Download demo failed: " + exception.getMessage());
        }
    }

    /**
     * Prints a professional startup banner.
     */
    private static void printWelcomeBanner() {
        System.out.println("====================================================");
        System.out.println(" Multi-Threaded File Downloader");
        System.out.println(" Initialized for coordinated download operations");
        System.out.println("====================================================");
    }
}