package com.alibaba.cloud.ai.dataagent.service.synctasks;

import com.alibaba.cloud.ai.dataagent.constant.EntityType;
import com.alibaba.cloud.ai.dataagent.constant.OperationType;
import com.alibaba.cloud.ai.dataagent.entity.SyncTasks;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

@Service
public class SyncTasksServiceImpl implements SyncTasksService {

	@Override
	public Long createSyncTask(EntityType entityType, Long entityId, OperationType operationType) {
		return null;
	}

	@Override
	public List<SyncTasks> claimTasks(String nodeId, int taskLimit) {
		return null;
	}

	@Override
	public void executeTask(SyncTasks task) {

	}

	@Override
	public void markTaskAsCompleted(Long taskId) {

	}

	@Override
	public void markTaskAsFailed(Long taskId, String errorMessage) {

	}

	@Override
	public int resetStuckTasks(Duration timeout) {
		return 0;
	}

}
