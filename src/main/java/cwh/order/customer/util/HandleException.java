package cwh.order.customer.util;

/**
 * Created by Administrator on 2018/11/4 0004.
 */
public class HandleException extends Exception {

    public HandleException(String message) {
        super(message + "！");
    }
}
