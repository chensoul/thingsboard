package org.thingsboard.common.dao.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.Data;
import org.thingsboard.common.dao.BaseEntity;

@Data
@MappedSuperclass
public abstract class StringBaseEntity<D> implements BaseEntity<D, String> {
	//	@TableId(value = "id", type = IdType.ASSIGN_UUID)
	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(name = "id", columnDefinition = "VARCHAR(64)")
	protected String id;

	@Column(name = "created_time", updatable = false)
	protected Long createdTime;

	protected Long updatedTime;
}
