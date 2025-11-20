/*
 * Copyright 2024-2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.cloud.ai.dataagent.service.synctasks;

import com.alibaba.cloud.ai.dataagent.enums.EntityType;
import com.alibaba.cloud.ai.dataagent.enums.OperationType;
import com.alibaba.cloud.ai.dataagent.entity.SyncTasks;

import java.time.Duration;
import java.util.List;

/**
 * 同步任务服务接口
 */
public interface SyncTasksService {

	/**
	 * 创建同步任务
	 * @param entityType 实体类型
	 * @param entityId 实体ID
	 * @param operationType 操作类型
	 * @return 创建的同步任务ID
	 */
	Long createSyncTask(EntityType entityType, Long entityId, OperationType operationType);

	/**
	 * 原子化地查询并认领一批待处理的同步任务。 这个方法是分布式Worker的核心，它使用数据库锁来确保每个任务只被一个Worker认领。
	 * @param nodeId 当前Worker节点的唯一ID，用于写入`processing_node_id`。
	 * @param taskLimit 本次最多认领的任务数量。
	 * @return 被当前节点成功认领的任务列表。如果没有待处理任务，则返回空列表。
	 */
	List<SyncTasks> claimTasks(String nodeId, int taskLimit);

	/**
	 * 执行一个具体的同步任务。 这个方法内部会包含一个策略模式（Strategy Pattern），根据任务的`entity_type`，
	 * 将任务分发给对应的处理器（Handler）来执行。
	 * @param task 需要执行的同步任务对象。
	 */
	void executeTask(SyncTasks task);

	/**
	 * 标记一个任务为成功完成。
	 * @param taskId 需要标记的任务ID。
	 */
	void markTaskAsCompleted(Long taskId);

	/**
	 * 标记一个任务为失败，并记录错误信息和增加重试次数。
	 * @param taskId 需要标记的任务ID。
	 * @param errorMessage 具体的失败原因。
	 */
	void markTaskAsFailed(Long taskId, String errorMessage);

	/**
	 * 查找并重置那些被锁定超过一定时间的“僵尸”任务。
	 * @param timeout 超时阈值，例如 Duration.ofMinutes(15)。
	 * @return 被成功重置的任务数量。
	 */
	int resetStuckTasks(Duration timeout);

}
