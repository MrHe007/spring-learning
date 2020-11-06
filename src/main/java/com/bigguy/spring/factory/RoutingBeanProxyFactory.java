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
package com.bigguy.spring.factory;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.framework.ProxyFactory;

import java.util.Map;

/**
 * @author ：hechen
 * @data ：2020/11/4
 * @description ：
 */
public class RoutingBeanProxyFactory {

    private final static String DEFAULT_BEAN_NAME = "helloService1";

    public static Object createProxy(String name, Class type, Map<String, Object> candidates) {
        ProxyFactory proxyFactory = new ProxyFactory();
        proxyFactory.setInterfaces(type);
        proxyFactory.addAdvice(new VersionRoutingMethodInterceptor(name, candidates));
        return proxyFactory.getProxy();
    }

    static class VersionRoutingMethodInterceptor implements MethodInterceptor {
        private Object targetObject;

        public VersionRoutingMethodInterceptor(String name, Map<String, Object> beans) {
            this.targetObject = beans.get(name);
            if (this.targetObject == null) {
                this.targetObject = beans.get(DEFAULT_BEAN_NAME);
            }
        }

        @Override
        public Object invoke(MethodInvocation invocation) throws Throwable {
            return invocation.getMethod().invoke(this.targetObject, invocation.getArguments());
        }
    }

}
