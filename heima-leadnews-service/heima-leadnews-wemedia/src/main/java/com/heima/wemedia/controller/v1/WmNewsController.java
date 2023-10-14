package com.heima.wemedia.controller.v1;

import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.wemedia.dtos.WmNewsDto;
import com.heima.model.wemedia.dtos.WmNewsPageReqDto;
import com.heima.model.wemedia.pojos.WmNews;
import com.heima.wemedia.service.WmNewsService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @description：TODO
 * @author：dinglie
 * @date：2023/10/14 11:04
 */
@RestController
@RequestMapping("/api/v1/news")
@Api(value = "内容服务",tags = "内容服务")
public class WmNewsController {
    @Resource
    WmNewsService wmNewsService;

    @PostMapping("/list")
    @ApiOperation(value = "查询全部文章")
    public ResponseResult findList(@RequestBody WmNewsPageReqDto wmNewsPageReqDto) {
        return wmNewsService.findAll(wmNewsPageReqDto);
    }

    @PostMapping("/submit")
    @ApiOperation(value = "上传新闻")
    public ResponseResult submitNews(@RequestBody WmNewsDto wmNewsDto) {
        return wmNewsService.submit(wmNewsDto);
    }
}
