package cwh.order.customer.controller;

import cwh.order.customer.service.FoodService;
import cwh.order.customer.util.Constant;
import cwh.order.customer.util.HandleException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

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

    @PostMapping("order")
    public Map<String, Object> order(HttpServletRequest request) {
        Map<String, Object> map = new HashMap<>();
        String openid = request.getAttribute("openid").toString();
        String store_id = getSafeParameter(request, "store_id");
        String table_id = getSafeParameter(request, "table_id");
        String order = getSafeParameter(request, "order");
        String phone = getSafeParameter(request, "phone");
        String message = getSafeParameter(request, "message");
        String foods = request.getParameter("foods").replaceAll("'", "’").trim();
        try {
            foodService.order(openid, foods, store_id, table_id, order, phone, message);
            map.put("status", Constant.CODE_OK);
        } catch (HandleException e) {
            map.put("status", Constant.CODE_ERROR);
            map.put("error_message", e.getMessage());
        }
        return map;
    }

    @PostMapping("getOrders")
    public Map<String, Object> getOrders(HttpServletRequest request) {
        Map<String, Object> map = new HashMap<>();
        String openid = request.getAttribute("openid").toString();
        int status = Integer.parseInt(getSafeParameter(request, "status"));
        int page = Integer.parseInt(getSafeParameter(request, "page"));
        int count = Integer.parseInt(getSafeParameter(request, "count"));
        map.put("message", foodService.getOrders(openid, status, page, count));
        map.put("status", Constant.CODE_OK);
        return map;
    }

    @PostMapping("getOrderDetail")
    public Map<String, Object> getOrderDetail(HttpServletRequest request) {
        Map<String, Object> map = new HashMap<>();
        String openid = request.getAttribute("openid").toString();
        long id = Long.parseLong(getSafeParameter(request, "id"));
        try {
            map.put("message", foodService.getOrderDetail(openid, id));
            map.put("status", Constant.CODE_OK);
        } catch (HandleException e) {
            map.put("status", Constant.CODE_ERROR);
            map.put("error_message", e.getMessage());
        }
        return map;
    }

    @PostMapping("cancelOrder")
    public Map<String, Object> cancelOrder(HttpServletRequest request) {
        Map<String, Object> map = new HashMap<>();
        String openid = request.getAttribute("openid").toString();
        long id = Long.parseLong(getSafeParameter(request, "id"));
        String reason = getSafeParameter(request, "reason");
        try {
            foodService.cancelOrder(openid, id, reason);
            map.put("status", Constant.CODE_OK);
        } catch (HandleException e) {
            map.put("status", Constant.CODE_ERROR);
            map.put("error_message", e.getMessage());
        }
        return map;
    }

    @PostMapping("orderEvaluate")
    public Map<String, Object> orderEvaluate(HttpServletRequest request) {
        Map<String, Object> map = new HashMap<>();
        String openid = request.getAttribute("openid").toString();
        long id = Long.parseLong(getSafeParameter(request, "id"));
        int type = Integer.parseInt(getSafeParameter(request, "type"));
        String message = getSafeParameter(request, "message");
        String foods = getSafeParameter(request, "foods");
        try {
            foodService.orderEvaluate(openid, id, type, message, foods);
            map.put("status", Constant.CODE_OK);
        } catch (HandleException e) {
            map.put("status", Constant.CODE_ERROR);
            map.put("error_message", e.getMessage());
        }
        return map;
    }

    @PostMapping("uploadEvaluatePicture")
    public Map<String, Object> uploadEvaluatePicture(HttpServletRequest request) {
        Map<String, Object> map = new HashMap<>();
        String openid = request.getAttribute("openid").toString();
        long id = Long.parseLong(getSafeParameter(request, "id"));
        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
        MultipartFile file = multipartRequest.getFile("picture");
        try {
            foodService.uploadEvaluatePicture(openid, id, file);
            map.put("status", Constant.CODE_OK);
        } catch (HandleException e) {
            map.put("status", Constant.CODE_ERROR);
            map.put("error_message", e.getMessage());
        }
        return map;
    }

}
