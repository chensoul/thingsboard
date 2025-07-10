/**
 * Copyright © 2016-2025 The Thingsboard Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.thingsboard.data.dao.jpa;


import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.thingsboard.data.model.BaseEntity;
import org.thingsboard.data.dao.aspect.SqlDao;
import org.thingsboard.common.model.BaseData;

@SqlDao
@ConditionalOnBean(EntityManager.class)
public abstract class AbstractPartitionedJpaDao<E extends BaseEntity<D, I>, D extends BaseData<I>, I> extends JpaAbstractDao<E, D, I> {
	@PersistenceContext
	private EntityManager entityManager;

	@Override
	protected E doSave(E entity, boolean isNew) {
		createPartition(entity);
		if (isNew) {
			entityManager.persist(entity);
		} else {
			entity = entityManager.merge(entity);
		}
		return entity;
	}

	public abstract void createPartition(E entity);

}
