package cwh.order.customer.service.impl;

import cwh.order.customer.dao.FoodDao;
import cwh.order.customer.model.Food;
import cwh.order.customer.service.FoodService;
import cwh.order.customer.util.HandleException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2018/11/18 0018.
 */
@Service
public class FoodServiceImpl implements FoodService {

    @Resource
    private FoodDao foodDao;

    @Override
    public List getFoods(String openid, String store_id) throws HandleException {
        return foodDao.queryAll(store_id);
    }
}
