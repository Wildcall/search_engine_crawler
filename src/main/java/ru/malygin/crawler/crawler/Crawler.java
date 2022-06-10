package ru.malygin.crawler.crawler;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.malygin.crawler.model.Task;
import ru.malygin.crawler.model.entity.Page;
import ru.malygin.crawler.model.entity.Statistic;
import ru.malygin.crawler.service.PageService;
import ru.malygin.crawler.service.StatisticService;
import ru.malygin.helper.enums.TaskState;
import ru.malygin.helper.model.TaskCallback;
import ru.malygin.helper.senders.TaskCallbackSender;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

/**
 * The class that performs the distribution of tasks for crawling and downloading sites, parsing content and saving the site content to the database.
 * A class object can be created using {@link Builder}
 *
 * @author Nikolay Malygin
 * @version 1.0
 * @see PageFetcher
 * @see LinkParser
 * @see PageSaver
 */

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class Crawler implements Runnable {

    // init in builder
    private final StatisticService statisticService;
    private final PageService pageService;
    private final TaskCallbackSender taskCallbackSender;
    // init
    private final Queue<Page> pagesQueue = new ConcurrentLinkedQueue<>();
    private final Queue<String> linksQueue = new ConcurrentLinkedQueue<>();
    private final Queue<Page> saveQueue = new ConcurrentLinkedQueue<>();
    // components
    private PageFetcher pageFetcher;
    private PageSaver pageSaver;
    private LinkParser linkParser;
    // init in start
    private TaskState taskState = TaskState.CREATE;
    private Task task;
    private Map<Task, Crawler> currentRunningTasks;
    private Statistic statistic;
    private String sitePath;

    private static void timeOut100ms() {
        try {
            TimeUnit.MILLISECONDS.sleep(100L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * The method initializes all child services, and starts the algorithm to scan site, the parsing and saving site
     *
     * @param task                the task for this service {@link Task}
     * @param currentRunningTasks the map of all tasks for this service
     * @see PageFetcher
     * @see LinkParser
     * @see PageSaver
     */
    public void start(Task task,
                      Map<Task, Crawler> currentRunningTasks) {
        //  @formatter:off
        this.task = task;
        Long siteId = task.getSiteId();
        Long appUserId = task.getAppUserId();
        this.sitePath = task.getPath();
        this.currentRunningTasks = currentRunningTasks;

        this.pageFetcher = new PageFetcher(linksQueue,
                                           pagesQueue,
                                           SiteFetcher.getFromTask(task));

        this.linkParser = new LinkParser(linksQueue,
                                         pagesQueue,
                                         saveQueue);

        this.pageSaver = new PageSaver(pageService,
                                       saveQueue,
                                       siteId,
                                       appUserId);

        this.statistic = new Statistic();
        this.statistic.setSiteId(siteId);
        this.statistic.setAppUserId(appUserId);

        Thread thread = new Thread(this);
        thread.setName("Crawler-" + sitePath + "-" + appUserId);
        thread.start();
        //  @formatter:on
    }

    /**
     * The method interrupts the algorithm
     */
    public void stop() {
        changeTaskState(TaskState.INTERRUPT);
    }

    @Override
    public void run() {
        statistic.setStartTime(LocalDateTime.now());

        changeTaskState(TaskState.START);

        linksQueue.add(sitePath);

        pageFetcher.start();
        linkParser.start();
        pageSaver.start();

        watchDogLoop();
    }

    private void watchDogLoop() {
        while (true) {
            timeOut100ms();
            checkNormalComplete();

            if (!taskState.equals(TaskState.START)) {
                pageFetcher.stop();
                linkParser.stop();
                pageSaver.stop();
                saveAndPublishFinalStat();
                break;
            }
        }
        currentRunningTasks.remove(task);
    }

    private void checkNormalComplete() {
        //  @formatter:off
        if (pagesQueue.isEmpty()
                && linksQueue.isEmpty()
                && saveQueue.isEmpty()
                && !pageFetcher.getServe()
                && !linkParser.getServe()
                && !pageSaver.getServe()) {
            changeTaskState(TaskState.COMPLETE);
        }
        //  @formatter:on
    }

    private void saveAndPublishFinalStat() {
        setActualStatistics();
        statisticService
                .save(statistic)
                .subscribe();
    }

    private void setActualStatistics() {
        statistic.setEndTime(!taskState.equals(TaskState.START) ? LocalDateTime.now() : null);
        statistic.setSavedPages(pageSaver.getCompleteTasks());
        statistic.setFetchPages(pageFetcher.getCompleteTasks());
        statistic.setErrors(pageSaver.getErrorsCount());
        statistic.setLinksCount(linkParser.getCompleteTasks());
    }

    private void changeTaskState(TaskState state) {
        this.taskState = state;
        setActualStatistics();
        TaskCallback callback = new TaskCallback(task.getId(), taskState, statistic.getStartTime(),
                                                 statistic.getEndTime());
        taskCallbackSender.send(callback);
    }

    @Component
    @RequiredArgsConstructor
    public static final class Builder {

        private final StatisticService statisticService;
        private final PageService pageService;
        private final TaskCallbackSender taskCallbackSender;

        public Crawler build() {
            return new Crawler(statisticService, pageService, taskCallbackSender);
        }
    }
}
