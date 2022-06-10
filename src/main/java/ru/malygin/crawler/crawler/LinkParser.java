package ru.malygin.crawler.crawler;

import lombok.RequiredArgsConstructor;
import ru.malygin.crawler.model.entity.Page;

import java.util.Queue;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The class that parse and extract links from page
 *
 * @author Nikolay Malygin
 * @version 1.0
 * @see SiteFetcher
 */

@RequiredArgsConstructor
public class LinkParser implements Runnable {

    private final Queue<String> links;
    private final Queue<Page> pages;
    private final Queue<Page> pageToSave;
    private final AtomicBoolean serve = new AtomicBoolean(false);
    private final AtomicInteger completeTasks = new AtomicInteger(0);
    private Boolean runFlag;

    /**
     * Starts an algorithm that polls the queue with pages ready for parsing, after parsing and extracting links, adds them to the queue for links ready for downloading
     */
    public void start() {
        Thread thread = new Thread(this);
        thread.setName("ParseLinks-" + new Random().nextLong());
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
        runFlag = true;
        while (runFlag) {
            Page page = pages.poll();
            if (page != null) {
                serve.set(true);
                pageToSave.add(page);
                if (page.getCode() == 200) links.addAll(SiteFetcher.getLinks(page));
                this.completeTasks.incrementAndGet();
                serve.set(false);
            }

        }
    }

    /**
     * Returns true if the algorithm executes the task, false otherwise
     * @return active task presence
     */
    public boolean getServe() {
        return serve.get();
    }

    /**
     * Returns the total number of completed tasks
     * @return completed tasks count
     */
    public int getCompleteTasks() {
        return completeTasks.get();
    }
}
