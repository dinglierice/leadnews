package com.heima.wemedia.controller.v1;

import com.heima.model.common.dtos.ResponseResult;
import com.heima.wemedia.service.WmChannelService;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @description：TODO
 * @author：dinglie
 * @date：2023/10/14 10:44
 */
@RestController
@RequestMapping("/api/v1/channel")
@Api(value = "频道服务",tags = "频道服务")
public class WmChannelController {

    @Resource
    WmChannelService wmChannelService;

    @GetMapping("/channels")
    public ResponseResult findAll() {
        return wmChannelService.findAll();
    }
}
