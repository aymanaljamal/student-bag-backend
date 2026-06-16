package com.studentbag.backend.analytics.service.impl;

import com.studentbag.backend.analytics.dto.dashboard.ChartDataPointDto;
import com.studentbag.backend.analytics.dto.dashboard.InstructorDashboardAnalyticsResponse;
import com.studentbag.backend.analytics.mapper.DashboardAnalyticsMapper;
import com.studentbag.backend.analytics.repository.AnalyticsQueryRepository;
import com.studentbag.backend.analytics.service.InstructorDashboardAnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InstructorDashboardAnalyticsServiceImpl implements InstructorDashboardAnalyticsService {

    private final AnalyticsQueryRepository queryRepository;
    private final DashboardAnalyticsMapper mapper;

    @Override
    public InstructorDashboardAnalyticsResponse getMyDashboardAnalyticsByEmail(String email) {
        UUID userId = queryRepository.resolveUserIdByEmail(email);
        Long instructorId = queryRepository.resolveInstructorIdByEmail(email);

        return InstructorDashboardAnalyticsResponse.builder()
                .resourceAnalytics(buildResourceAnalytics(userId))
                .eventAnalytics(buildEventAnalytics(instructorId))
                .teachingAnalytics(buildTeachingAnalytics(instructorId))
                .charts(buildCharts(userId, instructorId))
                .build();
    }

    private InstructorDashboardAnalyticsResponse.ResourceAnalytics buildResourceAnalytics(UUID userId) {
        return mapper.toInstructorResourceAnalytics(
                queryRepository.countInstructorResourcesByUserId(userId),
                queryRepository.countInstructorResourcesByStatusAndUserId(userId, "PENDING"),
                queryRepository.countInstructorResourcesByStatusAndUserId(userId, "APPROVED"),
                queryRepository.countInstructorResourcesByStatusAndUserId(userId, "REJECTED"),
                queryRepository.countInstructorResourcesByStatusAndUserId(userId, "REMOVED")
        );
    }

    private InstructorDashboardAnalyticsResponse.EventAnalytics buildEventAnalytics(Long instructorId) {
        return mapper.toInstructorEventAnalytics(
                queryRepository.countInstructorEvents(instructorId),
                queryRepository.countInstructorActiveEvents(instructorId),
                queryRepository.countInstructorEndedEvents(instructorId),
                queryRepository.countInstructorUpcomingEvents(instructorId),
                queryRepository.countInstructorEventRegistrations(instructorId)
        );
    }

    private InstructorDashboardAnalyticsResponse.TeachingAnalytics buildTeachingAnalytics(Long instructorId) {
        return mapper.toTeachingAnalytics(
                queryRepository.countInstructorSections(instructorId),
                queryRepository.sumInstructorSectionsCapacity(instructorId),
                queryRepository.sumInstructorSectionsEnrolled(instructorId)
        );
    }

    private InstructorDashboardAnalyticsResponse.InstructorCharts buildCharts(
            UUID userId,
            Long instructorId
    ) {
        List<ChartDataPointDto> resourcesByStatus = List.of(
                mapper.chartPoint("Pending", queryRepository.countInstructorResourcesByStatusAndUserId(userId, "PENDING")),
                mapper.chartPoint("Approved", queryRepository.countInstructorResourcesByStatusAndUserId(userId, "APPROVED")),
                mapper.chartPoint("Rejected", queryRepository.countInstructorResourcesByStatusAndUserId(userId, "REJECTED")),
                mapper.chartPoint("Removed", queryRepository.countInstructorResourcesByStatusAndUserId(userId, "REMOVED"))
        );

        List<ChartDataPointDto> eventsByStatus = List.of(
                mapper.chartPoint("Active", queryRepository.countInstructorActiveEvents(instructorId)),
                mapper.chartPoint("Ended", queryRepository.countInstructorEndedEvents(instructorId)),
                mapper.chartPoint("Upcoming", queryRepository.countInstructorUpcomingEvents(instructorId))
        );

        List<ChartDataPointDto> capacityVsEnrolled = List.of(
                mapper.chartPoint("Capacity", queryRepository.sumInstructorSectionsCapacity(instructorId)),
                mapper.chartPoint("Enrolled", queryRepository.sumInstructorSectionsEnrolled(instructorId))
        );

        return mapper.toInstructorCharts(
                resourcesByStatus,
                List.of(),
                eventsByStatus,
                List.of(),
                capacityVsEnrolled
        );
    }
}