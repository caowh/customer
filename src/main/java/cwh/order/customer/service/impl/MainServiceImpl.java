package cwh.order.customer.service.impl;

import com.alibaba.fastjson.JSONObject;
import cwh.order.customer.service.MainService;
import cwh.order.customer.util.Constant;
import cwh.order.customer.util.HandleException;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.ResponseBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Created by Administrator on 2018/11/18 0018.
 */
@Service
public class MainServiceImpl implements MainService {

    private final OkHttpClient client = new OkHttpClient();
    private static final Logger logger = LoggerFactory.getLogger(MainServiceImpl.class);
    @Resource
    private RedisTemplate<String, String> redisTemplate;

    @Override
    public String getToken(String code) throws HandleException {
        String url = String.format("https://api.weixin.qq.com/sns/jscode2session?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code"
                , Constant.APPID, Constant.APPSECRET, code);
        Request get = new Request.Builder().url(url).build();
        try {
            ResponseBody responseBody = client.newCall(get).execute().body();
            JSONObject jsonObject = JSONObject.parseObject(responseBody.string());
            Object errcode = jsonObject.get("errcode");
            if (errcode == null) {
                String openid = jsonObject.getString("openid");
                String uuid = UUID.randomUUID().toString().toLowerCase();
                redisTemplate.opsForValue().set(uuid, openid, Constant.TIMEOUT, TimeUnit.MINUTES);
                return uuid;
            } else {
                logger.error("getToken get errcode,code is {},error is {}", code, jsonObject.getString("errmsg"));
                throw new HandleException(Constant.ERROR);
            }
        } catch (IOException e) {
            logger.error("getToken raise IOException,code is {},error is {}", code, e.getMessage());
            throw new HandleException(Constant.ERROR);
        }
    }

}
