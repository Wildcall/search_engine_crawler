package ru.malygin.crawler.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.malygin.crawler.model.entity.Page;
import ru.malygin.crawler.repository.PageRepository;
import ru.malygin.crawler.service.PageService;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional
public class PageServiceImpl implements PageService {

    private final PageRepository pageRepository;

    @Override
    public Mono<Page> save(Page page) {
        if (page.hasRequiredField()) return pageRepository.save(page);
        return Mono.empty();
    }

    @Override
    public Flux<Page> findAllBySiteIdAndAppUserId(Long siteId,
                                                  Long appUserId) {
        return pageRepository.findAllBySiteIdAndAppUserId(siteId, appUserId);
    }

    @Override
    public Mono<Long> getCountBySiteIdAndAppUserId(Long siteId,
                                                   Long appUserId) {
        return pageRepository.countPagesBySiteIdAndAppUserId(siteId, appUserId);
    }

    @Override
    public Mono<Void> deleteAllBySiteIdAndAppUserId(Long siteId,
                                                    Long appUserId) {
        return pageRepository.deleteAllBySiteIdAndAppUserId(siteId, appUserId);
    }
}
