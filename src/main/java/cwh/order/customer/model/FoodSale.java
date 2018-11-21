package cwh.order.customer.model;

import lombok.Data;

import java.math.BigDecimal;

/**
 * Created by 曹文豪 on 2018/11/21.
 */
@Data
public class FoodSale {

    private long order_id;
    private long food_id;
    private String food_name;
    private BigDecimal food_price;
    private int food_count;
}
