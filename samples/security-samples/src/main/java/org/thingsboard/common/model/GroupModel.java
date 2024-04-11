package org.thingsboard.common.model;

import java.io.Serializable;

public interface GroupModel<I extends Serializable> extends HasId<I>, HasName, TenantModel, HasMerchantId {
}
