package com.studentbag.backend.administrator.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminUpdateManagedUserRequest {
    private String fullName;
    private String email;
}