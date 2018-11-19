package cwh.order.customer.websocket;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import cwh.order.customer.util.Constant;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import javax.annotation.Resource;
import java.io.IOException;
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
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
        String table_id = session.getHandshakeHeaders().get("table_id").get(0);
        JSONObject jsonObject = JSONObject.parseObject(message.getPayload());
        int type = jsonObject.getInteger("type");
        if (type == Constant.SocketFoodChange) {
            long id = jsonObject.getLong("id");
            int change = jsonObject.getInteger("type");
            String food_key = table_id + Constant.separator + "food";
            List<String> list = redisTemplate.opsForList().range(food_key, 0, -1);
            if (list == null || list.size() == 0) {
                jsonObject.remove("id");
                redisTemplate.opsForList().leftPush(food_key, jsonObject.toJSONString());
            } else {
                String replace = null;
                int index = 0, result = 0;
                for (String str : list) {
                    JSONObject food = JSONObject.parseObject(str);
                    long food_id = jsonObject.getLong("id");
                    if (food_id == id) {
                        int count = jsonObject.getInteger("count") + change;
                        if (count == 0) {
                            replace = str;
                            result = 1;
                            break;
                        } else {
                            food.put("count", count);
                            replace = food.toJSONString();
                            result = 2;
                            break;
                        }
                    }
                    index++;
                }
                if (result == 0) {
                    redisTemplate.opsForList().leftPush(food_key, jsonObject.toJSONString());
                } else if (result == 1) {
                    redisTemplate.opsForList().remove(food_key, 0, replace);
                } else {
                    redisTemplate.opsForList().set(food_key, index, replace);
                }
            }
            String userKey = table_id + Constant.separator + "user";
            broadcastNotSelf(userKey, message, session.getAttributes().get("openid").toString());
            TextMessage textMessage = new TextMessage(JSON.toJSONString(redisTemplate.opsForList().range(food_key, 0, -1)));
            broadcast(userKey, textMessage);
        }
    }

    private void broadcastNotSelf(String userKey, TextMessage message, String openid) throws IOException {
        Set<String> users = redisTemplate.opsForSet().members(userKey);
        if (users != null && users.size() > 0) {
            for (String user : users) {
                WebSocketSession webSocketSession = sessionMap.get(user);
                if (webSocketSession != null && !openid.equals(user)) {
                    webSocketSession.sendMessage(message);
                }
            }
        }
    }

    //连接建立后处理
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        super.afterConnectionEstablished(session);
        String openid = session.getAttributes().get("openid").toString();
        String table_id = session.getHandshakeHeaders().get("table_id").get(0);
        String nickName = session.getHandshakeHeaders().get("nickName").get(0);
        sessionMap.put(openid, session);
        TextMessage textMessage = new TextMessage(nickName + " 加入点餐");
        String userKey = table_id + Constant.separator + "user";
        broadcast(userKey, textMessage);
        redisTemplate.opsForSet().add(userKey, openid);
        List<String> list = redisTemplate.opsForList().range(table_id + Constant.separator + "food", 0, -1);
        if (list == null || list.size() == 0) {
            session.sendMessage(new TextMessage("0"));
        } else {
            session.sendMessage(new TextMessage(JSON.toJSONString(list)));
        }
    }

    private void broadcast(String userKey, TextMessage textMessage) throws IOException {
        Set<String> users = redisTemplate.opsForSet().members(userKey);
        if (users != null && users.size() > 0) {
            for (String user : users) {
                WebSocketSession webSocketSession = sessionMap.get(user);
                if (webSocketSession != null) {
                    webSocketSession.sendMessage(textMessage);
                }
            }
        }
    }

    //连接关闭后处理
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String openid = session.getAttributes().get("openid").toString();
        sessionMap.remove(openid);
        String table_id = session.getHandshakeHeaders().get("table_id").get(0);
        redisTemplate.opsForSet().remove(table_id + Constant.separator + "user", openid);
        super.afterConnectionClosed(session, status);
    }

    //抛出异常时处理
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        super.handleTransportError(session, exception);
        if (session.isOpen())
            session.close();
        exception.printStackTrace();
    }


    //是否支持局部消息
    @Override
    public boolean supportsPartialMessages() {
        return false;
    }

}