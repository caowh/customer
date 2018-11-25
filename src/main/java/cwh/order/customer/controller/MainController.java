package cwh.order.customer.controller;

import cwh.order.customer.service.MainService;
import cwh.order.customer.util.Constant;
import cwh.order.customer.util.HandleException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

import static cwh.order.customer.util.Constant.getSafeParameter;

/**
 * Created by Administrator on 2018/11/18 0018.
 */
@RestController
@RequestMapping("main")
public class MainController {

    @Resource
    private MainService mainService;

    @GetMapping("getToken")
    public Map<String, Object> getToken(HttpServletRequest request) {
        Map<String, Object> map = new HashMap<>();
        try {
            map.put("token", mainService.getToken(getSafeParameter(request, "code")));
            map.put("status", Constant.CODE_OK);
        } catch (HandleException e) {
            map.put("status", Constant.CODE_ERROR);
            map.put("error_message", e.getMessage());
        }
        return map;
    }

    @PostMapping("sendPhoneKey")
    public Map<String, Object> sendPhoneKey(HttpServletRequest request) {
        Map<String, Object> map = new HashMap<>();
        try {
            String openid = request.getAttribute("openid").toString();
            mainService.sendPhoneKey(openid, getSafeParameter(request, "phone"));
            map.put("status", Constant.CODE_OK);
        } catch (HandleException e) {
            map.put("status", Constant.CODE_ERROR);
            map.put("error_message", e.getMessage());
        }
        return map;
    }

    @PostMapping("bindPhone")
    public Map<String, Object> bindPhone(HttpServletRequest request) {
        Map<String, Object> map = new HashMap<>();
        try {
            String openid = request.getAttribute("openid").toString();
            mainService.bindingPhone(openid, getSafeParameter(request, "phone"), getSafeParameter(request, "code"));
            map.put("status", Constant.CODE_OK);
        } catch (HandleException e) {
            map.put("status", Constant.CODE_ERROR);
            map.put("error_message", e.getMessage());
        }
        return map;
    }

    @GetMapping("getBindPhone")
    public Map<String, Object> getBindPhone(HttpServletRequest request) {
        Map<String, Object> map = new HashMap<>();
        String openid = request.getAttribute("openid").toString();
        map.put("message", mainService.getBindPhone(openid));
        map.put("status", Constant.CODE_OK);
        return map;
    }
}
