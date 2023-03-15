/* Copyright 2023-2025 jobob@qq.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package test.mysql.task;

import com.flowlong.bpm.engine.entity.Instance;
import com.flowlong.bpm.engine.entity.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import test.mysql.MysqlTest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 拒绝任务测试类
 *
 * <p>
 * 尊重知识产权，CV 请保留版权，爱组搭 http://aizuda.com 出品
 * </p>
 *
 * @author fanco
 * @since 1.0
 */
public class TestReject extends MysqlTest {
	@BeforeEach
	public void before() {
		processId = this.deployByResource("test/task/reject.long");
	}
	
	@Test
	public void test() {
		Map<String, Object> args = new HashMap<String, Object>();
		args.put("number", 2);
		Instance instance = flowLongEngine.startInstanceById(processId, "creator");
		List<Task> tasks = flowLongEngine.queryService().getActiveTasksByInstanceId(instance.getId());
		Task nextTask = flowLongEngine.executeTask(tasks.get(0).getId(), "creator", args).get(0);
		flowLongEngine.executeAndJumpTask(nextTask.getId(), "creator", args, "task1");
	}
}