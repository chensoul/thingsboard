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
//package org.thingsboard.common.dao.mybatis;
//
//import com.baomidou.mybatisplus.core.mapper.BaseMapper;
//import com.baomidou.mybatisplus.core.toolkit.Wrappers;
//import com.google.common.collect.Lists;
//import com.google.common.util.concurrent.ListenableFuture;
//import java.io.Serializable;
//import java.util.Collection;
//import java.util.List;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.data.domain.Pageable;
//import org.springframework.transaction.annotation.Transactional;
//import org.thingsboard.common.dao.AbstractDaoListeningExecutorService;
//import org.thingsboard.common.dao.Dao;
//import org.thingsboard.common.dao.DaoUtil;
//import org.thingsboard.common.dao.aspect.SqlDao;
//import org.thingsboard.common.dao.BaseEntity;
//import org.thingsboard.common.model.BaseData;
//import org.thingsboard.common.util.JacksonUtil;
//
//@Slf4j
//@SqlDao
//public abstract class AbstractMybatisDao<E extends BaseEntity<? extends Serializable, D>, D extends BaseData>
//	extends AbstractDaoListeningExecutorService implements Dao<D> {
//
//	protected abstract Class<E> getEntityClass();
//
//	protected abstract BaseMapper<E> getRepository();
//
//	protected com.baomidou.mybatisplus.extension.plugins.pagination.Page toMybatisPage(Pageable pageable) {
//		return new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(pageable.getPageNumber(), pageable.getPageSize());
//	}
//
//	@Override
//	public List<D> find() {
//		List<E> entities = Lists.newArrayList(getRepository().selectList(Wrappers.query()));
//		return DaoUtil.convertDataList(entities);
//	}
//
//	@Override
//	public D findById(Serializable id) {
//		log.debug("Get entity by id {}", id);
//		E entity = getRepository().selectById(id);
//		return DaoUtil.getData(entity);
//	}
//
//	@Override
//	public boolean existsById(Serializable id) {
//		log.debug("Exists by id {}", id);
//
//		return getRepository().selectById(id) != null;
//	}
//
//	@Override
//	public ListenableFuture<Boolean> existsByIdAsync(Serializable id) {
//		return service.submit(() -> existsById(id));
//	}
//
//	@Override
//	public ListenableFuture<D> findByIdAsync(Serializable id) {
//		return service.submit(() -> DaoUtil.getData(getRepository().selectById(id)));
//	}
//
//	@Override
//	@Transactional
//	public D save(D domain) {
//		E entity;
//		try {
//			entity = JacksonUtil.convertValue(domain, getEntityClass());
//		} catch (Exception e) {
//			log.error("Can't create entity for domain object {}", domain, e);
//			throw new IllegalArgumentException("Can't create entity for domain object {" + domain + "}", e);
//		}
//
//		boolean isNew = domain.getId() == null;
//		if (isNew) {
//			entity.setCreatedTime(System.currentTimeMillis());
//			entity = doSave(entity, isNew);
//			return (D) JacksonUtil.convertValue(entity, domain.getClass());
//		} else {
//			entity = doSave(entity, isNew);
//			return findById(entity.getId());
//		}
//	}
//
//	protected E doSave(E entity, boolean isNew) {
//		if (isNew) {
//			getRepository().insert(entity);
//		} else {
//			getRepository().updateById(entity);
//		}
//		return entity;
//	}
//
//	@Override
//	@Transactional
//	public boolean removeById(Serializable id) {
//		log.debug("Remove entity by id {}", id);
//
//		return getRepository().deleteById(id) > 0;
//	}
//
//	@Override
//	@Transactional
//	public void removeByIds(Collection<Serializable> ids) {
//		log.debug("Remove entity by ids {}", ids);
//
//		getRepository().deleteBatchIds(ids);
//	}
//}
