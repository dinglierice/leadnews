package com.heima.wemedia.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.model.common.dtos.PageResponseResult;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.wemedia.dtos.WmNewsPageReqDto;
import com.heima.model.wemedia.pojos.WmNews;
import com.heima.utils.thread.WmThreadLocalUtil;
import com.heima.wemedia.mapper.WmNewsMapper;
import com.heima.wemedia.service.WmNewsService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @description：TODO
 * @author：dinglie
 * @date：2023/10/14 11:08
 */
@Slf4j
@Transactional
@Service
public class WmNewsServiceImpl extends ServiceImpl<WmNewsMapper, WmNews> implements WmNewsService {
    @Override
    public ResponseResult findAll(WmNewsPageReqDto wmNewsPageReqDto) {
        wmNewsPageReqDto.checkParam();
        IPage page = new Page(wmNewsPageReqDto.getPage(), wmNewsPageReqDto.getSize());
        LambdaQueryWrapper<WmNews> lambdaQueryWrapper = new LambdaQueryWrapper<>();

        if(null != wmNewsPageReqDto.getStatus()) {
            lambdaQueryWrapper.eq(WmNews::getStatus, wmNewsPageReqDto.getStatus());
        }

        if(null != wmNewsPageReqDto.getChannelId()) {
            lambdaQueryWrapper.eq(WmNews::getChannelId, wmNewsPageReqDto.getChannelId());
        }

        if(null != wmNewsPageReqDto.getBeginPubDate() && null != wmNewsPageReqDto.getEndPubDate()) {
            lambdaQueryWrapper.between(WmNews::getPublishTime, wmNewsPageReqDto.getBeginPubDate(), wmNewsPageReqDto.getEndPubDate());
        }

        if(StringUtils.isNotBlank(wmNewsPageReqDto.getKeyword())) {
            lambdaQueryWrapper.like(WmNews::getTitle, wmNewsPageReqDto.getKeyword());
        }

        lambdaQueryWrapper.eq(WmNews::getUserId, WmThreadLocalUtil.getUser().getId());
        lambdaQueryWrapper.orderByDesc(WmNews::getPublishTime);

        page = page(page, lambdaQueryWrapper);

        ResponseResult result = new PageResponseResult(wmNewsPageReqDto.getPage(), wmNewsPageReqDto.getSize(), (int)page.getTotal());
        result.setData(page.getRecords());
        return result;
    }
}
