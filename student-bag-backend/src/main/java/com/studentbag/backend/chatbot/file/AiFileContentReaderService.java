package com.studentbag.backend.chatbot.file;

public interface AiFileContentReaderService {

    String readFileContentPreview(
            String fileUrl,
            String fileName,
            String mimeType,
            Long fileSizeBytes,
            int maxChars
    );
}