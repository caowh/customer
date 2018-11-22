package cwh.order.customer.model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Created by 曹文豪 on 2018/11/21.
 */
@Data
public class FoodSale {

    @JsonSerialize(using = ToStringSerializer.class)
    private long order_id;
    @JsonSerialize(using = ToStringSerializer.class)
    private long food_id;
    private String food_name;
    private BigDecimal food_price;
    private int food_count;
}
