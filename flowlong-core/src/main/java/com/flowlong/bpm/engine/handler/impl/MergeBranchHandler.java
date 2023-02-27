/* Copyright 2023-2025 www.flowlong.com
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
package com.flowlong.bpm.engine.handler.impl;

import com.flowlong.bpm.engine.model.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 合并分支操作的处理器
 *
 * @author hubin
 * @since 1.0
 */
public class MergeBranchHandler extends AbstractMergeHandler {
    private JoinModel model;

    public MergeBranchHandler(JoinModel model) {
        this.model = model;
    }

    /**
     * 对join节点的所有输入变迁进行递归，查找join至fork节点的所有中间task元素
     *
     * @param node
     * @param buffer
     */
    private void findForkTaskNames(NodeModel node, StringBuilder buffer) {
        if (node instanceof ForkModel) return;
        List<TransitionModel> inputs = node.getInputs();
        for (TransitionModel tm : inputs) {
            if (tm.getSource() instanceof WorkModel) {
                buffer.append(tm.getSource().getName()).append(",");
            }
            findForkTaskNames(tm.getSource(), buffer);
        }
    }

    /**
     * 对join节点的所有输入变迁进行递归，查找join至fork节点的所有中间task元素
     *
     * @see AbstractMergeHandler#findActiveNodes()
     */
    @Override
    protected List<String> findActiveNodes() {
        StringBuilder buffer = new StringBuilder(20);
        findForkTaskNames(model, buffer);
        return Arrays.stream(buffer.toString().split(",")).collect(Collectors.toList());
    }
}
