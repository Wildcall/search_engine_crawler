package ru.malygin.crawler.crawler;

import lombok.RequiredArgsConstructor;
import ru.malygin.crawler.model.entity.Page;
import ru.malygin.crawler.service.PageService;

import java.time.LocalDateTime;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A class that saves pages and publishes a save event
 *
 * @author Nikolay Malygin
 * @version 1.0
 * @see PageService
 */

@RequiredArgsConstructor
public class PageSaver implements Runnable {

    private final PageService pageService;
    private final Queue<Page> pageToSave;
    private final Long siteId;
    private final Long appUserId;
    private final AtomicBoolean serve = new AtomicBoolean(false);
    private final AtomicInteger completeTasks = new AtomicInteger(0);
    private final AtomicInteger errorsCount = new AtomicInteger(0);
    private Boolean runFlag = true;

    /**
     * Starts an algorithm that polls the queue with sites ready to be saved, and saves them to the database
     */
    public void start() {
        Thread thread = new Thread(this);
        thread.setName("SavePage-" + new Random().nextLong());
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
        while (runFlag) {
            Page page = pageToSave.poll();
            if (page != null) {
                serve.set(true);

                page.setSiteId(siteId);
                page.setAppUserId(appUserId);
                page.setCreateTime(LocalDateTime.now());

                pageService
                        .save(page)
                        .subscribe();

                if (page.getCode() != 200) errorsCount.incrementAndGet();
                completeTasks.incrementAndGet();
                serve.set(false);
            }
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

    /**
     * Returns the total number of tasks with error
     *
     * @return error tasks count
     */
    public int getErrorsCount() {
        return errorsCount.get();
    }
}
