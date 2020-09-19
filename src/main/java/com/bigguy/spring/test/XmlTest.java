package com.bigguy.spring.test;

import com.bigguy.spring.service.UserSvc;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @Description:
 * @Author bigguy
 * @Date 2020/9/19
 **/
public class XmlTest {

    public static void main(String[] args) {
        String xmlPath = "spring-context.xml";
        ApplicationContext applicationContext = new ClassPathXmlApplicationContext(xmlPath);

        // 从容器中拿 bean
        UserSvc userSvc = applicationContext.getBean(UserSvc.class);
        userSvc.sayHello();


    }

}
