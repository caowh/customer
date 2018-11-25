package cwh.order.customer.dao;

import cwh.order.customer.model.BuyUser;
import org.springframework.stereotype.Repository;

/**
 * Created by Administrator on 2018/11/24 0024.
 */
@Repository
public interface BuyUserDao {

    String queryPhone(String openid);

    void insert(BuyUser buyUser);

    void updatePhone(BuyUser buyUser);
}
