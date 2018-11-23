package cwh.order.customer.model;

import lombok.Data;

import java.util.Date;

/**
 * Created by 曹文豪 on 2018/11/23.
 */
@Data
public class OrderEvaluate {

    private long order_id;
    private String message;
    private int evaluate_type;
    private Date create_time;

}
