package cwh.order.customer.model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Created by 曹文豪 on 2018/10/30.
 */
@Data
public class Food {

    @JsonSerialize(using = ToStringSerializer.class)
    private long id;
    private String f_name;
    private String description;
    private BigDecimal price;
    private int status;
    @JsonSerialize(using = ToStringSerializer.class)
    private long classify_id;
    private String picture_url;

    private int monthly_sale;
    private int praise_count;
    private String classify_name;
    private String openid;

}
