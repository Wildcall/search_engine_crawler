package ru.malygin.crawler.service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.malygin.crawler.model.entity.Page;

public interface PageService {
    Mono<Page> save(Page page);

    Flux<Page> findAllBySiteIdAndAppUserId(Long siteId,
                                           Long appUserId);

    Mono<Long> getCountBySiteIdAndAppUserId(Long siteId,
                                            Long appUserId);

    Mono<Void> deleteAllBySiteIdAndAppUserId(Long siteId,
                                             Long appUserId);
}
