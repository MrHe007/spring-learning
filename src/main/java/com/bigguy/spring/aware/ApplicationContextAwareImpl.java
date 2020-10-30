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
package com.bigguy.spring.aware;

import com.bigguy.spring.util.ApplicationContextUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * @author ：hechen
 * @data ：2020/10/30
 * @description ：实现 aware 接口，会自拿到 applicationContext 资源
 * 需要将这个类扫描到 spring 容器中
 */
@Component
@Slf4j
public class ApplicationContextAwareImpl implements ApplicationContextAware{
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        log.info("进入 applicationContextAware");
        System.out.println("xxxxxxxxxx");
        ApplicationContextUtils.setApplicationContext(applicationContext);
    }
}
