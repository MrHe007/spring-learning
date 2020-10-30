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
package com.bigguy.spring.util;

import org.springframework.context.ApplicationContext;

/**
 * @author ：hechen
 * @data ：2020/10/30
 * @description ：
 */
public class ApplicationContextUtils {

    /**
     * ApplicationContext对象，会ApplicationContextAwareImpl中的setApplicationContext方法中赋值
     */
    private static ApplicationContext applicationContext;
    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }
    public static void setApplicationContext(ApplicationContext applicationContext) {
        ApplicationContextUtils.applicationContext = applicationContext;
    }
    /**
     * 根据类型获取指定的bean
     * @param requiredType Class
     * @param <T> 泛型
     * @return
     */
    public static <T> T getBean(Class<T> requiredType ){
        return applicationContext.getBean(requiredType);
    }
    /**
     * 根据名称和类型获取Bean
     * @param name bean的id
     * @param requiredType class
     * @param <T>
     * @return
     */
    public static <T> T getBean(String name,Class<T> requiredType){
        return applicationContext.getBean(name,requiredType);
    }

}
