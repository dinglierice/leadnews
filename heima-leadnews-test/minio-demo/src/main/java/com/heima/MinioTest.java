package com.heima;

import io.minio.MinioClient;
import io.minio.PutObjectArgs;

import java.io.FileInputStream;

/**
 * @description：TODO
 * @author：dinglie
 * @date：2023/10/7 20:10
 */
public class MinioTest {
    public static void main(String[] args) {
        FileInputStream fileInputStream = null;
        try {

            fileInputStream =  new FileInputStream("D:\\list.html");;

            //1.创建minio链接客户端
            MinioClient minioClient = MinioClient.builder().credentials("minio", "minio123").endpoint("http://127.0.0.1:9000").build();
            //2.上传
            PutObjectArgs putObjectArgs = PutObjectArgs.builder()
                    .object("list.html")//文件名
                    .contentType("text/html")//文件类型
                    .bucket("leadnews")//桶名词  与minio创建的名词一致
                    .stream(fileInputStream, fileInputStream.available(), -1) //文件流
                    .build();
            minioClient.putObject(putObjectArgs);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
