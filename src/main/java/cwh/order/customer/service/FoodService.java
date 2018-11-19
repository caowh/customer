package cwh.order.customer.service;

import cwh.order.customer.util.HandleException;

import java.util.Map;

/**
 * Created by Administrator on 2018/11/18 0018.
 */
public interface FoodService {

    Map getFoods(String openid, String store_id, long table_id) throws HandleException;
}
