package org.thingsboard.domain.iot.product;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;

/**
 * TODO Comment
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since TODO
 */
@Data
public class ProductModel {
	//config property function event
	private JsonNode model;

	private String productId;

}
