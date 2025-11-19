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

package com.alibaba.cloud.ai.dataagent.entity;

import com.alibaba.cloud.ai.dataagent.constant.EntityType;
import com.alibaba.cloud.ai.dataagent.constant.OperationType;
import com.alibaba.cloud.ai.dataagent.constant.TaskStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Sync Tasks Entity Class
 */
@Data
@Builder
@NoArgsConstructor
public class SyncTasks {

	private Long id;

	private EntityType entityType; // 实体类型: AGENT_KNOWLEDGE, BUSINESS_TERM

	private Long entityId; // 对应实体在各自表中的主键ID

	private OperationType operationType; // 操作类型: EXECUTE_VECTORIZE, DELETE_VECTOR

	@Builder.Default
	private TaskStatus status = TaskStatus.PENDING; // 任务状态: PENDING, PROCESSING,
													// COMPLETED, FAILED

	@Builder.Default
	private Integer retryCount = 0;

	private String errorMessage;

	private String processingNodeId; // 正在处理此任务的节点ID (用于分布式锁定)

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime processingStartTime; // 开始处理任务的时间 (用于识别僵尸任务)

	@Builder.Default
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime createdTime = LocalDateTime.now();

	@Builder.Default
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime updatedTime = LocalDateTime.now();

	/**
	 * 全参数构造函数
	 */
	public SyncTasks(Long id, EntityType entityType, Long entityId, OperationType operationType, TaskStatus status,
			Integer retryCount, String errorMessage, String processingNodeId, LocalDateTime processingStartTime,
			LocalDateTime createdTime, LocalDateTime updatedTime) {
		this.id = id;
		this.entityType = entityType;
		this.entityId = entityId;
		this.operationType = operationType;
		this.status = status;
		this.retryCount = retryCount;
		this.errorMessage = errorMessage;
		this.processingNodeId = processingNodeId;
		this.processingStartTime = processingStartTime;
		this.createdTime = createdTime;
		this.updatedTime = updatedTime;
	}

}
