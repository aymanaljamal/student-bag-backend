package com.studentbag.backend.analytics.service.impl;

import com.studentbag.backend.analytics.dto.dashboard.AdminDashboardAnalyticsResponse;
import com.studentbag.backend.analytics.mapper.DashboardAnalyticsMapper;
import com.studentbag.backend.analytics.repository.AnalyticsQueryRepository;
import com.studentbag.backend.analytics.service.AdminDashboardAnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminDashboardAnalyticsServiceImpl implements AdminDashboardAnalyticsService {

    private final AnalyticsQueryRepository queryRepository;
    private final DashboardAnalyticsMapper mapper;

    @Override
    public AdminDashboardAnalyticsResponse getDashboardAnalyticsByEmail(String email) {
        queryRepository.resolveAdministratorIdByEmail(email);

        return AdminDashboardAnalyticsResponse.builder()
                .userAnalytics(buildUserAnalytics())
                .resourceAnalytics(buildResourceAnalytics())
                .eventAnalytics(buildEventAnalytics())
                .notificationAnalytics(buildNotificationAnalytics())
                .academicAnalytics(buildAcademicAnalytics())
                .charts(buildCharts())
                .build();
    }

    private AdminDashboardAnalyticsResponse.UserAnalytics buildUserAnalytics() {
        return mapper.toAdminUserAnalytics(
                queryRepository.countUsers(),
                queryRepository.countUsersByRole("STUDENT"),
                queryRepository.countUsersByRole("INSTRUCTOR"),
                queryRepository.countUsersByRole("ADMINISTRATOR"),
                queryRepository.countNewUsersThisMonth()
        );
    }

    private AdminDashboardAnalyticsResponse.ResourceAnalytics buildResourceAnalytics() {
        return mapper.toAdminResourceAnalytics(
                queryRepository.countResources(),
                queryRepository.countResourcesByStatus("PENDING"),
                queryRepository.countResourcesByStatus("APPROVED"),
                queryRepository.countResourcesByStatus("REJECTED"),
                queryRepository.countResourcesByStatus("REMOVED")
        );
    }

    private AdminDashboardAnalyticsResponse.EventAnalytics buildEventAnalytics() {
        return mapper.toAdminEventAnalytics(
                queryRepository.countEvents(),
                queryRepository.countActiveEvents(),
                queryRepository.countFinishedEvents(),
                queryRepository.countUpcomingEvents(),
                queryRepository.countEventsRequiresRegistration(),
                queryRepository.countAllEventRegistrations()
        );
    }

    private AdminDashboardAnalyticsResponse.NotificationAnalytics buildNotificationAnalytics() {
        return mapper.toAdminNotificationAnalytics(
                queryRepository.countNotificationsSentToday(),
                queryRepository.countNotificationsSentThisMonth()
        );
    }

    private AdminDashboardAnalyticsResponse.AcademicAnalytics buildAcademicAnalytics() {
        return mapper.toAdminAcademicAnalytics(
                queryRepository.countInstitutions(),
                queryRepository.countCourses(),
                queryRepository.countFaculties(),
                queryRepository.countDepartments(),
                queryRepository.countTerms()
        );
    }

    private AdminDashboardAnalyticsResponse.AdminCharts buildCharts() {
        return mapper.toAdminCharts(
                List.of(
                        mapper.chartPoint("Students", queryRepository.countUsersByRole("STUDENT")),
                        mapper.chartPoint("Instructors", queryRepository.countUsersByRole("INSTRUCTOR")),
                        mapper.chartPoint("Admins", queryRepository.countUsersByRole("ADMINISTRATOR"))
                ),
                List.of(),
                List.of(
                        mapper.chartPoint("Pending", queryRepository.countResourcesByStatus("PENDING")),
                        mapper.chartPoint("Approved", queryRepository.countResourcesByStatus("APPROVED")),
                        mapper.chartPoint("Rejected", queryRepository.countResourcesByStatus("REJECTED")),
                        mapper.chartPoint("Removed", queryRepository.countResourcesByStatus("REMOVED"))
                ),
                List.of(
                        mapper.chartPoint("Active", queryRepository.countActiveEvents()),
                        mapper.chartPoint("Ended", queryRepository.countFinishedEvents()),
                        mapper.chartPoint("Upcoming", queryRepository.countUpcomingEvents())
                ),
                List.of(),
                List.of(
                        mapper.chartPoint("Institutions", queryRepository.countInstitutions()),
                        mapper.chartPoint("Courses", queryRepository.countCourses()),
                        mapper.chartPoint("Faculties", queryRepository.countFaculties()),
                        mapper.chartPoint("Departments", queryRepository.countDepartments()),
                        mapper.chartPoint("Terms", queryRepository.countTerms())
                )
        );
    }
}