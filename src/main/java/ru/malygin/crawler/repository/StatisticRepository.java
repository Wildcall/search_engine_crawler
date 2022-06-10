package ru.malygin.crawler.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import ru.malygin.crawler.model.entity.Statistic;

public interface StatisticRepository extends ReactiveCrudRepository<Statistic, Long> {

    Flux<Statistic> findAllBySiteIdAndAppUserId(Long siteId,
                                                Long appUserId);

}
