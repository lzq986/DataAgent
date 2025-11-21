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

package com.alibaba.cloud.ai.dataagent.mapper;

import com.alibaba.cloud.ai.dataagent.entity.AgentKnowledge;
import com.alibaba.cloud.ai.dataagent.enums.KnowledgeType;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface AgentKnowledgeMapper {

	@Select("""

			SELECT * FROM agent_knowledge WHERE agent_id = #{agentId} AND is_deleted = 0 ORDER BY created_time DESC

			""")
	List<AgentKnowledge> selectByAgentId(@Param("agentId") Integer agentId);

	@Select("""
			SELECT * FROM agent_knowledge WHERE id = #{id} AND is_deleted = 0
			""")
	AgentKnowledge selectById(@Param("id") Integer id);

	@Insert("""

			INSERT INTO agent_knowledge (agent_id, title, content, type, question, status, source_filename, file_path, file_size, created_time, updated_time)
			VALUES (#{agentId}, #{title}, #{content}, #{type.code}, #{question}, #{status}, #{sourceFilename}, #{filePath}, #{fileSize}, #{createdTime}, #{updatedTime})

			""")
	@Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
	int insert(AgentKnowledge knowledge);

	@Update("""
			<script>
			UPDATE agent_knowledge
			<set>
				<if test="title != null">title = #{title},</if>
				<if test="content != null">content = #{content},</if>
				<if test="type != null">type = #{type},</if>
				<if test="category != null">category = #{category},</if>
				<if test="tags != null">tags = #{tags},</if>
				<if test="status != null">status = #{status},</if>
				<if test="sourceUrl != null">source_url = #{sourceUrl},</if>
				<if test="filePath != null">file_path = #{filePath},</if>
				<if test="fileSize != null">file_size = #{fileSize},</if>
				<if test="fileType != null">file_type = #{fileType},</if>
				<if test="embeddingStatus != null">embedding_status = #{embeddingStatus},</if>
				update_time = NOW()
			</set>
			WHERE id = #{id} AND is_deleted = 0
			</script>
			""")
	int update(AgentKnowledge knowledge);

	@Update("""
			UPDATE agent_knowledge SET is_deleted = 1, updated_time = NOW() WHERE id = #{id}
			""")
	int deleteById(@Param("id") Integer id);

	@Select("""
			SELECT * FROM agent_knowledge WHERE agent_id = #{agentId} AND type = #{type.code} AND is_deleted = 0 ORDER BY created_time DESC

			""")
	List<AgentKnowledge> selectByAgentIdAndType(@Param("agentId") Integer agentId, @Param("type") KnowledgeType type);

	@Select("""

			SELECT * FROM agent_knowledge WHERE agent_id = #{agentId} AND status = #{status} AND is_deleted = 0 ORDER BY created_time DESC

			""")
	List<AgentKnowledge> selectByAgentIdAndStatus(@Param("agentId") Integer agentId, @Param("status") String status);

	@Select("""

			SELECT * FROM agent_knowledge WHERE agent_id = #{agentId} AND is_deleted = 0 AND
			(title LIKE CONCAT('%', #{keyword}, '%') OR content LIKE CONCAT('%', #{keyword}, '%') OR question LIKE CONCAT('%', #{keyword}, '%'))
			ORDER BY created_time DESC

			""")
	List<AgentKnowledge> searchByAgentIdAndKeyword(@Param("agentId") Integer agentId, @Param("keyword") String keyword);

	@Update("""

			UPDATE agent_knowledge SET status = #{status}, updated_time = #{now} WHERE id = #{id} AND is_deleted = 0

			""")
	int updateStatus(@Param("id") Integer id, @Param("status") String status, @Param("now") LocalDateTime now);

	@Select("""
			SELECT COUNT(*) FROM agent_knowledge WHERE agent_id = #{agentId} AND is_deleted = 0
			""")
	int countByAgentId(@Param("agentId") Integer agentId);

	@Select("""
			SELECT type, COUNT(*) as count FROM agent_knowledge WHERE agent_id = #{agentId} AND is_deleted = 0 GROUP BY type
			""")
	List<Object[]> countByType(@Param("agentId") Integer agentId);

	@Select("""
			<script>
			SELECT * FROM agent_knowledge
			WHERE agent_id = #{agentId}
			<if test="title != null and title != ''">
				AND title LIKE CONCAT('%', #{title}, '%')
			</if>
			<if test="type != null and type != ''">
				AND type = #{type}
			</if>
			<if test="sourceUrl != null and sourceUrl != ''">
				AND source_url LIKE CONCAT('%', #{sourceUrl}, '%')
			</if>
			<if test="embeddingStatus != null and embeddingStatus != ''">
				AND embedding_status = #{embeddingStatus}
			</if>
			ORDER BY ${sortField} ${sortOrder}
			LIMIT #{offset}, #{pageSize}
			</script>
			""")
	List<AgentKnowledge> selectByConditionsWithPage(@Param("agentId") Integer agentId, @Param("title") String title,
			@Param("type") String type, @Param("sourceUrl") String sourceUrl,
			@Param("embeddingStatus") String embeddingStatus, @Param("sortField") String sortField,
			@Param("sortOrder") String sortOrder, @Param("offset") Integer offset, @Param("pageSize") Integer pageSize);

	@Select("""
			<script>
			SELECT COUNT(*) FROM agent_knowledge
			WHERE agent_id = #{agentId}
			<if test="title != null and title != ''">
				AND title LIKE CONCAT('%', #{title}, '%')
			</if>
			<if test="type != null and type != ''">
				AND type = #{type}
			</if>
			<if test="sourceUrl != null and sourceUrl != ''">
				AND source_url LIKE CONCAT('%', #{sourceUrl}, '%')
			</if>
			<if test="embeddingStatus != null and embeddingStatus != ''">
				AND embedding_status = #{embeddingStatus}
			</if>
			</script>
			""")
	Long countByConditions(@Param("agentId") Integer agentId, @Param("title") String title, @Param("type") String type,
			@Param("sourceUrl") String sourceUrl, @Param("embeddingStatus") String embeddingStatus);

}
