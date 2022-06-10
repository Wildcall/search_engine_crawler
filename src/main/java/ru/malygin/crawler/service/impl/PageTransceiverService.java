package ru.malygin.crawler.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import reactor.core.scheduler.Schedulers;
import ru.malygin.crawler.service.PageService;
import ru.malygin.helper.model.requests.DataRequest;
import ru.malygin.helper.senders.LogSender;
import ru.malygin.helper.service.DataTransceiver;
import ru.malygin.helper.service.DefaultQueueDeclareService;

@Slf4j
@RequiredArgsConstructor
@Service
public class PageTransceiverService implements DataTransceiver {

    private final PageService pageService;
    private final LogSender logSender;
    private final RabbitTemplate rabbitTemplate;
    private final DefaultQueueDeclareService declareService;

    @Override
    @RabbitListener(queues = "#{properties.getCommon().getRequest().getPageRoute()}")
    public Long dataRequestListen(DataRequest dataRequest) {
        DataTransceiver.super.dataRequestReceiveLog(logSender, dataRequest);
        return pageService
                .getCountBySiteIdAndAppUserId(dataRequest.getSiteId(), dataRequest.getAppUserId())
                .doOnSuccess(pageCount -> send(pageCount, dataRequest))
                .block();
    }

    @Override
    public void send(Long itemCount,
                     DataRequest dataRequest) {
        declareService.createQueue(dataRequest.getDataQueue(), null);
        pageService
                .findAllBySiteIdAndAppUserId(dataRequest.getSiteId(), dataRequest.getAppUserId())
                .subscribeOn(Schedulers.boundedElastic())
                .doOnNext(page -> rabbitTemplate.convertAndSend(dataRequest.getDataQueue(), page))
                .doOnComplete(() -> {
                    DataTransceiver.super.dataSendLog(logSender, dataRequest, itemCount);
                    declareService.removeQueue(dataRequest.getDataQueue());
                })
                .subscribe();
    }
}
