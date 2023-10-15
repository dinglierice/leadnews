package com.heima.article;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * @description：TODO
 * @author：dinglie
 * @date：2023/10/7 14:33
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableAsync
@MapperScan("com.heima.article.mapper")
public class ArticleApplication {
    public static void main(String[] args) {
        SpringApplication.run(ArticleApplication.class, args);
    }
}
