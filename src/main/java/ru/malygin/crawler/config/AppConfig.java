package ru.malygin.crawler.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.malygin.crawler.model.Task;
import ru.malygin.crawler.service.impl.CrawlerService;
import ru.malygin.helper.config.SearchEngineProperties;
import ru.malygin.helper.model.requests.DataRequest;
import ru.malygin.helper.senders.LogSender;
import ru.malygin.helper.service.DefaultQueueDeclareService;
import ru.malygin.helper.service.DefaultTaskReceiver;
import ru.malygin.helper.service.TaskReceiver;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class AppConfig {

    @Bean
    protected Map<String, Class<?>> idClassMap() {
        Map<String, Class<?>> map = new HashMap<>();
        map.put("NodeTask", Task.class);
        map.put("DataRequest", DataRequest.class);
        log.info("[o] Configurate idClassMap in application");
        return map;
    }

    @Bean
    public boolean declareQueue(DefaultQueueDeclareService defaultQueueDeclareService,
                                SearchEngineProperties properties) {
        SearchEngineProperties.Common.Request requestProp = properties
                .getCommon()
                .getRequest();
        SearchEngineProperties.Common.Task taskProp = properties
                .getCommon()
                .getTask();
        defaultQueueDeclareService.createQueue(requestProp.getPageRoute(), requestProp.getExchange());
        defaultQueueDeclareService.createQueue(taskProp.getRoute(), taskProp.getExchange());
        return true;
    }

    @Bean
    public TaskReceiver<Task> taskReceiver(LogSender logSender,
                                           CrawlerService crawlerService) {
        log.info("[o] Create TaskReceiver in application");
        return new DefaultTaskReceiver<>(logSender, crawlerService);
    }
}
