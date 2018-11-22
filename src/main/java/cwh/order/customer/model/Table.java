package cwh.order.customer.model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

/**
 * Created by 曹文豪 on 2018/11/19.
 */
@Data
public class Table {

    @JsonSerialize(using = ToStringSerializer.class)
    private long id;
    private String t_name;
    private String openid;

}
