package cwh.order.customer.util;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * Created by Administrator on 2018/11/4 0004.
 */
@Aspect
@Component
public class ServiceAspect {

    private static final Logger logger = LoggerFactory.getLogger(ServiceAspect.class);

    @Pointcut("execution(public * cwh.order.customer.service.*.*(..))")
    public void webLog() {
    }

    @Before("webLog()")
    public void beforeLog(JoinPoint jp) {
        Object[] args = jp.getArgs();
        logger.info("call method:" + jp.toString() + ",args:" + Arrays.toString(args));
    }
}
