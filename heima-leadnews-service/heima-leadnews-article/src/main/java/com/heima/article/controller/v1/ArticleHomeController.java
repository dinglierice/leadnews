package com.heima.article.controller.v1;

import com.heima.article.service.ApArticleService;
import com.heima.model.article.dtos.ArticleHomeDto;
import com.heima.model.article.pojos.ApArticle;
import com.heima.common.constants.ArticleConstants;
import com.heima.model.common.dtos.ResponseResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/api/v1/article")
@Api(value = "app首页文章",tags = "app首页文章")
public class ArticleHomeController {

    @Resource
    ApArticleService apArticleService;

    @PostMapping("/load")
    @ApiOperation("加载全部")
    public ResponseResult<List<ApArticle>> load(@RequestBody ArticleHomeDto dto) {
        return apArticleService.load(null, dto);
    }

    @PostMapping("/loadmore")
    @ApiOperation("加载更多")
    public ResponseResult<List<ApArticle>> loadMore(@RequestBody ArticleHomeDto dto) {
        return apArticleService.load(ArticleConstants.LOADTYPE_LOAD_MORE, dto);
    }

    @PostMapping("/loadnew")
    @ApiOperation("加载最新")
    public ResponseResult<List<ApArticle>> loadNew(@RequestBody ArticleHomeDto dto) {
        return apArticleService.load(ArticleConstants.LOADTYPE_LOAD_NEW, dto);
    }
}