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
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author ：hechen
 * @data ：2020/11/5
 * @description ：
 */
public class ApplicationContextXmlTest {

    private static final String SPRING_CONFIG_PATH = "spring-context.xml";

    public static void main(String[] args) {

        ApplicationContext applicationContext = new ClassPathXmlApplicationContext(SPRING_CONFIG_PATH);
        UserSvc userSvc = applicationContext.getBean(UserSvc.class);
        userSvc.sayHello();
    }

}
