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

import com.flowlong.bpm.engine.model.ProcessModel;
import com.flowlong.bpm.engine.model.TaskModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import test.mysql.MysqlTest;

/**
 * 简单测试获取流程field
 *
 * @author lipanre
 */
public class TestField extends MysqlTest {

    @BeforeEach
    public void before() {
        processId = this.deployByResource("test/task/field.long");
    }

    @Test
    public void testField() {
        ProcessModel processModel = flowLongEngine.processService().getProcessById(processId).getProcessModel();
        // 获取名称为task1的task
        TaskModel taskModel = (TaskModel) processModel.getNode("task1");

        // 获取task下面的field
        System.out.println("taskModel.getFields() : " + taskModel.getFields());
    }

}