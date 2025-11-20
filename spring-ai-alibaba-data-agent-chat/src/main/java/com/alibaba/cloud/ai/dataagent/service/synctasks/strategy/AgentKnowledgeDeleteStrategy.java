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

package com.alibaba.cloud.ai.dataagent.service.synctasks.strategy;

import com.alibaba.cloud.ai.dataagent.constant.Constant;
import com.alibaba.cloud.ai.dataagent.constant.DocumentMetadataConstant;
import com.alibaba.cloud.ai.dataagent.entity.AgentKnowledge;
import com.alibaba.cloud.ai.dataagent.entity.SyncTasks;
import com.alibaba.cloud.ai.dataagent.enums.EntityType;
import com.alibaba.cloud.ai.dataagent.enums.KnowledgeType;
import com.alibaba.cloud.ai.dataagent.mapper.AgentKnowledgeMapper;
import com.alibaba.cloud.ai.dataagent.service.vectorstore.AgentVectorStoreService;
import com.alibaba.cloud.ai.dataagent.util.DocumentConverterUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Agent知识删除向量策略
 */
@Component
@Slf4j
public class AgentKnowledgeDeleteStrategy implements DeleteStrategy {

	private final VectorStore vectorStore;

	private final AgentVectorStoreService agentVectorStoreService;

	private final AgentKnowledgeMapper agentKnowledgeMapper;

	public AgentKnowledgeDeleteStrategy(VectorStore vectorStore, AgentVectorStoreService agentVectorStoreService,
			AgentKnowledgeMapper agentKnowledgeMapper) {
		this.vectorStore = vectorStore;
		this.agentVectorStoreService = agentVectorStoreService;
		this.agentKnowledgeMapper = agentKnowledgeMapper;
	}

	@Override
	public boolean delete(SyncTasks task) {
		try {
			// 查询AgentKnowledge
			AgentKnowledge knowledge = agentKnowledgeMapper.selectById(task.getEntityId().intValue());
			if (knowledge == null) {
				log.warn("AgentKnowledge not found: entityId={}", task.getEntityId());
				return false;
			}

			if (KnowledgeType.QA.equals(knowledge.getType()) || KnowledgeType.FAQ.equals(knowledge.getType())) {
				String docId = DocumentConverterUtil.generateFixedQaFaqKnowledgeDocId(knowledge);
				agentVectorStoreService.deleteDocumentsByIds(List.of(docId));
			}
			else if (KnowledgeType.DOCUMENT.equals(knowledge.getType())) {
				// 通过元数据删除向量
				Map<String, Object> metadata = new HashMap<>();
				metadata.put(Constant.AGENT_ID, knowledge.getAgentId().toString());
				metadata.put(DocumentMetadataConstant.KNOWLEDGE_ID, knowledge.getId().toString());
				metadata.put(DocumentMetadataConstant.VECTOR_TYPE, DocumentMetadataConstant.AGENT_KNOWLEDGE);
				agentVectorStoreService.deleteDocumentsByMetedata(knowledge.getAgentId().toString(), metadata);
			}
			else {
				log.warn("Unsupported knowledge type: {}", knowledge.getType());
				return false;
			}

			log.info("Successfully deleted vector: id={}, type={}", knowledge.getId(), knowledge.getType());
			return true;
		}
		catch (Exception e) {
			log.error("Failed to delete vector: taskId={}, entityId={}", task.getId(), task.getEntityId(), e);
			return false;
		}
	}

	@Override
	public String getSupportedEntityType() {
		return EntityType.AGENT_KNOWLEDGE.getValue();
	}

}
