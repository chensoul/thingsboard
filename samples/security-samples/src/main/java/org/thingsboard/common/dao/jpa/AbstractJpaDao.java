///**
// * Copyright © 2016-2024 The Thingsboard Authors
// * <p>
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// * <p>
// * http://www.apache.org/licenses/LICENSE-2.0
// * <p>
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//package org.thingsboard.common.dao;
//
//import com.google.common.collect.Lists;
//import com.google.common.util.concurrent.ListenableFuture;
//import java.io.Serializable;
//import java.util.Collection;
//import java.util.List;
//import java.util.Optional;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.transaction.annotation.Transactional;
//import org.thingsboard.common.dao.aspect.SqlDao;
//import org.thingsboard.common.entity.BaseEntity;
//import org.thingsboard.common.model.BaseData;
//
///**
// * @author Valerii Sosliuk
// */
//@Slf4j
//@SqlDao
//public abstract class AbstractJpaDao<E extends BaseEntity<? extends Serializable, D>, D extends BaseData>
//	extends AbstractDaoListeningExecutorService
//	implements Dao<D> {
//
//	protected abstract Class<E> getEntityClass();
//
//	protected abstract JpaRepository<E, Serializable> getRepository();
//
//	@Override
//	@Transactional
//	public D save(D domain) {
//		E entity;
//		try {
//			entity = getEntityClass().getConstructor(domain.getClass()).newInstance(domain);
//		} catch (Exception e) {
//			log.error("Can't create entity for domain object {}", domain, e);
//			throw new IllegalArgumentException("Can't create entity for domain object {" + domain + "}", e);
//		}
//		log.debug("Saving entity {}", entity);
//		boolean isNew = entity.getId() == null;
//		if (isNew) {
//			entity.setCreatedTime(System.currentTimeMillis());
//		}
//		entity = doSave(entity, isNew);
//		return DaoUtil.getData(entity);
//	}
//
//	protected E doSave(E entity, boolean isNew) {
//		return getRepository().save(entity);
//	}
//
//	@Override
//	public D findById(Serializable id) {
//		log.debug("Get entity by key {}", id);
//		Optional<E> entity = getRepository().findById(id);
//		return DaoUtil.getData(entity);
//	}
//
//	@Override
//	public ListenableFuture<D> findByIdAsync(Serializable key) {
//		log.debug("Get entity by key async {}", key);
//		return service.submit(() -> DaoUtil.getData(getRepository().findById(key)));
//	}
//
//	@Override
//	public boolean existsById(Serializable key) {
//		log.debug("Exists by key {}", key);
//		return getRepository().existsById(key);
//	}
//
//	@Override
//	public ListenableFuture<Boolean> existsByIdAsync(Serializable key) {
//		log.debug("Exists by key async {}", key);
//		return service.submit(() -> getRepository().existsById(key));
//	}
//
//	@Override
//	@Transactional
//	public boolean removeById(Serializable id) {
//		getRepository().deleteById(id);
//		log.debug("Remove request: {}", id);
//		return !getRepository().existsById(id);
//	}
//
//	@Transactional
//	public void removeAllByIds(Collection<Serializable> ids) {
//		JpaRepository<E, Serializable> repository = getRepository();
//		ids.forEach(repository::deleteById);
//	}
//
//	@Override
//	public List<D> find() {
//		List<E> entities = Lists.newArrayList(getRepository().findAll());
//		return DaoUtil.convertDataList(entities);
//	}
//
//}
