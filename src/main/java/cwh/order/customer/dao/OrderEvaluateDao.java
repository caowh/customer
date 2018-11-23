package cwh.order.customer.dao;

import cwh.order.customer.model.OrderEvaluate;
import org.springframework.stereotype.Repository;

/**
 * Created by 曹文豪 on 2018/11/23.
 */
@Repository
public interface OrderEvaluateDao {

    void insert(OrderEvaluate orderEvaluate);
}
