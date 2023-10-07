package com.heima;

import com.heima.file.service.FileStorageService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

/**
 * @description：TODO
 * @author：dinglie
 * @date：2023/10/7 20:43
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class MinIOTest {
    @Autowired
    FileStorageService fileStorageService;

    @Test
    public void testUpdateImgFile() {
        try {
            FileInputStream fileInputStream = new FileInputStream("D:\\mc.jpg");
            String filePath = fileStorageService.uploadImgFile("", "mc.jpg", fileInputStream);
            System.out.println(filePath);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
