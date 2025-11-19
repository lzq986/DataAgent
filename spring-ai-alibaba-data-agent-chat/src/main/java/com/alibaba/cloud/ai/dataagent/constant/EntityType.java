/**
 * Copyright 2024-2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.cloud.ai.dataagent.constant;

import lombok.Getter;

/**
 * 同步任务实体类型枚举
 */
@Getter
public enum EntityType {

	AGENT_KNOWLEDGE("AGENT_KNOWLEDGE"), BUSINESS_TERM("BUSINESS_TERM");

	private final String value;

	EntityType(String value) {
		this.value = value;
	}

	public static EntityType fromValue(String value) {
		for (EntityType type : EntityType.values()) {
			if (type.value.equals(value)) {
				return type;
			}
		}
		throw new IllegalArgumentException("Unknown entity type: " + value);
	}

}
