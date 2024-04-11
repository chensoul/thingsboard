package org.thingsboard.domain.role;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.thingsboard.common.model.BaseDataWithExtra;
import org.thingsboard.common.model.EntityType;
import org.thingsboard.common.model.HasMerchantId;
import org.thingsboard.common.model.HasName;
import org.thingsboard.common.model.TenantModel;
import org.thingsboard.common.validation.Length;
import org.thingsboard.common.validation.NoXss;

@Data
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Role extends BaseDataWithExtra<Long> implements HasName, TenantModel, HasMerchantId {

	private static final long serialVersionUID = 5582010124562018986L;

	public static final String ROLE_TENANT_ADMIN_NAME = "Tenant Administrator";
	public static final String ROLE_TENANT_USER_NAME = "Tenant User";
	public static final String ROLE_CUSTOMER_ADMIN_NAME = "Customer Administrator";
	public static final String ROLE_CUSTOMER_USER_NAME = "Customer User";
	public static final String ROLE_PUBLIC_USER_NAME = "Public User";
	public static final String ROLE_PUBLIC_USER_ENTITY_GROUP_NAME = "Entity Group Public User";
	public static final String ROLE_READ_ONLY_ENTITY_GROUP_NAME = "Entity Group Read-only User";
	public static final String ROLE_WRITE_ENTITY_GROUP_NAME = "Entity Group Write User";

	private String tenantId;
	private Long merchantId;
	@NoXss
	@Length
	private String name;
	private RoleType type;
	private transient JsonNode permissions;

	@Override
	public EntityType getEntityType() {
		return EntityType.ROLE;
	}
}
