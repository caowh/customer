package cwh.order.customer.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import cwh.order.customer.dao.*;
import cwh.order.customer.model.FoodOrder;
import cwh.order.customer.model.FoodSale;
import cwh.order.customer.model.Store;
import cwh.order.customer.model.Table;
import cwh.order.customer.service.FoodService;
import cwh.order.customer.util.Constant;
import cwh.order.customer.util.HandleException;
import cwh.order.customer.util.IdWorker;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;

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
    @Resource
    private IdWorker idWorker;
    @Resource
    private FoodOrderDao foodOrderDao;
    @Resource
    private RedisTemplate<String, String> redisTemplate;
    @Resource
    private FoodSaleDao foodSaleDao;

    @Override
    @Transactional
    public Map getFoods(String openid, String store_id, String table_id) throws HandleException {
        Map<String, Object> map = new HashMap<>();
        if (store_id.equals("")) {
            Table table = tableDao.query(Long.parseLong(table_id));
            if (table == null) {
                throw new HandleException("餐桌不存在");
            }
            store_id = table.getOpenid();
            map.put("tableName", table.getT_name());
        }
        Store store = storeDao.query(store_id);
        if (store == null) {
            throw new HandleException("店铺不存在");
        }
        if (store.getBusiness() == 0) {
            throw new HandleException("店铺已打烊");
        }
        map.put("store", store);
        map.put("foods", foodDao.queryAll(store_id));
        return map;
    }

    @Override
    @Transactional
    public long order(String openid, String foods, String store_id, String table_id, String order, String phone, String message) throws HandleException {
        String table_name = null;
        if (store_id.equals("")) {
            Table table = tableDao.query(Long.parseLong(table_id));
            if (table == null) {
                throw new HandleException("餐桌不存在");
            }

            store_id = table.getOpenid();
            table_name = table.getT_name();
        }
        Store store = storeDao.query(store_id);
        if (store == null) {
            throw new HandleException("店铺不存在");
        }
        if (store.getBusiness() == 0) {
            throw new HandleException("店铺已打烊");
        }
        JSONArray list = JSONArray.parseArray(foods);
        if (list.size() == 0) {
            throw new HandleException("菜品不能为空");
        }
        BigDecimal total_price = new BigDecimal(0);
        List<FoodSale> foodSales = new ArrayList<>();
        long id = idWorker.nextId();
        for (Object aList : list) {
            JSONObject food = (JSONObject) aList;
            BigDecimal price = food.getBigDecimal("price");
            long food_id = food.getLong("id");
            BigDecimal t_price = foodDao.queryPrice(food_id);
            String name = food.getString("name");
            if (t_price == null) {
                throw new HandleException("菜品“" + name + "”已下架");
            }
            if (t_price.compareTo(price) != 0) {
                throw new HandleException("菜品“" + name + "”价格改变");
            }
            int count = food.getIntValue("change");
            if (count <= 0) {
                throw new HandleException("菜品“" + name + "”数量必须大于零");
            }
            total_price = total_price.add(t_price.multiply(new BigDecimal(count)));
            FoodSale foodSale = new FoodSale();
            foodSale.setOrder_id(id);
            foodSale.setFood_id(food_id);
            foodSale.setFood_count(count);
            foodSale.setFood_name(name);
            foodSale.setFood_price(price);
            foodSales.add(foodSale);
        }
        FoodOrder foodOrder = new FoodOrder();
        foodOrder.setStore_id(store_id);
        foodOrder.setOpenid(openid);
        foodOrder.setStore_name(store.getStore_name());
        foodOrder.setTotal_price(total_price);
        foodOrder.setCreate_time(new Date());
        foodOrder.setPhone(phone);
        foodOrder.setMessage(message);
        foodOrder.setId(id);
        if (!table_id.equals("")) {
            String uniqueId = String.valueOf(id);
            idWorker.lock(table_id, uniqueId);
            String key = table_id + Constant.separator + "key";
            String oldId = redisTemplate.opsForValue().get(key);
            if (foodOrderDao.queryCount(Long.parseLong(order)) != 0 || (oldId != null && !oldId.equals(order))) {
                idWorker.unLock(table_id, uniqueId);
                throw new HandleException("请不要重复生成订单");
            }
            foodOrder.setTable_id(Long.parseLong(table_id));
            foodOrder.setT_name(table_name);
            foodOrderDao.insert(foodOrder);
            redisTemplate.delete(table_id + Constant.separator + "food");
            redisTemplate.opsForValue().set(key, uniqueId);
            idWorker.unLock(table_id, uniqueId);
        } else {
            foodOrderDao.insert(foodOrder);
        }
        for (FoodSale foodSale : foodSales) {
            foodSaleDao.insert(foodSale);
        }
        return id;
    }

    @Override
    public List<FoodOrder> getOrders(String openid) {
       return foodOrderDao.query(openid);
    }
}
