package cwh.order.customer.service.impl;

import cwh.order.customer.dao.FoodDao;
import cwh.order.customer.dao.StoreDao;
import cwh.order.customer.dao.TableDao;
import cwh.order.customer.model.Store;
import cwh.order.customer.model.Table;
import cwh.order.customer.service.FoodService;
import cwh.order.customer.util.HandleException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2018/11/18 0018.
 */
@Service
public class FoodServiceImpl implements FoodService {

    @Resource
    private FoodDao foodDao;
    @Resource
    private StoreDao storeDao;
    @Resource
    private TableDao tableDao;

    @Override
    @Transactional
    public Map getFoods(String openid, String store_id, String table_id) throws HandleException {
        Map<String, Object> map = new HashMap<>();
        if(store_id.equals("")){
            Table table = tableDao.query(Long.parseLong(table_id));
            if (table == null) {
                throw new HandleException("餐桌不存在");
            }
            store_id = table.getOpenid();
            map.put("tableName", table.getT_name());
        }
        Store store = storeDao.query(store_id);
        if(store == null){
            throw new HandleException("店铺不存在");
        }
        if (store.getBusiness() == 0) {
            throw new HandleException("店铺已打烊");
        }
        map.put("store", store);
        map.put("foods", foodDao.queryAll(store_id));
        return map;
    }
}
