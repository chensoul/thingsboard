package org.thingsboard.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

@Data
public abstract class StringBaseEntity<D> implements BaseEntity<String, D> {
	@TableId(value = "id", type = IdType.ASSIGN_UUID)
	protected String id;

	protected Long createdTime;

	protected Long updatedTime;
}
