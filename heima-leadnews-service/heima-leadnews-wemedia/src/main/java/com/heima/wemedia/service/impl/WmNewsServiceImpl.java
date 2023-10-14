package com.heima.wemedia.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.common.constants.WemediaConstants;
import com.heima.common.exception.CustomException;
import com.heima.model.common.dtos.PageResponseResult;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.wemedia.dtos.WmNewsDto;
import com.heima.model.wemedia.dtos.WmNewsPageReqDto;
import com.heima.model.wemedia.pojos.WmMaterial;
import com.heima.model.wemedia.pojos.WmNews;
import com.heima.model.wemedia.pojos.WmNewsMaterial;
import com.heima.utils.thread.WmThreadLocalUtil;
import com.heima.wemedia.mapper.WmMaterialMapper;
import com.heima.wemedia.mapper.WmNewsMapper;
import com.heima.wemedia.mapper.WmNewsMaterialMapper;
import com.heima.wemedia.service.WmNewsService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @description：自媒体文章服务
 * @author：dinglie
 * @date：2023/10/14 11:08
 */
@Slf4j
@Transactional
@Service
public class WmNewsServiceImpl extends ServiceImpl<WmNewsMapper, WmNews> implements WmNewsService {
    @Resource
    WmNewsMaterialMapper wmNewsMaterialMapper;

    @Resource
    WmMaterialMapper wmMaterialMapper;

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

    @Override
    @SneakyThrows
    public ResponseResult submit(WmNewsDto wmNewsDto) {
        if (wmNewsDto == null || wmNewsDto.getContent() == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }

        WmNews wmNews = new WmNews();
        BeanUtils.copyProperties(wmNewsDto, wmNews);
        if (CollectionUtils.isNotEmpty(wmNewsDto.getImages())) {
            wmNews.setImages(StringUtils.join(wmNewsDto.getImages(), ","));
        }

        if (wmNewsDto.getType().equals(WemediaConstants.WM_NEWS_TYPE_AUTO)) {
            wmNews.setType(null);
        }

        saveOrUpdateWmNews(wmNews);

        if (wmNewsDto.getStatus().equals(WmNews.Status.NORMAL.getCode())) {
            return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
        }

        List<String> imgMaterials = extractUrlInfo(wmNewsDto.getContent());
        saveRelativeInfo4Content(imgMaterials, wmNews.getId());

        saveRelativeInfo4Cover(wmNewsDto, wmNews, wmNewsDto.getImages());

        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }

    /**
     * 提取文章中的图片信息
     * @param content
     * @return
     */
    private List<String> extractUrlInfo(String content) {
        List<String> materials = new ArrayList<>();

        List<Map> maps = JSON.parseArray(content, Map.class);
        maps.forEach(x -> {
            if ("image".equals(x.get("type"))) {
                String imgUrl = String.valueOf(x.get("value"));
                materials.add(imgUrl);
            }
        });

        return materials;
    }

    /**
     * 内容图片关系保存
     * @param materials
     * @param newsId
     */
    private void saveRelativeInfo4Content(List<String> materials, Integer newsId) {
        saveRelativeInfo(materials, newsId, WemediaConstants.WM_NEWS_NONE_IMAGE);
    }

    private void saveRelativeInfo4Cover(WmNewsDto dto, WmNews wmNews, List<String> materials) {
        List<String> images = dto.getImages();
        if(dto.getType().equals(WemediaConstants.WM_NEWS_TYPE_AUTO)) {
            if (materials.size() >= 3){
                wmNews.setType(WemediaConstants.WM_NEWS_MANY_IMAGE);
                images = materials.stream().limit(3).collect(Collectors.toList());
            } else if (materials.size() >= 1 && materials.size() < 3) {
                wmNews.setType(WemediaConstants.WM_NEWS_SINGLE_IMAGE);
                images = materials.stream().limit(1).collect(Collectors.toList());
            } else {
                wmNews.setType(WemediaConstants.WM_NEWS_NONE_IMAGE);
            }

            if (CollectionUtils.isNotEmpty(images)) {
                wmNews.setImages(StringUtils.join(images, ","));
            }
            updateById(wmNews);
        }

        if (CollectionUtils.isNotEmpty(images)) {
            saveRelativeInfo(images, wmNews.getId(), WemediaConstants.WM_COVER_REFERENCE);
        }
    }

    private void saveRelativeInfo(List<String> materials, Integer newsId, Short type) {
        if (CollectionUtils.isEmpty(materials)) {
            return;
        }
        List<WmMaterial> wmMaterials = wmMaterialMapper.selectList(Wrappers.<WmMaterial>lambdaQuery().in(WmMaterial::getUrl, materials));

        if (CollectionUtils.isEmpty(wmMaterials) || wmMaterials.size() != materials.size()) {
            throw new CustomException(AppHttpCodeEnum.MATERIAL_REFERENCE_FAIL);
        }

        List<Integer> idList = wmMaterials.stream().map(WmMaterial::getId).collect(Collectors.toList());


        wmNewsMaterialMapper.saveRelations(idList, newsId, type);
    }


    private void saveOrUpdateWmNews(WmNews wmNews) {
        wmNews.setUserId(WmThreadLocalUtil.getUser().getId());
        wmNews.setCreatedTime(new Date());
        wmNews.setSubmitedTime(new Date());
        wmNews.setEnable((short) 1);

        if (wmNews.getId() == null) {
            save(wmNews);
        } else {
            wmNewsMaterialMapper.delete(Wrappers.<WmNewsMaterial>lambdaQuery().eq(WmNewsMaterial::getId, wmNews.getId()));
            updateById(wmNews);
        }
    }
}
