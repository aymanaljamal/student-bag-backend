package com.studentbag.backend.tasks.mapper;

import com.studentbag.backend.tasks.dto.request.ReorderSubtasksRequest;
import com.studentbag.backend.tasks.dto.request.SubtaskOrderItemRequest;
import com.studentbag.backend.tasks.entity.Subtask;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class SubtaskReorderMapper {

    public void applyReorder(List<Subtask> subtasks, ReorderSubtasksRequest request) {
        if (subtasks == null || request == null || request.getItems() == null) {
            return;
        }

        Map<Long, Subtask> subtaskMap = subtasks.stream()
                .collect(Collectors.toMap(Subtask::getId, Function.identity()));

        for (SubtaskOrderItemRequest item : request.getItems()) {
            if (item == null || item.getSubtaskId() == null) {
                continue;
            }

            Subtask subtask = subtaskMap.get(item.getSubtaskId());
            if (subtask != null && item.getOrderIndex() != null) {
                subtask.setOrderIndex(item.getOrderIndex());
            }
        }
    }
}