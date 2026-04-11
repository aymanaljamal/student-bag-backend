package com.studentbag.backend.tasks.mapper;

import com.studentbag.backend.tasks.dto.request.CreateSubtaskRequest;
import com.studentbag.backend.tasks.dto.request.SubtaskOrderItemRequest;
import com.studentbag.backend.tasks.dto.request.UpdateSubtaskRequest;
import com.studentbag.backend.tasks.dto.response.SubtaskResponse;
import com.studentbag.backend.tasks.entity.Subtask;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class SubtaskMapper {

    public Subtask toEntity(CreateSubtaskRequest request) {
        if (request == null) {
            return null;
        }

        return Subtask.builder()
                .title(request.getTitle())
                .isCompleted(Boolean.TRUE.equals(request.getIsCompleted()))
                .orderIndex(request.getOrderIndex() != null ? request.getOrderIndex() : 0)
                .build();
    }

    public void updateEntity(Subtask subtask, UpdateSubtaskRequest request) {
        if (subtask == null || request == null) {
            return;
        }

        if (request.getTitle() != null) {
            subtask.setTitle(request.getTitle());
        }

        if (request.getIsCompleted() != null) {
            subtask.setIsCompleted(request.getIsCompleted());
        }

        if (request.getOrderIndex() != null) {
            subtask.setOrderIndex(request.getOrderIndex());
        }
    }

    public void applyOrderItem(Subtask subtask, SubtaskOrderItemRequest request) {
        if (subtask == null || request == null) {
            return;
        }

        if (request.getOrderIndex() != null) {
            subtask.setOrderIndex(request.getOrderIndex());
        }
    }

    public SubtaskResponse toResponse(Subtask entity) {
        if (entity == null) {
            return null;
        }

        return SubtaskResponse.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .isCompleted(entity.getIsCompleted())
                .orderIndex(entity.getOrderIndex())
                .build();
    }

    public List<SubtaskResponse> toResponseList(List<Subtask> entities) {
        if (entities == null) {
            return List.of();
        }

        return entities.stream()
                .filter(Objects::nonNull)
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
}