package cwh.order.customer.service;

import cwh.order.customer.util.HandleException;

/**
 * Created by Administrator on 2018/11/18 0018.
 */
public interface MainService {

    String getToken(String code) throws HandleException;

    void sendPhoneKey(String openid, String phoneNumber) throws HandleException;

    void bindingPhone(String openid, String phoneNumber, String verifyCode) throws HandleException;

    String getBindPhone(String openid);
}
