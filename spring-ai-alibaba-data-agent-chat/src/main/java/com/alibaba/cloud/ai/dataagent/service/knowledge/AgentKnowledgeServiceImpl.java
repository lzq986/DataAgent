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

package com.alibaba.cloud.ai.dataagent.service.knowledge;

import com.alibaba.cloud.ai.dataagent.converter.AgentKnowledgeConverter;
import com.alibaba.cloud.ai.dataagent.dto.agentknowledge.AgentKnowledgeQueryDTO;
import com.alibaba.cloud.ai.dataagent.dto.agentknowledge.CreateKnowledgeDto;
import com.alibaba.cloud.ai.dataagent.dto.PageResult;
import com.alibaba.cloud.ai.dataagent.dto.agentknowledge.UpdateKnowledgeDto;
import com.alibaba.cloud.ai.dataagent.entity.AgentKnowledge;
import com.alibaba.cloud.ai.dataagent.enums.EmbeddingStatus;
import com.alibaba.cloud.ai.dataagent.enums.KnowledgeType;
import com.alibaba.cloud.ai.dataagent.mapper.AgentKnowledgeMapper;
import com.alibaba.cloud.ai.dataagent.service.file.FileStorageService;
import com.alibaba.cloud.ai.dataagent.vo.AgentKnowledgeVO;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

@Slf4j
@Service
@AllArgsConstructor
public class AgentKnowledgeServiceImpl implements AgentKnowledgeService {

	private static final String AGENT_KNOWLEDGE_FILE_PATH = "agent-knowledge";

	private final AgentKnowledgeMapper agentKnowledgeMapper;

	private final AgentKnowledgeResourceManager agentKnowledgeResourceManager;

	private final ExecutorService executorService;

	private final FileStorageService fileStorageService;

	private final AgentKnowledgeConverter agentKnowledgeConverter;

	@Override
	public AgentKnowledge getKnowledgeById(Integer id) {
		return agentKnowledgeMapper.selectById(id);
	}

	@Override
	public AgentKnowledgeVO createKnowledge(CreateKnowledgeDto createKnowledgeDto) {
		String storagePath = null;
		checkCreateKnowledgeDto(createKnowledgeDto);

		if (createKnowledgeDto.getType().equals(KnowledgeType.DOCUMENT.getCode())) {
			// 将文件保存到磁盘
			try {
				storagePath = fileStorageService.storeFile(createKnowledgeDto.getFile(), AGENT_KNOWLEDGE_FILE_PATH);
			}
			catch (Exception e) {
				log.error("Failed to store file, agentId:{} title:{} type:{} ", createKnowledgeDto.getAgentId(),
						createKnowledgeDto.getTitle(), createKnowledgeDto.getType());
				throw new RuntimeException("Failed to store file.");
			}
		}

		AgentKnowledge knowledge = agentKnowledgeConverter.toEntityForCreate(createKnowledgeDto, storagePath);

		if (agentKnowledgeMapper.insert(knowledge) <= 0) {
			log.error("Failed to create knowledge, agentId:{} title:{} type:{} ", knowledge.getAgentId(),
					knowledge.getTitle(), knowledge.getType());
			throw new RuntimeException("Failed to create knowledge in database.");
		}

		knowledge.setEmbeddingStatus(EmbeddingStatus.PROCESSING);
		knowledge.setUpdatedTime(LocalDateTime.now());
		CompletableFuture.runAsync(() -> {
			try {
				// 更新状态为处理中
				agentKnowledgeMapper.update(knowledge);

				// 执行向量库操作
				agentKnowledgeResourceManager.doEmbedingToVectorStore(knowledge);

				// 更新状态为已完成
				knowledge.setEmbeddingStatus(EmbeddingStatus.COMPLETED);
				knowledge.setUpdatedTime(LocalDateTime.now());
				agentKnowledgeMapper.update(knowledge);

				log.info("Successfully embedded knowledge to vector store, knowledgeId: {}", knowledge.getId());
			}
			catch (Exception e) {
				// 更新状态为失败，并记录错误信息
				knowledge.setEmbeddingStatus(EmbeddingStatus.FAILED);
				knowledge.setErrorMsg(e.getMessage());
				knowledge.setUpdatedTime(LocalDateTime.now());
				agentKnowledgeMapper.update(knowledge);

				log.error("Failed to embed knowledge to vector store, knowledgeId: {}", knowledge.getId(), e);
			}
		}, executorService);

		return agentKnowledgeConverter.toVo(knowledge);
	}

