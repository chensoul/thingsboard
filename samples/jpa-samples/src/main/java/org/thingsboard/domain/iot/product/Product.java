package org.thingsboard.domain.iot.product;

import lombok.Data;
import org.thingsboard.common.model.BaseDataWithExtra;
import org.thingsboard.common.model.HasImage;
import org.thingsboard.common.model.HasName;

/**
 * TODO Comment
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since TODO
 */
@Data
public class Product extends BaseDataWithExtra<String> implements HasImage, HasName {
	private String name;

	private String image;

	private String description;

	private String thingModelId;

	private int status;

	private Boolean isPublic;

	private AccessType accessType;

	private ProductNetType netType;

	//	@ApiModelProperty(value = "跳过生产流程")
	private Boolean hasProduction;
}
