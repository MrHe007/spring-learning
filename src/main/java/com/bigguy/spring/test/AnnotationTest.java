package com.bigguy.spring.test;

import com.bigguy.spring.config.SpringContext;
import com.bigguy.spring.service.UserSvc;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * @Description:
 * @Author bigguy
 * @Date 2020/9/19
 **/
public class AnnotationTest {
    public static void main(String[] args) {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(SpringContext.class);
        UserSvc userSvc = context.getBean(UserSvc.class);
        userSvc.sayHello();
    }
}
