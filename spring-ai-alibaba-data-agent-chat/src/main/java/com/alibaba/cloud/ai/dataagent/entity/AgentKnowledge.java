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

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Agent Knowledge Entity Class
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AgentKnowledge {

	private Integer id;

	private Integer agentId;

	private String title;

	private String content;

	private String type; // document-文档，qa-问答，faq-常见问题

	private String category; // 知识分类

	private String tags; // 标签，逗号分隔

	private String status; // 状态：active-启用，inactive-禁用

	private String sourceUrl; // 来源URL

	private String filePath; // 文件路径

	private Long fileSize; // 文件大小（字节）

	private String fileType; // 文件类型

	private String embeddingStatus; // 向量化状态：pending-待处理，processing-处理中，completed-已完成，failed-失败

	private Long creatorId; // 创建者ID

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime createTime;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime updateTime;

}

