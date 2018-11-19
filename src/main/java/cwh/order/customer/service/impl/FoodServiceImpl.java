package cwh.order.customer.service.impl;

import cwh.order.customer.dao.FoodDao;
import cwh.order.customer.dao.StoreDao;
import cwh.order.customer.dao.TableDao;
import cwh.order.customer.model.Store;
import cwh.order.customer.model.Table;
import cwh.order.customer.service.FoodService;
import cwh.order.customer.util.HandleException;
import org.springframework.stereotype.Service;

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
    public Map getFoods(String openid, String store_id, long table_id) throws HandleException {
        Map<String, Object> map = new HashMap<>();
        Store store = storeDao.query(store_id);
        if (store == null) {
            throw new HandleException("店铺不存在");
        }
        if (store.getBusiness() == 0) {
            throw new HandleException("店铺已打烊");
        }
        if (table_id != 0) {
            Table table = new Table();
            table.setId(table_id);
            table.setOpenid(store_id);
            String tableName = tableDao.queryName(table);
            if (tableName == null) {
                throw new HandleException("餐桌不存在");
            }
            map.put("tableName", tableName);
        }
        map.put("store", store);
        map.put("foods", foodDao.queryAll(store_id));
        return map;
    }
}
