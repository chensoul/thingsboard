/**
 * Copyright © 2016-2024 The Thingsboard Authors
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.thingsboard.common.dao;

import com.google.common.util.concurrent.ListenableFuture;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import org.thingsboard.common.model.EntityType;

public interface Dao<D> {

	D findById(Serializable id);

	List<D> find();

	ListenableFuture<D> findByIdAsync(Serializable id);

	boolean existsById(Serializable id);

	ListenableFuture<Boolean> existsByIdAsync(Serializable id);

	D save(D t);

	boolean removeById(Serializable id);

	void removeByIds(Collection<Serializable> ids);

	default EntityType getEntityType() {
		return null;
	}
}
