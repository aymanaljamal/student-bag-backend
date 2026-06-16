package com.studentbag.backend.notifications.entity;

import com.studentbag.backend.student.entity.Student;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "student_notification_preferences")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentNotificationPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false)
    @JoinColumn(name = "student_id", unique = true, nullable = false)
    private Student student;
    @Builder.Default
    private Boolean weeklyResourceNotificationsEnabled = true;
    @Column(nullable = false)
    @Builder.Default
    private Boolean eventNotificationsEnabled = true;

    @Column(nullable = false)
    @Builder.Default
    private Boolean taskNotificationsEnabled = true;

    @Column(nullable = false)
    @Builder.Default
    private Boolean recurringTaskNotificationsEnabled = true;

    @Column(nullable = false)
    @Builder.Default
    private Boolean taskReminderOneDayBeforeEnabled = true;

    @Column(nullable = false)
    @Builder.Default
    private Boolean monthlyStatsNotificationsEnabled = false;
}