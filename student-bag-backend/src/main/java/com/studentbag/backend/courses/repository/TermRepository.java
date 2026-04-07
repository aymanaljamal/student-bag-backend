package com.studentbag.backend.courses.repository;

import com.studentbag.backend.courses.entity.Term;
import com.studentbag.backend.institution.entity.Institution;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TermRepository extends JpaRepository<Term, Long> {

    // البحث عن ترم محدد بواسطة الكود والمؤسسة
    Optional<Term> findByTermCodeAndInstitution(String termCode, Institution institution);

    // البحث عن ترم بواسطة المعرف الخارجي والمؤسسة
    Optional<Term> findByExternalIdAndInstitution(String externalId, Institution institution);
    Optional<Term> findByTermCodeAndInstitutionId(String termCode, Long institutionId);
    // جلب جميع الفصول الدراسية التابعة لمؤسسة معينة
    // هذه الميثود هي التي يستخدمها الـ SyncService لإدارة حالة الـ isCurrent
    List<Term> findAllByInstitution(Institution institution);
}