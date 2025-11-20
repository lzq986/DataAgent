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

import com.alibaba.cloud.ai.dataagent.entity.AgentKnowledge;
import com.alibaba.cloud.ai.dataagent.entity.SyncTasks;
import com.alibaba.cloud.ai.dataagent.enums.EntityType;
import com.alibaba.cloud.ai.dataagent.enums.KnowledgeType;
import com.alibaba.cloud.ai.dataagent.mapper.AgentKnowledgeMapper;
import com.alibaba.cloud.ai.dataagent.util.DocumentConverterUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Agent知识向量化策略
 */
@Component
@Slf4j
public class AgentKnowledgeVectorizeStrategy implements VectorizeStrategy {

	private final VectorStore vectorStore;

	private final AgentKnowledgeMapper agentKnowledgeMapper;

	private final TextSplitter textSplitter;

	public AgentKnowledgeVectorizeStrategy(VectorStore vectorStore, AgentKnowledgeMapper agentKnowledgeMapper,
			TextSplitter textSplitter) {
		this.vectorStore = vectorStore;
		this.agentKnowledgeMapper = agentKnowledgeMapper;
		this.textSplitter = textSplitter;
	}

	@Override
	public boolean vectorize(SyncTasks task) {
		// 查询AgentKnowledge
		AgentKnowledge knowledge;
		try {
			knowledge = agentKnowledgeMapper.selectById(task.getEntityId().intValue());
		}
		catch (Exception e) {
			log.error("Failed to query AgentKnowledge: taskId={}, entityId={}", task.getId(), task.getEntityId(), e);
			return false;
		}

		if (knowledge == null) {
			log.warn("AgentKnowledge not found: entityId={}", task.getEntityId());
			return false;
		}

		// 根据type判断处理逻辑
		if (KnowledgeType.QA.equals(knowledge.getType()) || KnowledgeType.FAQ.equals(knowledge.getType())) {
			return processQaKnowledge(task, knowledge);
		}
		else if (KnowledgeType.DOCUMENT.equals(knowledge.getType())) {
			return processDocumentKnowledge(task, knowledge);
		}
		else {
			log.warn("Unsupported knowledge type: {}", knowledge.getType());
			return false;
		}
	}

	/**
	 * 处理DOCUMENT类型知识
	 */
	private boolean processDocumentKnowledge(SyncTasks task, AgentKnowledge knowledge) {
		try {
			// 检查文件路径是否存在
			if (knowledge.getFilePath() == null || knowledge.getFilePath().trim().isEmpty()) {
				log.error("File path is empty for DOCUMENT knowledge: knowledgeId={}", knowledge.getId());
				return false;
			}

			// 处理文档
			List<Document> documents = processDocument(knowledge.getFilePath());
			if (documents == null || documents.isEmpty()) {
				log.warn("No documents extracted from file: knowledgeId={}, filePath={}", knowledge.getId(),
						knowledge.getFilePath());
				return false;
			}

			// 使用工具类为文档添加元数据
			List<Document> documentsWithMetadata = DocumentConverterUtil.convertDocumentsWithMetadata(documents,
					knowledge);

			// 添加到向量存储
			vectorStore.add(documentsWithMetadata);
			log.info("Successfully vectorized DOCUMENT knowledge: id={}, filePath={}, documentCount={}",
					knowledge.getId(), knowledge.getFilePath(), documentsWithMetadata.size());
			return true;
		}
		catch (Exception e) {
			log.error("Failed to process DOCUMENT knowledge: taskId={}, knowledgeId={}", task.getId(),
					knowledge.getId(), e);
			return false;
		}
	}

	private List<Document> processDocument(String filePath) {
		// 创建文件系统资源对象
		Resource resource = new FileSystemResource(filePath);

		// 使用TikaDocumentReader读取文件
		TikaDocumentReader tikaDocumentReader = new TikaDocumentReader(resource);
		List<Document> documents = tikaDocumentReader.read();

		return textSplitter.apply(documents);
	}

	/**
	 * 处理QA/FAQ类型知识
	 */
	private boolean processQaKnowledge(SyncTasks task, AgentKnowledge knowledge) {
		Document document = DocumentConverterUtil.convertQaFaqKnowledgeToDocument(knowledge);

		try {
			vectorStore.add(List.of(document));
			log.info("Successfully vectorized AgentKnowledge: id={}, type={}", knowledge.getId(), knowledge.getType());
			return true;
		}
		catch (Exception e) {
			log.error("Failed to add document to vector store: taskId={}, knowledgeId={}", task.getId(),
					knowledge.getId(), e);
			return false;
		}
	}

	@Override
	public String getSupportedEntityType() {
		return EntityType.AGENT_KNOWLEDGE.getValue();
	}

}
