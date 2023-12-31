package com.heima.article.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.article.mapper.ApArticleConfigMapper;
import com.heima.article.mapper.ApArticleContentMapper;
import com.heima.article.mapper.ApArticleMapper;
import com.heima.article.service.ApArticleService;
import com.heima.article.service.ArticleFreemarkerService;
import com.heima.model.article.dtos.ArticleDto;
import com.heima.model.article.dtos.ArticleHomeDto;
import com.heima.model.article.pojos.ApArticle;
import com.heima.common.constants.ArticleConstants;
import com.heima.model.article.pojos.ApArticleConfig;
import com.heima.model.article.pojos.ApArticleContent;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * @description：TODO
 * @author：dinglie
 * @date：2023/10/7 13:53
 */
@Service
@Transactional
@Slf4j
public class ApArticleServiceImpl extends ServiceImpl<ApArticleMapper, ApArticle> implements ApArticleService {
    private static final Short MAX_PAGE_SIZE = 20;

    @Resource
    ApArticleMapper apArticleMapper;

    @Resource
    ApArticleConfigMapper apArticleConfigMapper;

    @Resource
    ApArticleContentMapper apArticleContentMapper;

    @Resource
    ArticleFreemarkerService articleFreemarkerService;

    @Override
    public ResponseResult<List<ApArticle>> load(Short loadType, ArticleHomeDto dto) {

        Integer size = dto.getSize();
        int fixSize = null == size || 0 == size ? 10 : Math.min(size, MAX_PAGE_SIZE);
        dto.setSize(fixSize);

        if (loadType == null || (!loadType.equals(ArticleConstants.LOADTYPE_LOAD_NEW) && !loadType.equals(ArticleConstants.LOADTYPE_LOAD_MORE))) {
            loadType = ArticleConstants.LOADTYPE_LOAD_MORE;
        }

        if (StringUtils.isBlank(dto.getTag())) {
            dto.setTag(ArticleConstants.DEFAULT_TAG);
        }

        if(dto.getMaxBehotTime() == null) dto.setMaxBehotTime(new Date());
        if(dto.getMinBehotTime() == null) dto.setMinBehotTime(new Date());

        //2.查询数据
        List<ApArticle> apArticles = apArticleMapper.loadArticleList(dto, loadType);

        //3.结果封装
        return ResponseResult.okResult2(apArticles);
    }

    @Override
    @SneakyThrows
    public ResponseResult saveArticle(ArticleDto articleDto) {
        if (null == articleDto) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        ApArticle apArticle = new ApArticle();
        BeanUtils.copyProperties(apArticle, articleDto);

        if (null == articleDto.getId()) {
            save(apArticle);

            ApArticleConfig apArticleConfig = new ApArticleConfig(apArticle.getId());
            apArticleConfigMapper.insert(apArticleConfig);

            ApArticleContent apArticleContent = new ApArticleContent();
            apArticleContent.setArticleId(apArticle.getId());
            apArticleContentMapper.insert(apArticleContent);
        } else {
            updateById(apArticle);

            ApArticleContent apArticleContent = apArticleContentMapper.selectOne(Wrappers.<ApArticleContent>lambdaQuery().eq(ApArticleContent::getArticleId, apArticle.getId()));
            apArticleContent.setContent(articleDto.getContent());
            apArticleContentMapper.updateById(apArticleContent);
        }

        articleFreemarkerService.buildArticleToMinIO(apArticle, articleDto.getContent());

        return ResponseResult.okResult(apArticle.getId());
    }
}
