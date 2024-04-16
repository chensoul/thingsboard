package org.thingsboard.domain.message;

import java.io.Serializable;
import lombok.Data;

/**
 * TODO Comment
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since TODO
 */
@Data
public class ToUsageStatsServiceMsg {
	private String tenantId;
	private Long merchantId;
	private Serializable entityId;
	private String serviceId;

	//UsageStats
}
