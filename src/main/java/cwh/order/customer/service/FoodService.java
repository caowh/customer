package cwh.order.customer.service;

import cwh.order.customer.util.HandleException;

import java.util.List;

/**
 * Created by Administrator on 2018/11/18 0018.
 */
public interface FoodService {

    List getFoods(String openid, String store_id) throws HandleException;
}
