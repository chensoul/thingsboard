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
package org.thingsboard.common.validation;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import org.apache.commons.lang3.StringUtils;
import org.thingsboard.common.validation.exception.DataValidationException;
import org.thingsboard.common.exception.ThingsboardErrorCode;
import org.thingsboard.common.exception.ThingsboardException;

public class Validator {
	/**
	 * This method validate <code>String</code> string. If string is invalid than throw
	 * <code>IncorrectParameterException</code> exception
	 *
	 * @param val          the val
	 * @param errorMessage the error message for exception
	 */
	public static void validateString(String val, String errorMessage) {
		if (val == null || val.isEmpty()) {
			throw new DataValidationException(errorMessage);
		}
	}

	/*
	 * This method validate <code>String</code> string. If string is invalid than throw
	 * <code>IncorrectParameterException</code> exception
	 *
	 * @param val                       the value
	 * @param errorMessageFunction      the error message function that apply value
	 */
	public static void validateString(String val, Function<String, String> errorMessageFunction) {
		if (val == null || val.isEmpty()) {
			throw new DataValidationException(errorMessageFunction.apply(val));
		}
	}

	/**
	 * This method validate <code>long</code> value. If value isn't positive than throw
	 * <code>IncorrectParameterException</code> exception
	 *
	 * @param val          the val
	 * @param errorMessage the error message for exception
	 */
	public static void validatePositiveNumber(long val, String errorMessage) {
		if (val <= 0) {
			throw new DataValidationException(errorMessage);
		}
	}

	/**
	 * This method validate <code>UUID</code> id. If id is null than throw
	 * <code>IncorrectParameterException</code> exception
	 *
	 * @param id           the id
	 * @param errorMessage the error message for exception
	 */
	public static void validateId(Serializable id, String errorMessage) {
		if (id == null) {
			throw new DataValidationException(errorMessage);
		}
	}

	public static void validateId(Serializable id, Function<Serializable, String> errorMessageFunction) {
		if (id == null) {
			throw new DataValidationException(errorMessageFunction.apply(id));
		}
	}

	/**
	 * This method validate list of <code>UUIDBased</code> ids. If at least one of the ids is null than throw
	 * <code>IncorrectParameterException</code> exception
	 *
	 * @param ids          the list of ids
	 * @param errorMessage the error message for exception
	 */
	public static void validateIds(List<? extends Serializable> ids, String errorMessage) {
		if (ids == null || ids.isEmpty()) {
			throw new DataValidationException(errorMessage);
		} else {
			for (Serializable id : ids) {
				validateId(id, errorMessage);
			}
		}
	}

	public static <T> T checkNotNull(T reference) throws ThingsboardException {
		return checkNotNull(reference, "Requested item wasn't found!");
	}

	public static <T> T checkNotNull(T reference, String notFoundMessage) throws ThingsboardException {
		if (reference == null) {
			throw new ThingsboardException(notFoundMessage, ThingsboardErrorCode.NOT_FOUND);
		}
		return reference;
	}

	public static <T> T checkNotNull(Optional<T> reference) throws ThingsboardException {
		return checkNotNull(reference, "Requested item wasn't found!");
	}

	public static <T> T checkNotNull(Optional<T> reference, String notFoundMessage) throws ThingsboardException {
		if (reference.isPresent()) {
			return reference.get();
		} else {
			throw new ThingsboardException(notFoundMessage, ThingsboardErrorCode.NOT_FOUND);
		}
	}

	public static void checkParameter(String name, String param) {
		if (StringUtils.isEmpty(param)) {
			throw new ThingsboardException("Parameter '" + name + "' can't be empty!", ThingsboardErrorCode.BAD_REQUEST_PARAMS);
		}
	}

	public static void checkParameter(String name, Long param) throws ThingsboardException {
		if (param == null) {
			throw new ThingsboardException("Parameter '" + name + "' can't be empty!", ThingsboardErrorCode.BAD_REQUEST_PARAMS);
		}
	}


}
