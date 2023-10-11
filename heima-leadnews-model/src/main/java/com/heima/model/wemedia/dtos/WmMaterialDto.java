package com.heima.model.wemedia.dtos;

import com.heima.model.common.dtos.PageRequestDto;
import lombok.Data;

/**
 * @description：TODO
 * @author：dinglie
 * @date：2023/10/12 0:03
 */
@Data
public class WmMaterialDto extends PageRequestDto {
    /**
     * 1 收藏
     * 0 未收藏
     */
    private Short isCollection;
}
