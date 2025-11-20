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

import com.alibaba.cloud.ai.dataagent.entity.SyncTasks;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface SyncTasksMapper {

	@Insert("""
			INSERT INTO sync_tasks (entity_type, entity_id, operation_type, status, retry_count, error_message, processing_node_id, processing_start_time, created_time, updated_time)
			VALUES (#{entityType.value}, #{entityId}, #{operationType.value}, #{status.value}, #{retryCount}, #{errorMessage}, #{processingNodeId}, #{processingStartTime}, #{createdTime}, #{updatedTime})
			""")
	@Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
	int insert(SyncTasks syncTasks);

	@Select("""
			SELECT id, entity_type, entity_id, operation_type, status, retry_count, error_message,
			       processing_node_id, processing_start_time, created_time, updated_time
			FROM sync_tasks
			WHERE status = 'PENDING'
			ORDER BY created_time ASC
			LIMIT #{limit}
			FOR UPDATE SKIP LOCKED
			""")
	List<SyncTasks> selectPendingTasksForUpdate(int limit);

	@Update("""
			UPDATE sync_tasks
			SET status = 'PROCESSING',
			    processing_node_id = #{nodeId},
			    processing_start_time = NOW(),
			    updated_time = NOW()
			WHERE id = #{taskId}
			""")
	void updateTaskToProcessing(Long taskId, String nodeId);

	@Update("""
			UPDATE sync_tasks
			SET status = 'COMPLETED',
			    updated_time = NOW()
			WHERE id = #{taskId}
			""")
	void updateTaskToCompleted(Long taskId);

	@Update("""
			UPDATE sync_tasks
			SET status = 'FAILED',
			    error_message = #{errorMessage},
			    updated_time = NOW()
			WHERE id = #{taskId}
			""")
	void updateTaskToFailed(Long taskId, String errorMessage);

	@Select("""
			SELECT COUNT(*)
			FROM (
			    SELECT id
			    FROM sync_tasks
			    WHERE status = 'PROCESSING'
			      AND processing_start_time < DATE_SUB(NOW(), INTERVAL #{timeoutSeconds} SECOND)
			    FOR UPDATE SKIP LOCKED
			) AS temp
			""")
	int countStuckTasks(int timeoutSeconds);

	@Update("""
			UPDATE sync_tasks
			SET status = 'PENDING',
			    processing_node_id = NULL,
			    processing_start_time = NULL,
			    updated_time = NOW()
			WHERE id IN (
			    SELECT id FROM (
			        SELECT id
			        FROM sync_tasks
			        WHERE status = 'PROCESSING'
			          AND processing_start_time < DATE_SUB(NOW(), INTERVAL #{timeoutSeconds} SECOND)
			        ORDER BY id
			        FOR UPDATE SKIP LOCKED
			    ) AS temp
			)
			""")
	int resetStuckTasks(int timeoutSeconds);

}
