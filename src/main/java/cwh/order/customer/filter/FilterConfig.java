package cwh.order.customer.filter;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

/**
 * Created by 曹文豪 on 2018/6/15.
 */
@Configuration
public class FilterConfig {

    @Resource
    private AuthFilter authFilter;

    @Bean
    public FilterRegistrationBean responseFilter() {
        FilterRegistrationBean frBean = new FilterRegistrationBean();
        frBean.setOrder(1);
        frBean.setFilter(authFilter);
        frBean.addUrlPatterns("/*");
        return frBean;
    }

}