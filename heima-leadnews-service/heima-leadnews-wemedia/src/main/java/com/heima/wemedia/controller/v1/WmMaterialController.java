package com.heima.wemedia.controller.v1;

import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.wemedia.dtos.WmMaterialDto;
import com.heima.wemedia.service.WmMaterialService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;

/**
 * @description：TODO
 * @author：dinglie
 * @date：2023/10/10 23:38
 */
@RestController
@RequestMapping("/api/v1/material")
@Api(value = "自媒体服务",tags = "自媒体服务")
public class WmMaterialController {
    @Resource
    private WmMaterialService wmMaterialService;

    @PostMapping("/upload_picture")
    @ApiOperation(value = "上传图片")
    public ResponseResult uploadPicture(MultipartFile multipartFile){
        return wmMaterialService.uploadPicture(multipartFile);
    }

    @PostMapping("/list")
    @ApiOperation(value = "查询全部图片")
    public ResponseResult findList(@RequestBody WmMaterialDto dto){
        return wmMaterialService.findList(dto);
    }
}
