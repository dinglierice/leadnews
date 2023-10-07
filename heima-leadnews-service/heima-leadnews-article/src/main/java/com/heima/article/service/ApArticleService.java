package com.heima.article.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.heima.model.article.dtos.ArticleHomeDto;
import com.heima.model.article.pojos.ApArticle;
import com.heima.model.common.dtos.ResponseResult;

import java.util.List;

/**
 * @description：TODO
 * @author：dinglie
 * @date：2023/10/7 13:51
 */
public interface ApArticleService extends IService<ApArticle> {
    /**
     * 根据参数加载文章列表
     * @param loadType 1为加载更多  2为加载最新
     * @param dto
     * @return
     */
    ResponseResult<List<ApArticle>> load(Short loadType, ArticleHomeDto dto);
}
