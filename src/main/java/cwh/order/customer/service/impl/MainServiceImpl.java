package cwh.order.customer.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsRequest;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsResponse;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.profile.IClientProfile;
import cwh.order.customer.dao.BuyUserDao;
import cwh.order.customer.model.BuyUser;
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
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Random;
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
    @Resource
    private BuyUserDao buyUserDao;

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

    @Override
    public void sendPhoneKey(String openid, String phoneNumber) throws HandleException {
        String phone = this.getBindPhone(openid);
        if (phone.equals(phoneNumber)) {
            throw new HandleException("手机号不能和变更前一致");
        }
        Object obj = redisTemplate.opsForValue().get(openid + Constant.separator + Constant.phoneKey);
        if (obj != null) {
            String[] strings = obj.toString().split(Constant.separator);
            if (phoneNumber.equals(strings[0])) {
                if (System.currentTimeMillis() - Long.parseLong(strings[2]) <= 60 * 1000) {
                    throw new HandleException("手机号码刚获取过验证码，请一分钟后再试");
                }
            } else {
                if (System.currentTimeMillis() - Long.parseLong(strings[2]) <= 10 * 1000) {
                    throw new HandleException("获取验证码过于频繁，请10S后再试");
                }
            }
        }
        //设置超时时间-可自行调整
        System.setProperty("sun.net.client.defaultConnectTimeout", "10000");
        System.setProperty("sun.net.client.defaultReadTimeout", "10000");
        //初始化ascClient需要的几个参数
        final String product = "Dysmsapi";//短信API产品名称（短信产品名固定，无需修改）
        final String domain = "dysmsapi.aliyuncs.com";//短信API产品域名（接口地址固定，无需修改）
        //替换成你的AK
        final String accessKeyId = "LTAIvSdYeu6DFyXK";//你的accessKeyId,参考本文档步骤2
        final String accessKeySecret = "Gb0XOQoH4TfmQ0kEyMtpPU6NTokEWq";//你的accessKeySecret，参考本文档步骤2
        //初始化ascClient,暂时不支持多region（请勿修改）
        IClientProfile profile = DefaultProfile.getProfile("cn-hangzhou", accessKeyId,
                accessKeySecret);
        try {
            DefaultProfile.addEndpoint("cn-hangzhou", "cn-hangzhou", product, domain);
        } catch (ClientException e) {
            logger.error("sendPhoneKey addEndpoint raise ClientException,openid is {},phoneNumber is {},error is {}", openid, phoneNumber, e.getErrMsg());
            throw new HandleException(Constant.ERROR);
        }
        IAcsClient acsClient = new DefaultAcsClient(profile);
        //组装请求对象
        SendSmsRequest request = new SendSmsRequest();
        //使用post提交
        request.setMethod(MethodType.POST);
        //必填:待发送手机号。支持以逗号分隔的形式进行批量调用，批量上限为1000个手机号码,批量调用相对于单条调用及时性稍有延迟,验证码类型的短信推荐使用单条调用的方式；发送国际/港澳台消息时，接收号码格式为国际区号+号码，如“85200000000”
        request.setPhoneNumbers(phoneNumber);
        //必填:短信签名-可在短信控制台中找到
        request.setSignName("点不点");
        //必填:短信模板-可在短信控制台中找到，发送国际/港澳台消息时，请使用国际/港澳台短信模版
        request.setTemplateCode("SMS_150172310");
        //可选:模板中的变量替换JSON串,如模板内容为"亲爱的${name},您的验证码为${code}"时,此处的值为
        //友情提示:如果JSON中需要带换行符,请参照标准的JSON协议对换行符的要求,比如短信内容中包含\r\n的情况在JSON中需要表示成\\r\\n,否则会导致JSON在服务端解析失败
        String verifyCode = String.valueOf(new Random().nextInt(899999) + 100000);//生成短信验证码
        request.setTemplateParam("{\"code\":\"" + verifyCode + "\"}");
        //可选-上行短信扩展码(扩展码字段控制在7位或以下，无特殊需求用户请忽略此字段)
        //request.setSmsUpExtendCode("90997");
        //可选:outId为提供给业务方扩展字段,最终在短信回执消息中将此值带回给调用者
        //request.setOutId("yourOutId");
        //请求失败这里会抛ClientException异常
        SendSmsResponse sendSmsResponse;
        try {
            sendSmsResponse = acsClient.getAcsResponse(request);
        } catch (ClientException e) {
            logger.error("sendPhoneKey getAcsResponse raise ClientException,openid is {},phoneNumber is {},error is {}", openid, phoneNumber, e.getErrMsg());
            throw new HandleException(Constant.ERROR);
        }
        String code = sendSmsResponse.getCode();
        if (code != null && code.equals("OK")) {
            redisTemplate.opsForValue().set(openid + Constant.separator + Constant.phoneKey,
                    phoneNumber + Constant.separator + verifyCode + Constant.separator + System.currentTimeMillis(),
                    5, TimeUnit.MINUTES);
        } else {
            logger.error("sendPhoneKey getCode not ok,openid is {},phoneNumber is {},code is {}", openid, phoneNumber, code);
            String error = Constant.ERROR;
            switch (code) {
                case "isv.BUSINESS_LIMIT_CONTROL":
                    error = "发送失败，过于频繁（5条/小时，10条/天）";
                    break;
                case "isv.BLACK_KEY_CONTROL_LIMIT":
                    error = "发送失败，黑名单管控";
                    break;
                case "isv.AMOUNT_NOT_ENOUGH":
                    error = "发送失败，账户余额不足";
                    break;
            }
            throw new HandleException(error);
        }
    }

    @Override
    public void bindingPhone(String openid, String phoneNumber, String verifyCode) throws HandleException {
        Object obj = redisTemplate.opsForValue().get(openid + Constant.separator + Constant.phoneKey);
        if (obj == null) {
            throw new HandleException("验证码无效");
        }
        String[] strings = obj.toString().split(Constant.separator);
        if (!phoneNumber.equals(strings[0])) {
            throw new HandleException("验证码无效");
        }
        if (!verifyCode.equals(strings[1])) {
            throw new HandleException("验证码错误");
        }
        this.bindingPhone(openid, phoneNumber);
    }

    @Transactional(rollbackFor = {Exception.class})
    private void bindingPhone(String openid, String phoneNumber) {
        String phone = buyUserDao.queryPhone(openid);
        BuyUser buyUser = new BuyUser();
        buyUser.setOpenid(openid);
        buyUser.setPhone(phoneNumber);
        if (phone == null) {
            buyUserDao.insert(buyUser);
        } else {
            buyUserDao.updatePhone(buyUser);
        }
    }

    @Override
    public String getBindPhone(String openid) {
        String phone = buyUserDao.queryPhone(openid);
        return phone == null ? "" : phone;
    }
}
