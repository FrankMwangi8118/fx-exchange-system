package com.AnvilShieldGroup.main_service.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Configuration
public class WebClientConfig {
    @Value("${RATE-SERVICE_BASE_URL}")
    private String baseUrl;

    @Value("${webclient.connection.timeout:5000}")
    private Integer connectionTimeoutMillis;

    @Value("${webclient.response.timeout:10}")
    private Long responseTimeoutSeconds;

    @Value("${webclient.read.timeout:5}")
    private Long readTimeoutSeconds;

    @Value("${webclient.write.timeout:5}")
    private Long writeTimeoutSeconds;

    @Value("${webclient.pool.max-connections:50}")
    private Integer maxConnections;

    @Value("${webclient.pool.acquire-timeout:2000}")
    private Integer pendingAcquireTimeoutMillis;

    @Bean
    public WebClient webClient() {

        ConnectionProvider connectionProvider = ConnectionProvider.builder("custom-connection-pool")
                .maxConnections(maxConnections)
                .pendingAcquireTimeout(Duration.ofMillis(pendingAcquireTimeoutMillis))
                .build();

        HttpClient httpClient = HttpClient.create(connectionProvider)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectionTimeoutMillis)
                .responseTimeout(Duration.ofSeconds(responseTimeoutSeconds))
                .doOnConnected(conn -> conn
                        .addHandlerLast(new ReadTimeoutHandler(readTimeoutSeconds, TimeUnit.SECONDS))
                        .addHandlerLast(new WriteTimeoutHandler(writeTimeoutSeconds, TimeUnit.SECONDS)));

        return WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("api-key", "frank")
                .defaultHeader("api-passphrase", "frank")
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }
}
