package ru.malygin.crawler.crawler;

import lombok.RequiredArgsConstructor;
import ru.malygin.crawler.model.entity.Page;

import java.util.Queue;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The class that downloads the contents of the site
 *
 * @author Nikolay Malygin
 * @version 1.0
 * @see SiteFetcher
 */

@RequiredArgsConstructor
public class PageFetcher implements Runnable {

    private final Queue<String> links;
    private final Queue<Page> pages;
    private final SiteFetcher siteFetcher;
    private final AtomicBoolean serve = new AtomicBoolean(false);
    private final AtomicInteger completeTasks = new AtomicInteger(0);
    private Boolean runFlag;

    /**
     * Starts an algorithm that polls the queue with downloadable links, after downloading the link, adds the content to the queue of pages available for parsing
     */
    public void start() {
        Thread thread = new Thread(this);
        thread.setName("FetchPage-" + new Random().nextLong());
        thread.start();
    }

    /**
     * Stops an algorithm
     */
    public void stop() {
        runFlag = false;
    }

    @Override
    public void run() {
        try {
            runFlag = true;
            while (runFlag) {
                String link = links.poll();
                if (link != null) {
                    serve.set(true);
                    Page page = siteFetcher.fetchPath(link);
                    pages.add(page);
                    TimeUnit.MILLISECONDS.sleep(siteFetcher.getDelayTime());
                    this.completeTasks.incrementAndGet();
                    serve.set(false);
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns true if the algorithm executes the task, false otherwise
     *
     * @return active task presence
     */
    public boolean getServe() {
        return serve.get();
    }

    /**
     * Returns the total number of completed tasks
     *
     * @return completed tasks count
     */
    public int getCompleteTasks() {
        return completeTasks.get();
    }
}
