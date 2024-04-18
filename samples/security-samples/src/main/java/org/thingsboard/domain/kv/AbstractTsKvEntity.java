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
package org.thingsboard.domain.kv;

import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.Data;
import org.springframework.data.annotation.Transient;
import org.thingsboard.common.model.ToData;

@Data
@MappedSuperclass
public abstract class AbstractTsKvEntity implements ToData<TsKvEntry> {

	protected static final String SUM = "SUM";
	protected static final String AVG = "AVG";
	protected static final String MIN = "MIN";
	protected static final String MAX = "MAX";

	@Id
	protected String entityId;

	@Id
	protected int key;

	protected Long ts;

	protected Boolean booleanValue;

	protected String strValue;

	protected Long longValue;

	protected Double doubleValue;

	protected String jsonValue;

	@Transient
	protected String strKey;
	@Transient
	protected Long aggValuesLastTs;
	@Transient
	protected Long aggValuesCount;

	public AbstractTsKvEntity() {
	}

	public AbstractTsKvEntity(Long aggValuesLastTs) {
		this.aggValuesLastTs = aggValuesLastTs;
	}

	public abstract boolean isNotEmpty();

	protected static boolean isAllNull(Object... args) {
		for (Object arg : args) {
			if (arg != null) {
				return false;
			}
		}
		return true;
	}

	@Override
	public TsKvEntry toData() {
		KvEntry kvEntry = null;
		if (strValue != null) {
			kvEntry = new StringDataEntry(strKey, strValue);
		} else if (longValue != null) {
			kvEntry = new LongDataEntry(strKey, longValue);
		} else if (doubleValue != null) {
			kvEntry = new DoubleDataEntry(strKey, doubleValue);
		} else if (booleanValue != null) {
			kvEntry = new BooleanDataEntry(strKey, booleanValue);
		} else if (jsonValue != null) {
			kvEntry = new JsonDataEntry(strKey, jsonValue);
		}

		if (aggValuesCount == null) {
			return new BasicTsKvEntry(ts, kvEntry);
		} else {
			return new AggTsKvEntry(ts, kvEntry, aggValuesCount);
		}
	}

}
