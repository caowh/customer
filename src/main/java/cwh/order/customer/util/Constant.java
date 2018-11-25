package cwh.order.customer.util;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by 曹文豪 on 2018/10/30.
 */
public class Constant {

    public static final int CODE_OK = 0;

    public static final int CODE_UNLOGIN = -1;

    public static final int CODE_ERROR = -2;

    public static final String APPID = "wx014054728119c998";

    public static final String APPSECRET = "6b5e4adab0ec196d90104f19ec484f09";

    public static final long TIMEOUT = 30;  //min

    public static final String separator = "%%";

    public static final String phoneKey = "buyPhoneKey";

    public static final String ERROR = "网络异常，稍后重试";

    public static final int ORDER = 1;

    public static final int ADD_ORDER = 0;

    public static final int ORDER_FOOD = 2;

    public static final int CLEAN_FOOD = 3;

    public static final int FIRST_FOOD = 4;

    public static final int ORDER_KEY = 5;

    public static final String FILEPATH = "/usr/share/nginx/html";

    public static String getSafeParameter(HttpServletRequest request, String arg) {
        String param = request.getParameter(arg);
        return param == null ? "" : param.replaceAll("\"", "“").
                replaceAll("'", "‘").trim();
    }

}
