package tech.yakov.AtemProxy.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import tech.yakov.AtemProxy.components.SocketTextHandler;
import tech.yakov.AtemProxy.service.TallyConstellationService;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
    private final TallyConstellationService tallyConstellationService;

    public WebSocketConfig(TallyConstellationService tallyConstellationService){
        this.tallyConstellationService = tallyConstellationService;
    }

    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(new SocketTextHandler(tallyConstellationService), "/web-socket").setAllowedOrigins("*");
    }
}
