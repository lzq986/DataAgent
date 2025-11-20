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

package com.alibaba.cloud.ai.dataagent.service.synctasks;

import com.alibaba.cloud.ai.dataagent.enums.EntityType;
import com.alibaba.cloud.ai.dataagent.enums.OperationType;
import com.alibaba.cloud.ai.dataagent.enums.TaskStatus;
import com.alibaba.cloud.ai.dataagent.entity.SyncTasks;
import com.alibaba.cloud.ai.dataagent.mapper.SyncTasksMapper;
import com.alibaba.cloud.ai.dataagent.service.synctasks.handler.SyncTaskHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class SyncTasksServiceImpl implements SyncTasksService {

	private final SyncTasksMapper syncTasksMapper;

	// 使用策略模式，注入所有的任务处理器
	private final List<SyncTaskHandler> taskHandlers;

	// 实体类型到处理器的映射缓存
	private final Map<String, SyncTaskHandler> handlerMap = new HashMap<>();

	public SyncTasksServiceImpl(SyncTasksMapper syncTasksMapper, List<SyncTaskHandler> taskHandlers) {
		this.syncTasksMapper = syncTasksMapper;
		this.taskHandlers = taskHandlers;
	}

	@Override
	public Long createSyncTask(EntityType entityType, Long entityId, OperationType operationType) {
		// 创建同步任务对象
		SyncTasks syncTask = SyncTasks.builder()
			.entityType(entityType)
			.entityId(entityId)
			.operationType(operationType)
			.status(TaskStatus.PENDING)
			.retryCount(0)
			.createdTime(LocalDateTime.now())
			.updatedTime(LocalDateTime.now())
			.build();

		// 插入数据库并返回生成的ID
		int result = syncTasksMapper.insert(syncTask);
		if (result > 0) {
			return syncTask.getId();
		}
		log.error("Failed to create sync task");
		return null;
	}

	@Override
	public List<SyncTasks> claimTasks(String nodeId, int taskLimit) {
		// 查询待处理任务，使用 FOR UPDATE SKIP LOCKED 确保并发安全
		List<SyncTasks> pendingTasks = syncTasksMapper.selectPendingTasksForUpdate(taskLimit);

		// 如果没有待处理任务，直接返回空列表
		if (pendingTasks == null || pendingTasks.isEmpty()) {
			return Collections.emptyList();
		}

		// 将查询到的任务状态更新为 PROCESSING，并设置处理节点ID
		for (SyncTasks task : pendingTasks) {
			syncTasksMapper.updateTaskToProcessing(task.getId(), nodeId);
			// 更新本地对象状态，保持一致性
			task.setStatus(TaskStatus.PROCESSING);
			task.setProcessingNodeId(nodeId);
			task.setProcessingStartTime(LocalDateTime.now());
		}

		return pendingTasks;
	}

	@Override
	public void executeTask(SyncTasks task) {
		if (task == null) {
			log.error("Task cannot be null");
			return;
		}

		try {
			log.info("Starting to execute sync task: taskId={}, entityType={}, entityId={}, operationType={}",
					task.getId(), task.getEntityType(), task.getEntityId(), task.getOperationType());

			// 获取对应的任务处理器
			SyncTaskHandler handler = getTaskHandler(task.getEntityType().getValue());
			if (handler == null) {
				log.error("No task handler found for entity type: {}", task.getEntityType().getValue());
				markTaskAsFailed(task.getId(), "No supported task handler found");
				return;
			}

			// 执行任务
			boolean success = handler.handle(task);

			// 根据执行结果更新任务状态
			if (success) {
				markTaskAsCompleted(task.getId());
				log.info("Task executed successfully: taskId={}", task.getId());
			}
			else {
				markTaskAsFailed(task.getId(), "Task processing failed");
				log.error("Task execution failed: taskId={}", task.getId());
			}
		}
		catch (Exception e) {
			log.error("Exception occurred while executing task: taskId={}", task.getId(), e);
			markTaskAsFailed(task.getId(), "Task execution exception: " + e.getMessage());
		}
	}

	/**
	 * 根据实体类型获取对应的任务处理器
	 */
	private SyncTaskHandler getTaskHandler(String entityType) {
		if (handlerMap.isEmpty()) {
			for (SyncTaskHandler handler : taskHandlers) {
				handlerMap.put(handler.getSupportedEntityType(), handler);
			}
		}

		return handlerMap.get(entityType);
	}

	@Override
	public void markTaskAsCompleted(Long taskId) {
		try {
			syncTasksMapper.updateTaskToCompleted(taskId);
			log.info("Task marked as completed: taskId={}", taskId);
		}
		catch (Exception e) {
			log.error("Exception occurred while marking task as completed: taskId={}", taskId, e);
		}
	}

	@Override
	public void markTaskAsFailed(Long taskId, String errorMessage) {
		try {
			syncTasksMapper.updateTaskToFailed(taskId, errorMessage);
			log.info("Task marked as failed: taskId={}, errorMessage={}", taskId, errorMessage);
		}
		catch (Exception e) {
			log.error("Exception occurred while marking task as failed: taskId={}", taskId, e);
		}
	}

	@Override
	public int resetStuckTasks(Duration timeout) {
		// 实现重置超时任务的逻辑
		// 1. 查询所有状态为PROCESSING且处理开始时间早于timeout的任务
		// 2. 将这些任务的状态重置为PENDING，清除处理节点ID和处理开始时间
		// 3. 返回重置的任务数量

		log.info("Resetting stuck tasks: timeout={}", timeout);

		try {
			// 将Duration转换为秒数
			int timeoutSeconds = (int) timeout.getSeconds();

			// 先统计超时任务数量
			int stuckTaskCount = syncTasksMapper.countStuckTasks(timeoutSeconds);

			if (stuckTaskCount > 0) {
				log.info("Found {} stuck tasks, resetting to PENDING status", stuckTaskCount);

				// 重置超时任务
				int resetCount = syncTasksMapper.resetStuckTasks(timeoutSeconds);

				log.info("Successfully reset {} stuck tasks to PENDING status", resetCount);
				return resetCount;
			}
			else {
				log.info("No stuck tasks found");
				return 0;
			}
		}
		catch (Exception e) {
			log.error("Error while resetting stuck tasks", e);
			return 0;
		}
	}

}
