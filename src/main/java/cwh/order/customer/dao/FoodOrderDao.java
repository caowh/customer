package cwh.order.customer.dao;

import cwh.order.customer.model.FoodOrder;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by 曹文豪 on 2018/11/21.
 */
@Repository
public interface FoodOrderDao {

    void insert(FoodOrder foodOrder);

    int queryCount(long id);

    List<FoodOrder> query(String openid);
}
