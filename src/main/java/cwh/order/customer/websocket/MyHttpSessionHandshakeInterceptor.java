package cwh.order.customer.websocket;

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

    //握手前
    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler,
                                   Map<String, Object> attributes) throws Exception {
        if (request instanceof ServletServerHttpRequest) {
            ServletServerHttpRequest servletRequest = (ServletServerHttpRequest) request;
            Object openid = servletRequest.getServletRequest().getAttribute("openid");
            String tableid = servletRequest.getServletRequest().getParameter("tableid");
            String nickname = servletRequest.getServletRequest().getParameter("nickname");
            attributes.put("openid", openid);
            attributes.put("tableid", tableid);
            attributes.put("nickname", nickname);
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
