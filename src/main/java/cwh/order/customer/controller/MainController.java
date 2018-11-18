package cwh.order.customer.controller;

import cwh.order.customer.service.MainService;
import cwh.order.customer.util.Constant;
import cwh.order.customer.util.HandleException;
import org.springframework.web.bind.annotation.GetMapping;
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
}
