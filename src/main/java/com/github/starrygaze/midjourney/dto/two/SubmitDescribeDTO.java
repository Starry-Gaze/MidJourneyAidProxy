package com.github.starrygaze.midjourney.dto.two;

import com.github.starrygaze.midjourney.dto.base.BaseSubmitDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@ApiModel("Describe提交参数")
@EqualsAndHashCode(callSuper = true)
public class SubmitDescribeDTO extends BaseSubmitDTO {

	@ApiModelProperty(value = "图片base64", required = true, example = "data:image/png;base64,xxx")
	private String base64;
}
