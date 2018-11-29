package cwh.order.customer.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import cwh.order.customer.dao.*;
import cwh.order.customer.model.*;
import cwh.order.customer.service.FoodService;
import cwh.order.customer.util.*;
import cwh.order.customer.websocket.MyTextWebSocketHandler;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;
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
    @Resource
    private OrderEvaluateDao orderEvaluateDao;
    @Resource
    private EvaluatePictureDao evaluatePictureDao;

    @Override
    @Transactional(rollbackFor = HandleException.class)
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
        map.put("count", foodOrderDao.queryCountByStore(store_id));
        map.put("store", store);
        map.put("foods", foodDao.queryAll(store_id));
        return map;
    }

    @Override
    @Transactional(rollbackFor = HandleException.class)
    public void order(String openid, String foods, String store_id, String table_id, String order, String phone, String message) throws HandleException {
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
            int count = food.getIntValue("count");
            if (count <= 0) {
                throw new HandleException("菜品“" + name + "”数量必须大于零");
            }
            total_price = total_price.add(t_price.multiply(new BigDecimal(count)));
            FoodSale foodSale = new FoodSale();
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
        foodOrder.setHeadPictureUrl(store.getHeadPictureUrl());
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
            if (oldId == null) {
                throw new HandleException("请重进小程序下单");
            }
            long oldId1 = Long.parseLong(oldId);
            foodOrder.setId(oldId1);
            foodOrderDao.insert(foodOrder);
            for (FoodSale foodSale : foodSales) {
                foodSale.setOrder_id(oldId1);
                foodSaleDao.insert(foodSale);
            }
            redisTemplate.delete(table_id + Constant.separator + "food");
            redisTemplate.opsForValue().set(key, uniqueId);
            idWorker.unLock(table_id, uniqueId);
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("type", Constant.ORDER_KEY);
            jsonObject.put("order", id);
            MyTextWebSocketHandler.sendMessage(openid, jsonObject);
        } else {
            foodOrder.setId(id);
            foodOrderDao.insert(foodOrder);
            for (FoodSale foodSale : foodSales) {
                foodSale.setOrder_id(id);
                foodSaleDao.insert(foodSale);
            }
        }
    }

    @Override
    public List<FoodOrder> getOrders(String openid, int status, int page, int count) {
        PageQuery pageQuery = new PageQuery();
        pageQuery.setInt_param(status);
        pageQuery.setCount(count);
        pageQuery.setStart(page * count);
        pageQuery.setString_param(openid);
        return foodOrderDao.query(pageQuery);
    }

    @Override
    @Transactional(rollbackFor = HandleException.class)
    public FoodOrder getOrderDetail(String openid, long order_id) throws HandleException {
        FoodOrder foodOrder = new FoodOrder();
        foodOrder.setId(order_id);
        foodOrder.setOpenid(openid);
        FoodOrder foodOrder1 = foodOrderDao.queryDetail(foodOrder);
        if (foodOrder1 == null) {
            throw new HandleException("订单不存在");
        }
        foodOrder1.setFoodSales(foodSaleDao.queryByOrder(order_id));
        return foodOrder1;
    }

    @Override
    public void cancelOrder(String openid, long order_id, String reason) throws HandleException {
        FoodOrder foodOrder = new FoodOrder();
        foodOrder.setId(order_id);
        foodOrder.setOpenid(openid);
        foodOrder.setStatus(2);
        foodOrder.setReason(reason);
        int result = foodOrderDao.updateStatus(foodOrder);
        if (result == 0) {
            throw new HandleException("订单无法取消");
        }
    }

    @Override
    @Transactional(rollbackFor = HandleException.class)
    public void orderEvaluate(String openid, long order_id, int type, String message, String foods) throws HandleException {
        if (type != 1 && type != 2) {
            throw new HandleException("不支持的操作类型");
        }
        if (message.length() > 500) {
            throw new HandleException("评论内容不能超过500字符");
        }
        FoodOrder foodOrder = new FoodOrder();
        foodOrder.setId(order_id);
        foodOrder.setOpenid(openid);
        int status = foodOrderDao.queryStatus(foodOrder);
        if (status != 1) {
            throw new HandleException("该订单尚未付款");
        }
        JSONArray jsonArray = JSONArray.parseArray(foods);
        for (Object object : jsonArray) {
            JSONObject jsonObject = (JSONObject) object;
            FoodSale foodSale = new FoodSale();
            foodSale.setFood_id(jsonObject.getLong("food_id"));
            foodSale.setOrder_id(order_id);
            int praise = jsonObject.getIntValue("praise");
            if (praise != 1 && praise != 2) {
                throw new HandleException("不支持的操作类型");
            }
            foodSale.setPraise(praise);
            int result = foodSaleDao.updatePraise(foodSale);
            if (result == 0) {
                throw new HandleException("存在菜品不可评论");
            }
        }
        OrderEvaluate orderEvaluate = new OrderEvaluate();
        orderEvaluate.setOrder_id(order_id);
        orderEvaluate.setMessage(message);
        orderEvaluate.setCreate_time(new Date());
        orderEvaluate.setEvaluate_type(type);
        try {
            orderEvaluateDao.insert(orderEvaluate);
        } catch (DuplicateKeyException e) {
            throw new HandleException("请不要重复评论");
        }
    }

    @Override
    @Transactional(rollbackFor = HandleException.class)
    public void uploadEvaluatePicture(String openid, long order_id, MultipartFile file) throws HandleException {
        if (file == null) {
            throw new HandleException("图片不能为空");
        }
        FoodOrder foodOrder = new FoodOrder();
        foodOrder.setId(order_id);
        foodOrder.setOpenid(openid);
        int status = foodOrderDao.queryStatus(foodOrder);
        if (status != 1) {
            throw new HandleException("该订单尚未付款");
        }
        EvaluatePicture evaluatePicture = new EvaluatePicture();
        evaluatePicture.setOrder_id(order_id);
        try {
            evaluatePicture.setPicture(FileUtil.save(file));
        } catch (IOException e) {
            throw new HandleException("图片保存失败");
        }
        if (evaluatePictureDao.queryCount(order_id) == 3) {
            throw new HandleException("最多只能添加三张评论图片");
        }
        evaluatePictureDao.insert(evaluatePicture);
    }

    @Override
    @Transactional(rollbackFor = HandleException.class)
    public OrderEvaluate getOrderEvaluate(String openid, long order_id) throws HandleException {
        FoodOrder foodOrder = new FoodOrder();
        foodOrder.setId(order_id);
        foodOrder.setOpenid(openid);
        int status = foodOrderDao.queryStatus(foodOrder);
        if (status != 1) {
            throw new HandleException("该订单尚未付款");
        }
        OrderEvaluate orderEvaluate = orderEvaluateDao.query(order_id);
        List<String> list;
        if (orderEvaluate == null) {
            orderEvaluate = new OrderEvaluate();
            orderEvaluate.setOrder_id(order_id);
            list = new ArrayList<>();
        } else {
            list = evaluatePictureDao.query(order_id);
        }
        orderEvaluate.setPictures(list);
        orderEvaluate.setFoodSales(foodSaleDao.queryEvaluate(order_id));
        return orderEvaluate;
    }

    @Override
    @Transactional(rollbackFor = HandleException.class)
    public void orderPay(String openid, long order_id) throws HandleException {
        FoodOrder foodOrder = new FoodOrder();
        foodOrder.setId(order_id);
        foodOrder.setOpenid(openid);
        foodOrder.setStatus(1);
        int result = foodOrderDao.updateStatus(foodOrder);
        if (result == 0) {
            throw new HandleException("该订单已付款");
        }
        List<Long> ids = foodSaleDao.queryFoodIds(order_id);
        for (Long id : ids) {
            FoodSale foodSale = new FoodSale();
            foodSale.setOrder_id(order_id);
            foodSale.setFood_id(id);
            foodSale.setStatus(1);
            int result1 = foodSaleDao.updateStatus(foodSale);
            if (result1 == 0) {
                throw new HandleException("菜品已付款");
            }
        }
        redisTemplate.convertAndSend("orderNew", order_id);
    }
}
