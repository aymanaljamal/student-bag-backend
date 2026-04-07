package com.studentbag.backend.courses.sync.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RitajFetchedPagesDto {
    // تم تغيير المسميات من Html إلى Content لتعبر عن ملفات الـ txt
    private String arabicContent;
    private String englishContent;
}