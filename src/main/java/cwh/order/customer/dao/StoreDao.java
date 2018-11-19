package cwh.order.customer.dao;

import cwh.order.customer.model.Store;
import org.springframework.stereotype.Repository;

/**
 * Created by 曹文豪 on 2018/11/19.
 */
@Repository
public interface StoreDao {

    Store query(String openid);

}
