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
package com.bigguy.spring.processor;

import com.bigguy.spring.annotation.RountingInjected;
import com.bigguy.spring.factory.RoutingBeanProxyFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.Map;

/**
 * @author ：hechen
 * @data ：2020/11/4
 * @description ：
 */

@Component
public class HelloServiceInjectProcessor implements BeanPostProcessor{

    @Autowired
    private ApplicationContext applicationContext;

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {

        return bean;
    }

    /**
     * @param field
     * @param bean
     * @param type
     * @throws IllegalAccessException
     */
    private void handleRoutingInjected(Field field, Object bean, Class type) throws IllegalAccessException {
        Map<String, Object> candidates = this.applicationContext.getBeansOfType(type);
        field.setAccessible(true);
        if (candidates.size() == 1) {
            field.set(bean, candidates.values().iterator().next());
        } else if (candidates.size() == 2) {
            String injectVal = field.getAnnotation(RountingInjected.class).value();
            Object proxy = RoutingBeanProxyFactory.createProxy(injectVal, type, candidates);
            field.set(bean, proxy);
        } else {
            throw new IllegalArgumentException("Find more than 2 beans for type: " + type);
        }
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Class<?> targetCls = bean.getClass();
        Field[] targetFld = targetCls.getDeclaredFields();
        for (Field field : targetFld) {
            //找到制定目标的注解类
            if (field.isAnnotationPresent(RountingInjected.class)) {
                if (!field.getType().isInterface()) {
                    throw new BeanCreationException("RoutingInjected field must be declared as an interface:" + field.getName()
                            + " @Class " + targetCls.getName());
                }
                try {
                    this.handleRoutingInjected(field, bean, field.getType());
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }

        return bean;
    }
}
