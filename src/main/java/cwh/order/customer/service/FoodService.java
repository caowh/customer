package cwh.order.customer.service;

import cwh.order.customer.model.FoodOrder;
import cwh.order.customer.util.HandleException;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2018/11/18 0018.
 */
public interface FoodService {

    Map getFoods(String openid, String store_id, String table_id) throws HandleException;

    void order(String openid, String foods, String store_id, String table_id, String order, String phone, String message) throws HandleException;

    List<FoodOrder> getOrders(String openid, int status, int page, int count);

    FoodOrder getOrderDetail(String openid, long order_id) throws HandleException;

    void cancelOrder(String openid, long order_id, String reason) throws HandleException;

    void orderEvaluate(String openid, long order_id, int type, String message, String foods) throws HandleException;

    void uploadEvaluatePicture(String openid, long order_id, MultipartFile file) throws HandleException;
}
