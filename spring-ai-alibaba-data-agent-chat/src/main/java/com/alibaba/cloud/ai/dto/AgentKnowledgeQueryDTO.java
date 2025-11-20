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
package com.alibaba.cloud.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class AgentKnowledgeQueryDTO {

	/**
	 * 智能体ID（必填）
	 */
	private Integer agentId;

	/**
	 * 知识标题（模糊查询）
	 */
	private String title;

	/**
	 * 知识类型：document-文档，qa-问答，faq-常见问题
	 */
	private String type;

	/**
	 * 来源URL（模糊查询）
	 */
	private String sourceUrl;

	/**
	 * 向量化状态：pending-待处理，processing-处理中，completed-已完成，failed-失败
	 */
	private String embeddingStatus;

	/**
	 * 当前页码（默认第1页）
	 */
	private Integer pageNum = 1;

	/**
	 * 每页大小（默认10条）
	 */
	private Integer pageSize = 10;

	/**
	 * 排序字段（默认按创建时间排序）
	 */
	private String sortField = "create_time";

	/**
	 * 排序方向（默认降序）
	 */
	private String sortOrder = "DESC";

}
