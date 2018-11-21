package cwh.order.customer.service;

import cwh.order.customer.model.FoodOrder;
import cwh.order.customer.util.HandleException;

import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2018/11/18 0018.
 */
public interface FoodService {

    Map getFoods(String openid, String store_id, String table_id) throws HandleException;

    void order(String openid, String foods, String store_id, String table_id, String order, String phone, String message) throws HandleException;

    List<FoodOrder> getOrders(String openid);
}
