package com.studentbag.backend.courses.specification;

import com.studentbag.backend.courses.entity.Course;
import org.springframework.data.jpa.domain.Specification;

public class CourseSpecification {

    /**
     * Specification to search courses with optional filters
     */
    public static Specification<Course> search(
            String keyword,
            Long institutionId,
            String level,
            Boolean isActive
    ) {
        return (root, query, cb) -> {

            var predicates = cb.conjunction();

            // Keyword filter
            if (keyword != null && !keyword.isEmpty()) {
                String like = "%" + keyword.toLowerCase() + "%";
                predicates = cb.and(predicates,
                        cb.or(
                                cb.like(cb.lower(root.get("nameArabic")), like),
                                cb.like(cb.lower(root.get("nameEnglish")), like),
                                cb.like(cb.lower(root.get("code")), like)
                        )
                );
            }

            // Institution filter
            if (institutionId != null) {
                predicates = cb.and(predicates,
                        cb.equal(root.get("institution").get("id"), institutionId)
                );
            }

            // Level filter
            if (level != null && !level.isEmpty()) {
                predicates = cb.and(predicates,
                        cb.equal(root.get("level"), level)
                );
            }

            // isActive filter
            if (isActive != null) {
                predicates = cb.and(predicates,
                        cb.equal(root.get("isActive"), isActive)
                );
            }

            return predicates;
        };
    }
}