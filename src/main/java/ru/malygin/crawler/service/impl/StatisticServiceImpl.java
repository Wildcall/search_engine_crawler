package ru.malygin.crawler.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import ru.malygin.crawler.model.entity.Statistic;
import ru.malygin.crawler.repository.StatisticRepository;
import ru.malygin.crawler.service.StatisticService;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional
public class StatisticServiceImpl implements StatisticService {

    private final StatisticRepository statisticRepository;

    @Override
    public Mono<Statistic> save(Statistic statistic) {
        log.info("SAVE STAT / SiteId: {} / AppUserId: {}", statistic.getSiteId(), statistic.getAppUserId());
        return statisticRepository.save(statistic);
    }
}
