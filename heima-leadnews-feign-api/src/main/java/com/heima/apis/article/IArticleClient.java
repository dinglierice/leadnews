package com.heima.apis.article;

import com.heima.model.article.dtos.ArticleDto;
import com.heima.model.common.dtos.ResponseResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * @description：TODO
 * @author：dinglie
 * @date：2023/10/15 10:29
 */
@FeignClient("leadnews-article")
public interface IArticleClient {
    @PostMapping("/api/v1/article/save")
    ResponseResult saveArticle(ArticleDto articleDto);
}
