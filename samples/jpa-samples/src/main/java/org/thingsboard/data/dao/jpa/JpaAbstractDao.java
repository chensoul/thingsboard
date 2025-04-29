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

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;
import org.thingsboard.common.model.BaseData;
import org.thingsboard.common.util.JacksonUtil;
import org.thingsboard.data.dao.AbstractDaoExecutorService;
import org.thingsboard.data.dao.Dao;
import org.thingsboard.data.dao.DaoUtil;
import org.thingsboard.data.dao.aspect.SqlDao;
import org.thingsboard.data.model.BaseEntity;

@Slf4j
@SqlDao
public abstract class JpaAbstractDao<E extends BaseEntity<D, I>, D extends BaseData<I>, I>
	extends AbstractDaoExecutorService
	implements Dao<D, I> {

	protected abstract Class<E> getEntityClass();

	protected abstract JpaRepository<E, I> getRepository();

	@Override
	@Transactional
	public D save(D domain) {
		E entity;
		try {
			entity = JacksonUtil.convertValue(domain, getEntityClass());
		} catch (Exception e) {
			throw new IllegalArgumentException("Can't create entity for domain object {" + domain + "}", e);
		}
		log.debug("Saving entity {}", entity);
		boolean isNew = entity.getId() == null;
		if (isNew) {
			entity.setCreatedTime(System.currentTimeMillis());
		}
		entity = doSave(entity, isNew);
		return DaoUtil.getData(entity);
	}

	protected E doSave(E entity, boolean isNew) {
		return getRepository().saveAndFlush(entity);
	}

	@Override
	public D findById(I id) {
		log.debug("Get entity by key {}", id);
		Optional<E> entity = getRepository().findById(id);
		return DaoUtil.getData(entity);
	}

	@Override
	public ListenableFuture<D> findByIdAsync(I key) {
		log.debug("Get entity by key async {}", key);
		return service.submit(() -> DaoUtil.getData(getRepository().findById(key)));
	}

	@Override
	public boolean existsById(I key) {
		log.debug("Exists by key {}", key);
		return getRepository().existsById(key);
	}

	@Override
	public ListenableFuture<Boolean> existsByIdAsync(I key) {
		log.debug("Exists by key async {}", key);
		return service.submit(() -> getRepository().existsById(key));
	}

	@Override
	@Transactional
	public boolean removeById(I id) {
		getRepository().deleteById(id);
		log.debug("Remove request: {}", id);
		return !getRepository().existsById(id);
	}

	@Transactional
	public void removeByIds(Collection<I> ids) {
		JpaRepository<E, I> repository = getRepository();
		ids.forEach(repository::deleteById);
	}

	@Override
	public List<D> find() {
		List<E> entities = Lists.newArrayList(getRepository().findAll());
		return DaoUtil.convertDataList(entities);
	}

}
