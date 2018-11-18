package cwh.order.customer.websocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

import java.util.Map;

/**
 * Created by Administrator on 2018/11/18 0018.
 */
@Component
public class MyHttpSessionHandshakeInterceptor extends HttpSessionHandshakeInterceptor {

    private static final Logger log = LoggerFactory.getLogger(MyHttpSessionHandshakeInterceptor.class);

    //握手前
    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler,
                                   Map<String, Object> attributes) throws Exception {
        log.trace("Before Handshake");
        if (request instanceof ServletServerHttpRequest) {
            ServletServerHttpRequest servletRequest = (ServletServerHttpRequest) request;
            Object openid = servletRequest.getServletRequest().getAttribute("openid");
            attributes.put("openid", openid);
        }
        return super.beforeHandshake(request, response, wsHandler, attributes);
    }

    //握手后
    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler,
                               Exception ex) {
        super.afterHandshake(request, response, wsHandler, ex);
    }
}
