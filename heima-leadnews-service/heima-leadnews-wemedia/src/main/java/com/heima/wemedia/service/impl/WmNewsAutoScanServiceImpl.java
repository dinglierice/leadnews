package com.heima.wemedia.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.heima.apis.article.IArticleClient;
import com.heima.common.tess4j.Tess4jClient;
import com.heima.file.service.FileStorageService;
import com.heima.model.article.dtos.ArticleDto;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.wemedia.pojos.WmChannel;
import com.heima.model.wemedia.pojos.WmNews;
import com.heima.model.wemedia.pojos.WmSensitive;
import com.heima.model.wemedia.pojos.WmUser;
import com.heima.utils.common.SensitiveWordUtil;
import com.heima.wemedia.mapper.WmChannelMapper;
import com.heima.wemedia.mapper.WmNewsMapper;
import com.heima.wemedia.mapper.WmSensitiveMapper;
import com.heima.wemedia.mapper.WmUserMapper;
import com.heima.wemedia.service.WmNewsAutoScanService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @description：TODO
 * @author：dinglie
 * @date：2023/10/15 14:11
 */
@Service
@Slf4j
@Transactional
public class WmNewsAutoScanServiceImpl implements WmNewsAutoScanService {
    @Resource
    WmNewsMapper wmNewsMapper;

    @Resource
    IArticleClient iArticleClient;

    @Resource
    Tess4jClient tess4jClient;

    @Resource
    WmChannelMapper wmChannelMapper;

    @Resource
    WmUserMapper wmUserMapper;

    @Resource
    FileStorageService fileStorageService;

    @Resource
    WmSensitiveMapper wmSensitiveMapper;

    @Override
    @Async
    public void autoScanWmNews(Integer id) {
        WmNews wmNews = wmNewsMapper.selectById(id);
        if (null == wmNews) {
            throw new RuntimeException("WmNewsAutoScanServiceImpl-文章不存在");
        }
        if (wmNews.getStatus().equals(WmNews.Status.SUBMIT.getCode())) {
            //从内容中提取纯文本内容和图片
            Map<String,Object> textAndImages = handleTextAndImages(wmNews);

            Boolean contentScanResult = scanTextResult(String.valueOf(textAndImages.get("content")), wmNews);
            Boolean imagesScanResult = scanImgResult((List<String>) textAndImages.get("images"), wmNews);
            Boolean isSensitive = scanBySensitiveWords((String) textAndImages.get("content"), wmNews);

            if (!contentScanResult || !imagesScanResult || !isSensitive) {
                return;
            }

            ResponseResult responseResult = saveAppArticle(wmNews);
            if(!responseResult.getCode().equals(200)){
                throw new RuntimeException("WmNewsAutoScanServiceImpl-文章审核，保存app端相关文章数据失败");
            }

            //回填article_id
            wmNews.setArticleId((Long) responseResult.getData());
            updateWmNews(wmNews,(short) 9,"审核成功");
        }

    }

    private Boolean scanBySensitiveWords(String content, WmNews wmNews) {
        boolean flag = true;

        //获取所有的敏感词
        List<WmSensitive> wmSensitives = wmSensitiveMapper.selectList(Wrappers.<WmSensitive>lambdaQuery().select(WmSensitive::getSensitives));
        List<String> sensitiveList = wmSensitives.stream().map(WmSensitive::getSensitives).collect(Collectors.toList());

        //初始化敏感词库
        SensitiveWordUtil.initMap(sensitiveList);

        //查看文章中是否包含敏感词
        Map<String, Integer> map = SensitiveWordUtil.matchWords(content);
        if(map.size() >0){
            updateWmNews(wmNews,(short) 2,"当前文章中存在违规内容"+map);
            flag = false;
        }

        return flag;
    }

    private void updateWmNews(WmNews wmNews, short status, String msg) {
        wmNews.setStatus(status);
        wmNews.setReason(msg);
        wmNewsMapper.updateById(wmNews);
    }

    @SneakyThrows
    private ResponseResult saveAppArticle(WmNews wmNews) {
        ArticleDto dto = new ArticleDto();
        BeanUtils.copyProperties(dto , wmNews);

        dto.setLayout(wmNews.getType());
        WmChannel wmChannel = wmChannelMapper.selectById(wmNews.getChannelId());
        dto.setChannelId(wmChannel.getId());

        dto.setAuthorId(wmNews.getUserId().longValue());
        WmUser wmUser = wmUserMapper.selectById(wmNews.getUserId());
        if(wmUser != null){
            dto.setAuthorName(wmUser.getName());
        }

        //设置文章id
        dto.setId(wmNews.getArticleId());
        dto.setCreatedTime(new Date());

        ResponseResult responseResult = iArticleClient.saveArticle(dto);
        return responseResult;
    }


    private Boolean scanImgResult(List<String> images, WmNews wmNews) {
        boolean flag = true;

        if (images == null || images.size() == 0) {
            return flag;
        }

        images = images.stream().distinct().collect(Collectors.toList());


        try {
            for (String image : images) {
                byte[] bytes = fileStorageService.downLoadFile(image);

                //byte[] 转换为bufferedImage
                ByteArrayInputStream in = new ByteArrayInputStream(bytes);
                BufferedImage bufferedImage = ImageIO.read(in);

                //图片识别
                String result = tess4jClient.doOCR(bufferedImage);
                //过滤文字
                boolean isSensitive = scanBySensitiveWords(result, wmNews);
                if(!isSensitive){
                    return isSensitive;
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        return flag;
    }

    private Boolean scanTextResult(String content, WmNews wmNews) {
        return true;
    }

    private Map<String, Object> handleTextAndImages(WmNews wmNews) {
        //存储纯文本内容
        StringBuilder stringBuilder = new StringBuilder();
        List<String> images = new ArrayList<>();

        if(StringUtils.isNotBlank(wmNews.getContent())){
            List<Map> maps = JSONArray.parseArray(wmNews.getContent(), Map.class);
            for (Map map : maps) {
                if (map.get("type").equals("text")){
                    stringBuilder.append(map.get("value"));
                }

                if (map.get("type").equals("image")){
                    images.add((String) map.get("value"));
                }
            }
        }
        //2.提取文章的封面图片
        if(StringUtils.isNotBlank(wmNews.getImages())){
            String[] split = wmNews.getImages().split(",");
            images.addAll(Arrays.asList(split));
        }

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("content",stringBuilder.toString());
        resultMap.put("images",images);
        return resultMap;
    }
}
