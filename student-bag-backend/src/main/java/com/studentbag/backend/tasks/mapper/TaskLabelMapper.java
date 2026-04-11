package com.studentbag.backend.tasks.mapper;

import com.studentbag.backend.tasks.dto.request.CreateTaskLabelRequest;
import com.studentbag.backend.tasks.dto.request.UpdateTaskLabelRequest;
import com.studentbag.backend.tasks.dto.response.TaskLabelResponse;
import com.studentbag.backend.tasks.entity.TaskLabel;
import org.springframework.stereotype.Component;

@Component
public class TaskLabelMapper {

    public TaskLabel toEntity(CreateTaskLabelRequest request) {
        if (request == null) {
            return null;
        }

        return TaskLabel.builder()
                .name(request.getName())
                .colorHex(request.getColorHex())
                .build();
    }

    public void updateEntity(TaskLabel label, UpdateTaskLabelRequest request) {
        if (label == null || request == null) {
            return;
        }

        if (request.getName() != null) {
            label.setName(request.getName());
        }

        if (request.getColorHex() != null) {
            label.setColorHex(request.getColorHex());
        }
    }

    public TaskLabelResponse toResponse(TaskLabel entity) {
        if (entity == null) {
            return null;
        }

        return TaskLabelResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .colorHex(entity.getColorHex())
                .build();
    }
}