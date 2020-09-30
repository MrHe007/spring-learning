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

    private static final String SPRING_CONFIG_PATH = "spring-context.xml";

    public static void main(String[] args) {
        testMain();
    }

    private static void testMain() {
        String xmlPath = "spring-context.xml";
        ApplicationContext applicationContext = new ClassPathXmlApplicationContext(xmlPath);

        // 从容器中拿 bean
        UserSvc userSvc = applicationContext.getBean(UserSvc.class);
        userSvc.sayHello();
    }

    public static ApplicationContext getApplication(){
        String xmlPath = "spring-context.xml";
        ApplicationContext applicationContext = new ClassPathXmlApplicationContext(xmlPath);
        return applicationContext;
    }

}
