package cwh.order.customer.util;

import lombok.Data;

import java.util.List;

/**
 * Created by 曹文豪 on 2018/11/12.
 */
@Data
public class PageQuery {

    private String string_param;
    private int int_param;
    private int start;
    private int count;
}
