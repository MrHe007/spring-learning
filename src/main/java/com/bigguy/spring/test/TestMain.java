/*
Copyright (C) 2011-$today.year. ShenZhen IBOXCHAIN Information Technology Co.,Ltd.

All right reserved.

This software is the confidential and proprietary
information of IBOXCHAIN Company of China.
("Confidential Information"). You shall not disclose
such Confidential Information and shall use it only
in accordance with the terms of the contract agreement
you entered into with IBOXCHAIN inc.

*/
package com.bigguy.spring.test;

import com.bigguy.spring.service.UserSvc;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.ClassPathResource;

/**
 * @author ：hechen
 * @data ：2020/10/30
 * @description ：
 */
public class TestMain {

    private static final String SPRING_CONFIG_PATH = "spring-context.xml";

    @Test
    public void testSayhello(){
        BeanFactory beanFactory =  new XmlBeanFactory(new ClassPathResource(SPRING_CONFIG_PATH));
        UserSvc userSvc = beanFactory.getBean(UserSvc.class);
        System.out.println("ff");
    }


}
