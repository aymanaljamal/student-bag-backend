package com.studentbag.backend.chatbot.service;


import com.studentbag.backend.chatbot.dto.context.StudentAiContextDto;

public interface StudentAiContextService {

    StudentAiContextDto buildGeneralContext(Long studentId);

    StudentAiContextDto buildTodayStudyContext(Long studentId);

    StudentAiContextDto buildTaskContext(Long studentId);

    StudentAiContextDto buildScheduleContext(Long studentId);

    StudentAiContextDto buildNotesContext(Long studentId);

    StudentAiContextDto buildResourcesContext(Long studentId);

    StudentAiContextDto buildEventsContext(Long studentId);

    StudentAiContextDto buildGradesContext(Long studentId);

    StudentAiContextDto buildQuizContextFromNote(Long studentId, Long noteId);

    StudentAiContextDto buildContextForQuestion(Long studentId, String question);
}