/*
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

package com.alibaba.cloud.ai.dataagent.service.synctasks.handler;

import com.alibaba.cloud.ai.dataagent.entity.SyncTasks;

/**
 * 同步任务处理器接口
 */
public interface SyncTaskHandler {

	/**
	 * 处理同步任务
	 * @param task 同步任务
	 * @return 处理结果，true表示成功，false表示失败
	 */
	boolean handle(SyncTasks task);

	/**
	 * 获取支持的实体类型
	 * @return 支持的实体类型
	 */
	String getSupportedEntityType();

}
