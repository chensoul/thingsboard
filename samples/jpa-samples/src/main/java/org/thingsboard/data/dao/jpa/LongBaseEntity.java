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
public abstract class LongBaseEntity<D> implements BaseEntity<D, Long> {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", columnDefinition = "bigint")
	protected Long id;

	@Column(name = "created_time", updatable = false)
	protected Long createdTime;

	protected Long updatedTime;
}
