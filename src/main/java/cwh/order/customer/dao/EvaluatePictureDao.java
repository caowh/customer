package cwh.order.customer.dao;

import cwh.order.customer.model.EvaluatePicture;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by 曹文豪 on 2018/11/23.
 */
@Repository
public interface EvaluatePictureDao {

    void insert(EvaluatePicture evaluatePicture);

    List<String> query(long id);

    int queryCount(long id);
}
