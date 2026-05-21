package com.studentbag.backend.courses.sync.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RitajFetchedPagesDto {
    
    private String jsonContent;

    @Deprecated
    private String arabicContent;
    @Deprecated
    private String englishContent;
}