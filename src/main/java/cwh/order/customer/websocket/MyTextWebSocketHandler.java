package cwh.order.customer.websocket;

import com.alibaba.fastjson.JSON;
import cwh.order.customer.util.Constant;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Administrator on 2018/11/18 0018.
 */
@Component
public class MyTextWebSocketHandler extends TextWebSocketHandler {

    private static final Map<String, WebSocketSession> sessionMap = new ConcurrentHashMap<>();
    @Resource
    private RedisTemplate<String, String> redisTemplate;

    //处理文本消息
    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) {

    }

    //连接建立后处理
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        super.afterConnectionEstablished(session);
        String openid = session.getAttributes().get("openid").toString();
        String table_id = session.getHandshakeHeaders().get("table_id").get(0);
        String nickName = session.getHandshakeHeaders().get("nickName").get(0);
        String userKey = table_id + Constant.separator + "user";
        sessionMap.put(openid, session);
        Set<String> users = redisTemplate.opsForSet().members(userKey);
        if (users != null && users.size() > 0) {
            TextMessage textMessage = new TextMessage(nickName + " 加入点餐");
            for (String user : users) {
                WebSocketSession webSocketSession = sessionMap.get(user);
                if (webSocketSession != null) {
                    webSocketSession.sendMessage(textMessage);
                }
            }
        }
        redisTemplate.opsForSet().add(userKey, openid);
        List<String> list = redisTemplate.opsForList().range(table_id + Constant.separator + "food", 0, -1);
        if (list == null || list.size() == 0) {
            session.sendMessage(new TextMessage("0"));
        } else {
            session.sendMessage(new TextMessage(JSON.toJSONString(list)));
        }
    }

    //连接关闭后处理
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String openid = session.getAttributes().get("openid").toString();
        sessionMap.remove(openid);
        String table_id = session.getAttributes().get("table_id").toString();
        redisTemplate.opsForSet().remove(table_id + Constant.separator + "user", openid);
        super.afterConnectionClosed(session, status);
    }

    //抛出异常时处理
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        super.handleTransportError(session, exception);
        if (session.isOpen())
            session.close();

    }


    //是否支持局部消息
    @Override
    public boolean supportsPartialMessages() {
        return false;
    }

}