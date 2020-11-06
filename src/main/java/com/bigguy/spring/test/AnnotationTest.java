package com.bigguy.spring.test;

import com.bigguy.spring.config.SpringContext;
import com.bigguy.spring.service.HelloServiceTest;
import com.bigguy.spring.service.UserSvc;
import com.bigguy.spring.util.ApplicationContextUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * @Description:
 * @Author bigguy
 * @Date 2020/9/19
 **/
@Slf4j
public class AnnotationTest {
    public static void main(String[] args) {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(SpringContext.class);

        HelloServiceTest helloServiceTest = context.getBean(HelloServiceTest.class);
        helloServiceTest.testSayHello();
    }

    private static AnnotationConfigApplicationContext testInit() {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(SpringContext.class);
        UserSvc userSvc = context.getBean(UserSvc.class);
        userSvc.sayHello();
        UserSvc bean = ApplicationContextUtils.getBean(UserSvc.class);
        log.info("userSvc equals bean {}", userSvc.equals(bean));
        log.info("userSvc == bean {}", userSvc == bean);
        return context;
    }
}
