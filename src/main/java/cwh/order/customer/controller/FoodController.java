package cwh.order.customer.controller;

import cwh.order.customer.service.FoodService;
import cwh.order.customer.util.Constant;
import cwh.order.customer.util.HandleException;
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
@RequestMapping("food")
public class FoodController {

    @Resource
    private FoodService foodService;

    @PostMapping("getFoods")
    public Map<String, Object> getFoods(HttpServletRequest request) {
        Map<String, Object> map = new HashMap<>();
        String openid = request.getAttribute("openid").toString();
        String store_id = getSafeParameter(request, "store_id");
        String table_id = getSafeParameter(request, "table_id");
        try {
            map.put("message", foodService.getFoods(openid, store_id, table_id));
            map.put("status", Constant.CODE_OK);
        } catch (HandleException e) {
            map.put("status", Constant.CODE_ERROR);
            map.put("error_message", e.getMessage());
        }

        return map;
    }
}
