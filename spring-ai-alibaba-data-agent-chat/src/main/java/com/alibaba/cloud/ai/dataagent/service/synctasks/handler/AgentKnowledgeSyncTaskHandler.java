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

import com.alibaba.cloud.ai.dataagent.enums.EntityType;
import com.alibaba.cloud.ai.dataagent.enums.OperationType;
import com.alibaba.cloud.ai.dataagent.entity.SyncTasks;
import com.alibaba.cloud.ai.dataagent.service.synctasks.strategy.DeleteStrategy;
import com.alibaba.cloud.ai.dataagent.service.synctasks.strategy.VectorizeStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Agent知识同步任务处理器
 */
@Component
@Slf4j
public class AgentKnowledgeSyncTaskHandler implements SyncTaskHandler {

	private final Map<String, VectorizeStrategy> vectorizeStrategies;

	private final Map<String, DeleteStrategy> deleteStrategies;

	@Autowired
	public AgentKnowledgeSyncTaskHandler(List<VectorizeStrategy> vectorizeStrategies,
			List<DeleteStrategy> deleteStrategies) {
		this.vectorizeStrategies = vectorizeStrategies.stream()
			.collect(Collectors.toMap(VectorizeStrategy::getSupportedEntityType, Function.identity()));
		this.deleteStrategies = deleteStrategies.stream()
			.collect(Collectors.toMap(DeleteStrategy::getSupportedEntityType, Function.identity()));
	}

	@Override
	public boolean handle(SyncTasks task) {
		try {
			log.info("Start processing Agent knowledge sync task: taskId={}, entityId={}, operationType={}",
					task.getId(), task.getEntityId(), task.getOperationType());

			// 根据操作类型执行不同的处理逻辑
			if (task.getOperationType() == OperationType.EXECUTE_VECTORIZE) {
				// 执行向量化操作
				return executeVectorize(task);
			}
			else if (task.getOperationType() == OperationType.DELETE_VECTOR) {
				// 执行删除向量操作
				return deleteVector(task);
			}

			log.warn("Unsupported operation type: {}", task.getOperationType());
			return false;
		}
		catch (Exception e) {
			log.error("Failed to process Agent knowledge sync task: taskId={}", task.getId(), e);
			return false;
		}
	}

	@Override
	public String getSupportedEntityType() {
		return EntityType.AGENT_KNOWLEDGE.getValue();
	}

	/**
	 * 执行向量化操作
	 */
	private boolean executeVectorize(SyncTasks task) {
		String entityType = task.getEntityType().getValue();
		VectorizeStrategy strategy = vectorizeStrategies.get(entityType);

		if (strategy == null) {
			log.warn("No vectorize strategy found for entity type: {}", entityType);
			return false;
		}

		return strategy.vectorize(task);
	}

	/**
	 * 执行删除向量操作
	 */
	private boolean deleteVector(SyncTasks task) {
		String entityType = task.getEntityType().getValue();
		DeleteStrategy strategy = deleteStrategies.get(entityType);

		if (strategy == null) {
			log.warn("No delete strategy found for entity type: {}", entityType);
			return false;
		}

		return strategy.delete(task);
	}

}
