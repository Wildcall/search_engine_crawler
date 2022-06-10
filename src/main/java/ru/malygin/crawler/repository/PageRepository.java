package ru.malygin.crawler.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.malygin.crawler.model.entity.Page;

public interface PageRepository extends ReactiveCrudRepository<Page, Long> {

    Flux<Page> findAllBySiteIdAndAppUserId(Long siteId,
                                           Long appUserId);

    Mono<Void> deleteAllBySiteIdAndAppUserId(Long siteId,
                                             Long appUserId);

    Mono<Long> countPagesBySiteIdAndAppUserId(Long siteId,
                                              Long appUserId);
}
