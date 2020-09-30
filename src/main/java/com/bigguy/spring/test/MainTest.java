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

import org.junit.jupiter.api.Test;

import java.util.StringTokenizer;

/**
 * @author ：hechen
 * @data ：2020/9/30
 * @description ：
 */
public class MainTest {

    /**
     * 可以通过",", ";" 分割beanName
     */
    @Test
    public void testStringToken(){
        String str = "a,b,c,d";
        String str2 = "a;b;c;d";
        String delimiters = ",;";
        StringTokenizer stoken1 = new StringTokenizer(str, delimiters);
        StringTokenizer stoken2 = new StringTokenizer(str2, delimiters);

        while(stoken1.hasMoreTokens()) {
            System.out.println(stoken1.nextToken());
        }

        System.out.println("\n-------------------------------\n");

        while(stoken2.hasMoreTokens()) {
            System.out.println(stoken2.nextToken());
        }
    }

}
