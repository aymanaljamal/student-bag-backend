package com.studentbag.backend.analytics.service.impl;

import com.studentbag.backend.analytics.dto.dashboard.ChartDataPointDto;
import com.studentbag.backend.analytics.dto.dashboard.InstructorDashboardAnalyticsResponse;
import com.studentbag.backend.analytics.mapper.DashboardAnalyticsMapper;
import com.studentbag.backend.analytics.repository.AnalyticsQueryRepository;
import com.studentbag.backend.analytics.service.InstructorDashboardAnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InstructorDashboardAnalyticsServiceImpl implements InstructorDashboardAnalyticsService {

    private final AnalyticsQueryRepository queryRepository;
    private final DashboardAnalyticsMapper mapper;

    @Override
    public InstructorDashboardAnalyticsResponse getMyDashboardAnalyticsByEmail(String email) {
        Long instructorId = queryRepository.resolveInstructorIdByEmail(email);

        return InstructorDashboardAnalyticsResponse.builder()
                .resourceAnalytics(buildResourceAnalytics(instructorId))
                .eventAnalytics(buildEventAnalytics(instructorId))
                .teachingAnalytics(buildTeachingAnalytics(instructorId))
                .charts(buildCharts(instructorId))
                .build();
    }
    private InstructorDashboardAnalyticsResponse.ResourceAnalytics buildResourceAnalytics(Long instructorId) {
        return mapper.toInstructorResourceAnalytics(
                queryRepository.countInstructorResources(instructorId),
                queryRepository.countInstructorResourcesByStatus(instructorId, "PENDING"),
                queryRepository.countInstructorResourcesByStatus(instructorId, "APPROVED"),
                queryRepository.countInstructorResourcesByStatus(instructorId, "REJECTED"),
                queryRepository.countInstructorResourcesByStatus(instructorId, "REMOVED")
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

    private InstructorDashboardAnalyticsResponse.InstructorCharts buildCharts(Long instructorId) {
        List<ChartDataPointDto> resourcesByStatus = List.of(
                mapper.chartPoint("Pending", queryRepository.countInstructorResourcesByStatus(instructorId, "PENDING")),
                mapper.chartPoint("Approved", queryRepository.countInstructorResourcesByStatus(instructorId, "APPROVED")),
                mapper.chartPoint("Rejected", queryRepository.countInstructorResourcesByStatus(instructorId, "REJECTED")),
                mapper.chartPoint("Removed", queryRepository.countInstructorResourcesByStatus(instructorId, "REMOVED"))
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