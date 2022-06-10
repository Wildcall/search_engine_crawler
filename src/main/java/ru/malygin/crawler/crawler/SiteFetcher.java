package ru.malygin.crawler.crawler;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.util.retry.Retry;
import ru.malygin.crawler.model.Task;
import ru.malygin.crawler.model.entity.Page;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Builder(access = AccessLevel.PRIVATE)
public class SiteFetcher {

    private final String userAgent;
    private final String referrer;
    private final int timeOutTime;
    private final int reconnect;
    @Getter
    private final int delayTime;

    SiteFetcher(String userAgent,
                String referrer,
                int timeOutTime,
                int reconnect,
                int delayTime) {
        this.userAgent = userAgent;
        this.referrer = referrer;
        this.timeOutTime = timeOutTime;


        this.reconnect = reconnect;
        this.delayTime = delayTime;
    }

    public static SiteFetcher getFromTask(Task task) {
        return SiteFetcher
                .builder()
                .userAgent(task.getUserAgent())
                .referrer(task.getReferrer())
                .timeOutTime(task.getTimeOutInMs())
                .reconnect(task.getReconnect())
                .delayTime(task.getDelayInMs())
                .build();
    }

    public static Set<String> getLinks(Page page) {
        Set<String> linkSet = new HashSet<>();
        Document document = Jsoup.parse(page.getContent());
        Elements links = document.select("a[href]");
        links.forEach(link -> {
            String tmp = parseLink(link.attr("href"), page.getPath());
            if (tmp != null) linkSet.add(tmp);
        });
        return linkSet;
    }

    private static String parseLink(String link,
                                    String parentPath) {
        if (link.contains("#") || link.equals("/")) return null;
        try {
            URL parentUrl = new URL(parentPath);
            URL url = link.startsWith("/") ? new URL(
                    parentUrl.getProtocol() + "://" + parentUrl.getHost() + link.toLowerCase(Locale.ROOT)) : new URL(
                    link);

            if (url.getPath() != null && url
                    .getHost()
                    .startsWith(parentUrl.getHost()) && url
                    .getPath()
                    .startsWith(parentUrl.getPath()) && !url
                    .getPath()
                    .equals(parentUrl.getPath())) return url.toString();
        } catch (MalformedURLException e) {
            return null;
        }
        return null;
    }

    public Page fetchPath(String path) {
        Page page = new Page();
        page.setPath(path);

        try {
            page.setContent(getWebClient()
                                    .get()
                                    .uri(path)
                                    .exchangeToMono(response -> {
                                        int code = -1;
                                        Mono<String> body = Mono.just("");
                                        if (response
                                                .statusCode()
                                                .is2xxSuccessful()) {
                                            code = response
                                                    .statusCode()
                                                    .value();
                                            body = response.bodyToMono(String.class);
                                        } else if (response
                                                .statusCode()
                                                .is4xxClientError() || response
                                                .statusCode()
                                                .is5xxServerError()) {
                                            code = response
                                                    .statusCode()
                                                    .value();
                                            body = response.bodyToMono(String.class);
                                        }
                                        page.setCode(code);
                                        return body;
                                    })
                                    .retryWhen(Retry.fixedDelay(reconnect, Duration.ofMillis(delayTime)))
                                    .block());
        } catch (Exception e) {
            page.setCode(-1);
            page.setContent(e.getMessage());
        }

        return page;
    }

    private WebClient getWebClient() {
        HttpClient httpClient = HttpClient
                .create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, timeOutTime)
                .responseTimeout(Duration.ofMillis(timeOutTime))
                .followRedirect(true)
                .doOnConnected(conn -> conn
                        .addHandlerLast(new ReadTimeoutHandler(timeOutTime, TimeUnit.MILLISECONDS))
                        .addHandlerLast(new WriteTimeoutHandler(timeOutTime, TimeUnit.MILLISECONDS)));

        return WebClient
                .builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .defaultHeader("User-Agent", userAgent)
                .defaultHeader("Referrer", referrer)
                .build();
    }
}
