package ru.malygin.crawler.service;

import reactor.core.publisher.Mono;
import ru.malygin.crawler.model.entity.Statistic;

public interface StatisticService {
    Mono<Statistic> save(Statistic statistic);
}
