package com.studentbag.backend.events.repository;

import com.studentbag.backend.events.entity.Opportunity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface OpportunityRepository extends JpaRepository<Opportunity, Long> {

    // FR-9.6: Filter opportunities by field and work mode (e.g., Remote, On-site)
    @Query("SELECT o FROM Opportunity o WHERE " +
            "(:field IS NULL OR o.field = :field) AND " +
            "(:mode IS NULL OR o.workMode = :mode) AND " +
            "(:isPaid IS NULL OR o.isPaid = :isPaid) AND " +
            "(o.applicationDeadline >= :today OR o.applicationDeadline IS NULL)")
    List<Opportunity> findActiveOpportunities(
            @Param("field") String field,
            @Param("mode") String mode,
            @Param("isPaid") Boolean isPaid,
            @Param("today") LocalDate today);

    // FR-9.7: For synchronization from external Career Centers
    boolean existsByEventTitleAndCompanyName(String title, String companyName);
}