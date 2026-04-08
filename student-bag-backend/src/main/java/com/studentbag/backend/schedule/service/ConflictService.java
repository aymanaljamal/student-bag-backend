package com.studentbag.backend.schedule.service;

import com.studentbag.backend.courses.entity.CourseSection;
import com.studentbag.backend.courses.entity.ClassSession;
import java.util.List;


public interface ConflictService {
    boolean isOverlap(ClassSession s1, ClassSession s2);
    boolean hasConflict(CourseSection newSection, List<CourseSection> currentPicks);
}