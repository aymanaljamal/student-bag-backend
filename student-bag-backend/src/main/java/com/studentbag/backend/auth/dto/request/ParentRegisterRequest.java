package com.studentbag.backend.auth.dto.request;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ParentRegisterRequest extends BaseRegisterRequest {

    private String defaultRelationshipLabel;
}