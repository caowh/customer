package cwh.order.customer.filter;

import com.alibaba.fastjson.JSON;
import cwh.order.customer.util.Constant;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.*;
import javax.servlet.FilterConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by 曹文豪 on 2018/6/15.
 */
@Component
public class AuthFilter implements Filter {

    @Resource
    private RedisTemplate redisTemplate;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        if (servletResponse instanceof HttpServletResponse) {
            HttpServletResponse httpServletResponse = (HttpServletResponse) servletResponse;
            httpServletResponse.setHeader("Access-Control-Allow-Origin", "*");
            httpServletResponse.setHeader("Access-Control-Allow-Methods", "OPTIONS,GET,POST");
            httpServletResponse.setHeader("Access-Control-Allow-Headers", "content-type,token");
            HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
            if (httpServletRequest.getMethod().equals("OPTIONS")) {
                httpServletResponse.setStatus(200);
                httpServletResponse.setHeader("Access-Control-Max-Age", "1728000"); //20
                httpServletResponse.getWriter().write("OPTIONS returns OK");
                return;
            }
            httpServletRequest.setCharacterEncoding("utf-8");
            httpServletResponse.setCharacterEncoding("utf-8");
            String token = httpServletRequest.getHeader("token");
            String url = httpServletRequest.getRequestURI();
            if (url.endsWith("/getToken")) {
                filterChain.doFilter(httpServletRequest, httpServletResponse);
                return;
            }
            assert token != null;
            Object o = redisTemplate.opsForValue().get(token);
            if (o == null) {
                httpServletResponse.setStatus(200);
                Map<String, Object> map = new HashMap<>();
                map.put("status", Constant.CODE_UNLOGIN);
                httpServletResponse.getWriter().write(JSON.toJSONString(map));
            } else {
                httpServletRequest.setAttribute("openid", o);
                filterChain.doFilter(httpServletRequest, httpServletResponse);
            }
        }
    }


    @Override
    public void destroy() {

    }
}
