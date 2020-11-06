package com.bigguy.spring.test;

import com.bigguy.spring.entity.User;
import com.bigguy.spring.service.HelloServiceTest;
import com.bigguy.spring.service.UserFactoryBean;
import com.bigguy.spring.service.UserSvc;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.io.ClassPathResource;

/**
 * @Description:
 * @Author bigguy
 * @Date 2020/9/19
 **/
public class XmlTest {

    private static final String SPRING_CONFIG_PATH = "spring-context.xml";

    public static void main(String[] args) {
        BeanFactory beanFactory =  new XmlBeanFactory(new ClassPathResource(SPRING_CONFIG_PATH));
        HelloServiceTest bean = beanFactory.getBean(HelloServiceTest.class);
        bean.testSayHello();

//        testBeanInstantce();

    }

    private static void testBeanInstantce() {
        BeanFactory beanFactory =  new XmlBeanFactory(new ClassPathResource(SPRING_CONFIG_PATH));
        User user = beanFactory.getBean( "userFactory", User.class);

        UserFactoryBean userFactoryBean = beanFactory.getBean("&userFactory", UserFactoryBean.class);
        User user2 = userFactoryBean.getObject();

        System.out.println(user);
        // 两个实例是同一个
        System.out.println(user.equals(user2));
    }

    @Test
    private static void testBeanFactory() {
        BeanFactory beanFactory =  new XmlBeanFactory(new ClassPathResource(SPRING_CONFIG_PATH));
        UserSvc userSvc = beanFactory.getBean(UserSvc.class);
        UserSvc userSvc2 = (UserSvc)beanFactory.getBean("userSvc");
        System.out.println("ff");
    }

    @Test
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
