package com.heima.wemedia.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.wemedia.dtos.WmMaterialDto;
import com.heima.model.wemedia.pojos.WmMaterial;
import org.springframework.web.multipart.MultipartFile;

/**
 * @description：TODO
 * @author：dinglie
 * @date：2023/10/10 23:49
 */
public interface WmMaterialService extends IService<WmMaterial> {
    ResponseResult uploadPicture(MultipartFile multipartFile);

    /**
     * 素材列表查询
     * @param dto
     * @return
     */
    ResponseResult findList( WmMaterialDto dto);
}
