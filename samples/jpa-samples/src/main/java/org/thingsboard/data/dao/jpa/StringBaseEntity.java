package org.thingsboard.data.dao.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.Data;
import org.thingsboard.data.model.BaseEntity;

@Data
@MappedSuperclass
public abstract class StringBaseEntity<D> implements BaseEntity<D, String> {
	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(name = "id", columnDefinition = "VARCHAR(64)")
	protected String id;

	@Column(name = "created_time", updatable = false)
	protected Long createdTime;

	protected Long updatedTime;
}