	private static void checkCreateKnowledgeDto(CreateKnowledgeDto createKnowledgeDto) {
		if (createKnowledgeDto.getType().equals(KnowledgeType.DOCUMENT.getCode())
				&& createKnowledgeDto.getFile() == null) {
			throw new RuntimeException("File is required for document type.");
		}
		if (createKnowledgeDto.getType().equals(KnowledgeType.QA.getCode())
				|| createKnowledgeDto.getType().equals(KnowledgeType.FAQ.getCode())) {

			if (!StringUtils.hasText(createKnowledgeDto.getQuestion())) {
				throw new RuntimeException("Question is required for QA or FAQ type.");
			}
			if (!StringUtils.hasText(createKnowledgeDto.getContent())) {
				throw new RuntimeException("Content is required for QA or FAQ type.");
			}

		}
	}

	@Override
	@Transactional
	public AgentKnowledgeVO updateKnowledge(Integer id, UpdateKnowledgeDto updateKnowledgeDto) {
		// 基础校验：根据 id 查询数据库
		AgentKnowledge existingKnowledge = agentKnowledgeMapper.selectById(id);
		if (existingKnowledge == null) {
			log.warn("Knowledge not found with id: {}", id);
			throw new RuntimeException("Knowledge not found.");
		}

		if (StringUtils.hasText(updateKnowledgeDto.getTitle()))
			existingKnowledge.setTitle(updateKnowledgeDto.getTitle());

		// content
		if (StringUtils.hasText(updateKnowledgeDto.getContent()))
			existingKnowledge.setContent(updateKnowledgeDto.getContent());

		// 更新数据库
		int updateResult = agentKnowledgeMapper.update(existingKnowledge);
		if (updateResult <= 0) {
			log.error("Failed to update knowledge with id: {}", existingKnowledge.getId());
			throw new RuntimeException("Failed to update knowledge in database.");
		}
		return agentKnowledgeConverter.toVo(existingKnowledge);
	}

	@Override
	@Transactional
	public boolean deleteKnowledge(Integer id) {
		// 先获取知识信息，用于后续删除文件和向量数据
		AgentKnowledge knowledge = agentKnowledgeMapper.selectById(id);
		if (knowledge == null) {
			log.warn("Knowledge not found with id: {}, treating as already deleted", id);
			return true;
		}

		// 同步执行软删除
		knowledge.setIsDeleted(1);
		knowledge.setIsResourceCleaned(0);
		knowledge.setUpdatedTime(LocalDateTime.now());
		agentKnowledgeMapper.update(knowledge);

		// 异步删除相关资源
		CompletableFuture.runAsync(() -> {
			boolean vectorDeleted;
			boolean fileDeleted;

			try {
				Long agentId = Long.valueOf(knowledge.getAgentId());

				// 删除向量
				vectorDeleted = agentKnowledgeResourceManager.deleteFromVectorStore(agentId, id);

				// 删除文件
				fileDeleted = agentKnowledgeResourceManager.deleteKnowledgeFile(knowledge);

				if (!vectorDeleted || !fileDeleted) {
					log.error("Knowledge soft deleted but resources clean up failed. ID: {}, Vector: {}, File: {}", id,
							vectorDeleted, fileDeleted);
				}
				else {
					knowledge.setIsResourceCleaned(1);
					agentKnowledgeMapper.update(knowledge);
					log.info("Knowledge resources cleaned up successfully. ID: {}", id);
				}
			}
			catch (Exception e) {
				log.error("Exception during async resource cleanup for knowledgeId: {}", id, e);
			}
		}, executorService);

		// 立即返回true，表示删除请求已接受
		return true;
	}

	@Override
	public PageResult<AgentKnowledgeVO> queryByConditionsWithPage(AgentKnowledgeQueryDTO queryDTO) {

		int offset = (queryDTO.getPageNum() - 1) * queryDTO.getPageSize();

		Long total = agentKnowledgeMapper.countByConditions(queryDTO);

		List<AgentKnowledge> dataList = agentKnowledgeMapper.selectByConditionsWithPage(queryDTO, offset);
		List<AgentKnowledgeVO> dataListVO = dataList.stream().map(agentKnowledgeConverter::toVo).toList();
		PageResult<AgentKnowledgeVO> pageResult = new PageResult<>();
		pageResult.setData(dataListVO);
		pageResult.setTotal(total);
		pageResult.setPageNum(queryDTO.getPageNum());
		pageResult.setPageSize(queryDTO.getPageSize());
		pageResult.calculateTotalPages();

		return pageResult;
	}

	@Override
	public AgentKnowledgeVO updateKnowledgeRecallStatus(Integer id, Integer recalled) {
		// 查询知识
		AgentKnowledge knowledge = agentKnowledgeMapper.selectById(id);
		if (knowledge == null) {
			throw new RuntimeException("Knowledge not found.");
		}

		// 更新召回状态
		knowledge.setIsRecall(recalled);

		// 更新数据库
		boolean res = agentKnowledgeMapper.update(knowledge) > 0;
		if (!res) {
			log.error("Failed to update knowledge with id: {}", knowledge.getId());
			throw new RuntimeException("Failed to update knowledge in database.");
		}
		return agentKnowledgeConverter.toVo(knowledge);
	}

}
