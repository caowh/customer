package cwh.order.customer.websocket;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import javax.annotation.Resource;

/**
 * Created by Administrator on 2018/11/18 0018.
 */
@Configuration
@EnableWebSocket
public class MyWebSocketConfig implements WebSocketConfigurer {

    @Resource
    private MyTextWebSocketHandler handler;
    @Resource
    private MyHttpSessionHandshakeInterceptor interceptor;

    public void registerWebSocketHandlers(WebSocketHandlerRegistry webSocketHandlerRegistry) {
        webSocketHandlerRegistry
                .addHandler(handler, "/ws")
                .addInterceptors(interceptor)
                .setAllowedOrigins("*");
    }

}
