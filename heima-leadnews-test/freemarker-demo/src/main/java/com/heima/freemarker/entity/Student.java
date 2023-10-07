package com.heima.freemarker.entity;

import lombok.Data;

import java.util.Date;

/**
 * @description：TODO
 * @author：dinglie
 * @date：2023/10/7 16:35
 */
@Data
public class Student {
    private String name;//姓名
    private int age;//年龄
    private Date birthday;//生日
    private Float money;//钱包
}
