package cwh.order.customer.dao;

import cwh.order.customer.model.FoodSale;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by 曹文豪 on 2018/11/21.
 */
@Repository
public interface FoodSaleDao {

    void insert(FoodSale foodSale);

    List<FoodSale> queryByOrder(long id);

    int updatePraise(FoodSale foodSale);
}
