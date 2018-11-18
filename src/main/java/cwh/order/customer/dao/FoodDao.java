package cwh.order.customer.dao;

import cwh.order.customer.model.Food;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by Administrator on 2018/11/18 0018.
 */
@Repository
public interface FoodDao {

    List<Food> queryAll(String store_id);
}
