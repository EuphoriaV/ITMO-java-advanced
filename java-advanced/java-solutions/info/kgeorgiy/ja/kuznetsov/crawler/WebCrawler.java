package info.kgeorgiy.ja.kuznetsov.crawler;

import info.kgeorgiy.java.advanced.crawler.*;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

/**
 * Crawls websites.
 *
 * @author Ilya Kuznetsov (ilyakuznecov84@gmail.com)
 * Class implements {@link Crawler}
 */
public class WebCrawler implements Crawler {
    private final Downloader downloader;
    private final ExecutorService downloadService;
    private final ExecutorService extractService;

    public WebCrawler(Downloader downloader, int downloaders, int extractors, int perHost) {
        this.downloader = downloader;
        downloadService = Executors.newFixedThreadPool(downloaders);
        extractService = Executors.newFixedThreadPool(extractors);
    }

    /**
     * Downloads website up to specified depth.
     *
     * @param url   start <a href="http://tools.ietf.org/html/rfc3986">URL</a>.
     * @param depth download depth.
     * @return download result.
     */
    public Result download(String url, int depth) {
        Map<String, IOException> errors = new ConcurrentHashMap<>();
        Set<String> downloaded = ConcurrentHashMap.newKeySet();
        Set<String> allLinks = ConcurrentHashMap.newKeySet();
        Set<Future<?>> tasks = new HashSet<>();
        Queue<Link> queue = new ConcurrentLinkedQueue<>();
        queue.add(new Link(url, depth));
        while (hasUnfinishedTasks(tasks, queue)) {
            Link link = queue.poll();
            Future<?> future = downloadService.submit(() -> {
                Document document = null;
                try {
                    boolean willProcess = false;
                    synchronized (allLinks) {
                        if (!allLinks.contains(link.url())) {
                            willProcess = true;
                            allLinks.add(link.url());
                        }
                    }
                    if (willProcess) {
                        document = downloader.download(link.url());
                        downloaded.add(link.url());
                    }
                } catch (IOException e) {
                    errors.put(link.url(), e);
                }
                if (document == null || link.depth == 1) {
                    return;
                }
                var futureExtractedLinks = extractService.submit(document::extractLinks);
                List<String> extractedLinks;
                try {
                    extractedLinks = futureExtractedLinks.get();
                } catch (InterruptedException | ExecutionException e) {
                    throw new RuntimeException(e);
                }
                extractedLinks.forEach(link1 -> queue.add(new Link(link1, link.depth() - 1)));
            });
            tasks.add(future);
        }
        return new Result(List.copyOf(downloaded), errors);
    }

    private boolean hasUnfinishedTasks(Set<Future<?>> tasks, Queue<Link> queue) {
        while (queue.isEmpty()) {
            tasks.removeIf(Future::isDone);
            if (tasks.isEmpty() && queue.isEmpty()) {
                return false;
            } else {
                List<CompletableFuture<?>> completableFutures = new ArrayList<>();
                tasks.forEach(task -> completableFutures.add(CompletableFuture.supplyAsync(() -> {
                    try {
                        return task.get();
                    } catch (InterruptedException | ExecutionException e) {
                        throw new RuntimeException(e);
                    }
                })));
                CompletableFuture<?> anyTask = CompletableFuture.
                        anyOf(completableFutures.toArray(new CompletableFuture[0]));
                try {
                    anyTask.get();
                } catch (InterruptedException | ExecutionException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return true;
    }

    record Link(String url, int depth) {
    }

    /**
     * Closes this web-crawler.
     */
    public void close() {
        downloadService.shutdownNow();
        extractService.shutdownNow();
    }

    /**
     * Main method that crawls website. Arguments: url depth downloaders extractors perHost
     *
     * @param args program arguments
     */
    public static void main(String[] args) {
        if (args == null || args.length != 5 || args[0] == null ||
                args[1] == null || args[2] == null || args[3] == null || args[4] == null) {
            System.err.println("Invalid arguments");
            return;
        }
        try {
            String url = args[0];
            int depth = Integer.parseInt(args[1]);
            int downloaders = Integer.parseInt(args[2]);
            int extractors = Integer.parseInt(args[3]);
            int perHost = Integer.parseInt(args[4]);
            WebCrawler webCrawler = new WebCrawler(new CachingDownloader(1000), downloaders, extractors, perHost);
            Result result = webCrawler.download(url, depth);
            System.out.println("-----------------------------------------------");
            System.out.println("Downloaded successfully:");
            for (String downoaded : result.getDownloaded()) {
                System.out.println("\t" + downoaded);
            }
            System.out.println("-----------------------------------------------");
            System.out.println("Errors occurred while downloading:");
            for (var entry : result.getErrors().entrySet()) {
                System.out.println("\t" + entry.getKey() + ": " + entry.getValue().getMessage());
            }
        } catch (NumberFormatException e) {
            System.err.println("Every argument apart from url must be an integer");
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }
}
