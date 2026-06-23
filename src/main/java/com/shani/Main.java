package com.shani;

import com.shani.manager.DownloadManager;

/**
 * Application entry point for the multi-threaded file downloader.
 *
 * <p>This class is intentionally minimal and only wires startup concerns.</p>
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
        downloadManager.initialize();
        downloadManager.start();
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