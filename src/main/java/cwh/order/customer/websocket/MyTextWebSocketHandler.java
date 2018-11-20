package cwh.order.customer.websocket;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import cwh.order.customer.util.Constant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import javax.annotation.Resource;
import java.io.EOFException;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Created by Administrator on 2018/11/18 0018.
 */
@Component
public class MyTextWebSocketHandler extends TextWebSocketHandler {

    private static final Map<String, WebSocketSession> sessionMap = new ConcurrentHashMap<>();
    private static final Logger logger = LoggerFactory.getLogger(MyTextWebSocketHandler.class);
    @Resource
    private RedisTemplate<String, String> redisTemplate;

    //处理文本消息
    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
        String openid = session.getAttributes().get("openid").toString();
        String table_id = session.getAttributes().get("tableid").toString();
        String nickName = session.getAttributes().get("nickname").toString();
        logger.info("receive message,openid is {},table_id is {},message is {}", openid, table_id, message.getPayload());
        JSONObject jsonObject = JSONObject.parseObject(message.getPayload());
        int type = jsonObject.getInteger("type");
        String food_key = table_id + Constant.separator + "food";
        String userKey = table_id + Constant.separator + "user";
        if (type == Constant.ORDER) {
            jsonObject.remove("type");
            long id = jsonObject.getLong("id");
            int change = jsonObject.getInteger("change");
            String uuid = UUID.randomUUID().toString();
            lock(table_id, uuid);
            String foods = redisTemplate.opsForValue().get(food_key);
            JSONArray list;
            if (foods == null) {
                list = new JSONArray();
                if (change > 0) {
                    list.add(jsonObject);
                }
            } else {
                list = JSONArray.parseArray(foods);
                Boolean find = false;
                for (Object str : list) {
                    JSONObject food = (JSONObject) str;
                    long food_id = food.getLong("id");
                    if (food_id == id) {
                        int count = food.getInteger("change") + change;
                        if (count == 0) {
                            list.remove(str);
                            find = true;
                            break;
                        } else {
                            food.put("change", count);
                            find = true;
                            break;
                        }
                    }
                }
                if (!find && change > 0) {
                    list.add(jsonObject);
                }
            }
            if (list.size() == 0) {
                redisTemplate.delete(food_key);
            } else {
                redisTemplate.opsForValue().set(food_key, list.toJSONString());
            }
            unLock(table_id, uuid);
            JSONObject jsonObject1 = JSONObject.parseObject(message.getPayload());
            jsonObject1.put("nickname", nickName);
            broadcastNotSelf(userKey, makeMessage(jsonObject1), openid);
            JSONObject jsonObject2 = new JSONObject();
            jsonObject2.put("type", Constant.ORDER_FOOD);
            if (list.size() == 0) {
                jsonObject2.put("foods", "0");
            } else {
                jsonObject2.put("foods", list);
            }
            broadcast(userKey, makeMessage(jsonObject2));
        } else if (type == Constant.CLEAN_FOOD) {
            redisTemplate.delete(food_key);
            jsonObject.put("name", nickName);
            broadcastNotSelf(userKey, makeMessage(jsonObject), openid);
        }
    }

    private TextMessage makeMessage(JSONObject jsonObject) {
        String message = jsonObject.toJSONString();
        logger.info("send message {}", message);
        return new TextMessage(message);
    }

    private void lock(String table_id, String uuid) {
        String lock_key = table_id + Constant.separator + "lock";
        long max_time = 500;
        long waitTime = 0;
        while (waitTime < max_time) {
            Boolean result = redisTemplate.opsForValue().setIfAbsent(lock_key, uuid, max_time, TimeUnit.MILLISECONDS);
            if (result != null && result) {
                break;
            } else {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                waitTime += 10;
            }
        }
        if (waitTime == max_time) {
            redisTemplate.opsForValue().set(lock_key, uuid, max_time, TimeUnit.MILLISECONDS);
        }
    }

    private void unLock(String table_id, String uuid) {
        String lock_key = table_id + Constant.separator + "lock";
        String result = redisTemplate.opsForValue().get(lock_key);
        if (result != null && result.equals(uuid)) {
            redisTemplate.delete(lock_key);
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
        String table_id = session.getAttributes().get("tableid").toString();
        String nickName = session.getAttributes().get("nickname").toString();
        logger.info("connection opened,openid is {},table_id is {},nickName is {}", openid, table_id, nickName);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type", Constant.ADD_ORDER);
        jsonObject.put("name", nickName);
        String userKey = table_id + Constant.separator + "user";
        broadcast(userKey, makeMessage(jsonObject));
        sessionMap.put(openid, session);
        redisTemplate.opsForSet().add(userKey, openid);
        String foods = redisTemplate.opsForValue().get(table_id + Constant.separator + "food");
        JSONObject jsonObject1 = new JSONObject();
        jsonObject1.put("type", Constant.FIRST_FOOD);
        if (foods == null) {
            jsonObject1.put("foods", "0");
        } else {
            jsonObject1.put("foods", JSONArray.parseArray(foods));
        }
        session.sendMessage(makeMessage(jsonObject1));
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
        String table_id = session.getAttributes().get("tableid").toString();
        String nickName = session.getAttributes().get("nickname").toString();
        sessionMap.remove(openid);
        redisTemplate.opsForSet().remove(table_id + Constant.separator + "user", openid);
        logger.info("connection closed,openid is {},table_id is {},nickName is {}", openid, table_id, nickName);
        super.afterConnectionClosed(session, status);
    }

    //抛出异常时处理
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        super.handleTransportError(session, exception);
        String openid = session.getAttributes().get("openid").toString();
        String table_id = session.getAttributes().get("tableid").toString();
        String nickName = session.getAttributes().get("nickname").toString();
        logger.warn("handleTransportError,openid is {},table_id is {},nickName is {}", openid, table_id, nickName);
        if (session.isOpen())
            session.close();
        if (exception instanceof EOFException) {
            logger.warn("read time out,auto close the session!");
        } else {
            exception.printStackTrace();
        }

    }


    //是否支持局部消息
    @Override
    public boolean supportsPartialMessages() {
        return false;
    }

}