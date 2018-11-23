package cwh.order.customer.dao;

import cwh.order.customer.model.EvaluatePicture;
import org.springframework.stereotype.Repository;

/**
 * Created by 曹文豪 on 2018/11/23.
 */
@Repository
public interface EvaluatePictureDao {

    void insert(EvaluatePicture evaluatePicture);
}
