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

import com.alibaba.cloud.ai.dataagent.enums.KnowledgeType;
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

	private KnowledgeType type; // DOCUMENT, QA, FAQ

	private String question; // 仅当type为QA或FAQ时使用

	private String content; // 对于QA/FAQ是答案; 对于DOCUMENT, 此字段通常为空

	private Integer isRecall; // 业务状态: 1=召回, 0=非召回

	private String sourceFilename; // 上传时的原始文件名

	private String filePath; // 文件在服务器上的物理存储路径

	private Long fileSize; // 文件大小 (字节)

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime createdTime;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime updatedTime;

	private Integer isDeleted;// 0=未删除, 1=已删除

}
