package cwh.order.customer.dao;

import cwh.order.customer.model.Table;
import org.springframework.stereotype.Repository;

/**
 * Created by 曹文豪 on 2018/11/19.
 */
@Repository
public interface TableDao {

    String queryName(Table table);
}
