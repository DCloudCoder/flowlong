/*
 * Copyright 2023-2025 Licensed under the AGPL License
 */
package test.mysql;

import com.aizuda.bpm.engine.QueryService;
import com.aizuda.bpm.engine.TaskService;
import com.aizuda.bpm.engine.core.FlowCreator;
import com.aizuda.bpm.engine.entity.FlwHisTask;
import com.aizuda.bpm.engine.entity.FlwTask;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

/**
 * 测试简单流程
 *
 * @author xdg
 */
@Slf4j
public class TestPurchase extends MysqlTest {

    FlowCreator creator = FlowCreator.of("XJHXYY127", "1101", "doudou");

    @BeforeEach
    public void before() {
        processId = this.deployByResource("test/purchase.json", testCreator);
    }

    /**
     * 豆豆发起申请
     */
    @Test
    public void test1() {
        Map<String, Object> args = new HashMap<>();
        args.put("deptId", 1001);
        args.put("deptName", "心内科");
        // 启动指定流程定义ID启动流程实例
        flowLongEngine.startInstanceById(processId, creator, args);
    }

    /**
     * 科室教秘审批
     */
    @Test
    public void test2() {
        // 审核人员当前科室
        Long currentDeptId = 1001L;

        // 启动指定流程定义ID启动流程实例
        flowLongEngine.startInstanceById(processId, creator).ifPresent(instance -> {
            // 当流程节点流转到科室手上时，由科室管理者进行审核
            List<FlwTask> tasks = queryService.getActiveTasksByInstanceId(instance.getId()).get();
            FlwTask flwTask  = tasks.stream().filter(t -> {
                JSONObject json = this.getJson(t.getVariable());
                return Objects.equals(currentDeptId, json.getLong("deptId"));
            }).findFirst().get();
            flowLongEngine.executeTask(flwTask.getId(), creator, Collections.singletonMap("reason", "同意"));
        });
    }

    private JSONObject getJson(String variable) {
        return JSONObject.parseObject(variable);
    }

    /**
     * 小哥撤回
     */
    @Test
    public void test3() {
        // 启动指定流程定义ID启动流程实例
        flowLongEngine.startInstanceById(processId, creator).ifPresent(instance -> {
            // 小哥审批
            Map<String, Object> args = new HashMap<>();
            args.put("小哥撤回参数", "小哥撤回");

            // 撤回任务（领导审批）
            QueryService queryService = flowLongEngine.queryService();
            List<FlwHisTask> hisTasks = queryService.getHisTasksByInstanceId(instance.getId()).get();
            FlwHisTask hisTask = hisTasks.stream().filter(t -> Objects.equals("领导审批", t.getTaskName())).findFirst().get();
            TaskService taskService = flowLongEngine.taskService();
            taskService.withdrawTask(hisTask.getId(), testCreator);
        });
    }

    /**
     * 大哥审批
     */
    @Test
    public void test4() {
        // 启动指定流程定义ID启动流程实例
        flowLongEngine.startInstanceById(processId, testCreator).ifPresent(instance -> {

            // 领导审批
            this.executeActiveTasks(instance.getId(), testCreator);

            // 撤回任务（领导审批）
            QueryService queryService = flowLongEngine.queryService();
            List<FlwHisTask> hisTasks = queryService.getHisTasksByInstanceId(instance.getId()).get();
            FlwHisTask hisTask = hisTasks.stream().filter(t -> Objects.equals("领导审批", t.getTaskName())).findFirst().get();
            TaskService taskService = flowLongEngine.taskService();
            taskService.withdrawTask(hisTask.getId(), testCreator);

            // 当前任务ID 用拿回任务
            // this.executeActiveTasks(instance.getId(), t -> taskService.reclaimTask(t.getParentTaskId(), testCreator));


            // 驳回任务（领导审批驳回，任务至发起人）
            this.executeActiveTasks(instance.getId(), t ->
                    taskService.rejectTask(t, testCreator, new HashMap<String, Object>() {{
                        put("reason", "不符合要求");
                    }})
            );

            // 执行当前任务并跳到【经理确认】节点
            this.executeActiveTasks(instance.getId(), t ->
                    flowLongEngine.executeJumpTask(t.getId(), "k005", testCreator)
            );

            // 经理确认，流程结束
            this.executeActiveTasks(instance.getId(), testCreator);
        });
    }
}
