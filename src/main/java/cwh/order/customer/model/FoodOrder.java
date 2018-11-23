package cwh.order.customer.model;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * Created by 曹文豪 on 2018/11/21.
 */
@Data
public class FoodOrder {

    private long id;
    private long table_id;
    private String t_name;
    @JSONField(format="yyyy-MM-dd HH:mm:ss")
    private Date create_time;
    private String openid;
    private String store_id;
    private String store_name;
    private BigDecimal total_price;
    private String phone;
    private String message;
    private int sort;
    private int status;
    private String reason;
    private String headPictureUrl;


    private List<FoodSale> foodSales;
    private int evaluate;

}
