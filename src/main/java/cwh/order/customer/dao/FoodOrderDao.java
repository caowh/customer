package cwh.order.customer.dao;

import cwh.order.customer.model.FoodOrder;
import cwh.order.customer.util.PageQuery;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Created by 曹文豪 on 2018/11/21.
 */
@Repository
public interface FoodOrderDao {

    void insert(FoodOrder foodOrder);

    int queryCount(long id);

    int queryCountByStore(String store_id);

    FoodOrder queryDetail(FoodOrder foodOrder);

    List<FoodOrder> query(PageQuery pageQuery);

    int updateStatus(FoodOrder foodOrder);

    int queryStatus(FoodOrder foodOrder);

    String queryStore(long id);
}
