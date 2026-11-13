package com.airhive.backend.config;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import com.airhive.backend.security.WebSocketAuthInterceptor;
import com.airhive.backend.security.WebSocketOutboundAuthInterceptor;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Value("${airhive.websocket.allowed-origins:http://localhost:*,http://127.0.0.1:*}")
    private String allowedOrigins;

    private final WebSocketAuthInterceptor webSocketAuthInterceptor;
    private final WebSocketOutboundAuthInterceptor webSocketOutboundAuthInterceptor;

    public WebSocketConfig(
            WebSocketAuthInterceptor webSocketAuthInterceptor,
            WebSocketOutboundAuthInterceptor webSocketOutboundAuthInterceptor) {

        this.webSocketAuthInterceptor = webSocketAuthInterceptor;
        this.webSocketOutboundAuthInterceptor =
                webSocketOutboundAuthInterceptor;
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic");
        registry.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void configureClientInboundChannel(
            ChannelRegistration registration) {

        registration.interceptors(webSocketAuthInterceptor);
    }

    @Override
    public void configureClientOutboundChannel(
            ChannelRegistration registration) {

        registration.interceptors(webSocketOutboundAuthInterceptor);
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns(
                        Arrays.stream(allowedOrigins.split(","))
                                .map(String::trim)
                                .filter(origin -> !origin.isEmpty())
                                .toArray(String[]::new)
                );
    }
}
