package com.studentbag.backend.chatbot.file;

import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.TikaCoreProperties;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.sax.BodyContentHandler;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Locale;

@Slf4j
@Service
public class AiFileContentReaderServiceImpl implements AiFileContentReaderService {

    private static final long MAX_FILE_BYTES = 8L * 1024 * 1024;

    private final WebClient webClient = WebClient.builder().build();
    private final AutoDetectParser parser = new AutoDetectParser();
    private final Tika tika = new Tika();

    @Override
    public String readFileContentPreview(
            String fileUrl,
            String fileName,
            String mimeType,
            Long fileSizeBytes,
            int maxChars
    ) {
        if (fileUrl == null || fileUrl.isBlank()) {
            return "";
        }

        if (fileSizeBytes != null && fileSizeBytes > MAX_FILE_BYTES) {
            log.warn(
                    "AI skipped large file. fileName={}, size={}",
                    fileName,
                    fileSizeBytes
            );
            return "";
        }

        try {
            byte[] bytes = downloadFile(fileUrl);

            if (bytes.length == 0) {
                return "";
            }

            if (bytes.length > MAX_FILE_BYTES) {
                log.warn(
                        "AI skipped downloaded large file. fileName={}, downloadedSize={}",
                        fileName,
                        bytes.length
                );
                return "";
            }

            String detectedMimeType = detectMimeType(bytes, fileName, mimeType);

            if (isImage(detectedMimeType, fileName)) {
                return "";
            }

            String extractedText = extractText(bytes, fileName, maxChars);

            return cleanAndLimit(extractedText, maxChars);

        } catch (Exception ex) {
            log.warn(
                    "AI failed to read file content. fileName={}, url={}",
                    fileName,
                    fileUrl,
                    ex
            );
            return "";
        }
    }

    private byte[] downloadFile(String fileUrl) {
        byte[] bytes = webClient.get()
                .uri(fileUrl)
                .retrieve()
                .bodyToMono(byte[].class)
                .block();

        return bytes == null ? new byte[0] : bytes;
    }

    private String detectMimeType(
            byte[] bytes,
            String fileName,
            String mimeType
    ) throws IOException {
        if (mimeType != null && !mimeType.isBlank()) {
            return mimeType.toLowerCase(Locale.ROOT);
        }

        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes)) {
            return tika.detect(inputStream, fileName).toLowerCase(Locale.ROOT);
        }
    }

    private String extractText(
            byte[] bytes,
            String fileName,
            int maxChars
    ) throws IOException, SAXException, org.apache.tika.exception.TikaException {

        Metadata metadata = new Metadata();

        if (fileName != null && !fileName.isBlank()) {
            metadata.set(TikaCoreProperties.RESOURCE_NAME_KEY, fileName);
        }

        BodyContentHandler handler = new BodyContentHandler(Math.max(maxChars, 1000));

        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes)) {
            parser.parse(inputStream, handler, metadata);
        }

        return handler.toString();
    }

    private boolean isImage(String mimeType, String fileName) {
        if (mimeType != null && mimeType.startsWith("image/")) {
            return true;
        }

        String name = fileName == null
                ? ""
                : fileName.toLowerCase(Locale.ROOT);

        return name.endsWith(".png")
                || name.endsWith(".jpg")
                || name.endsWith(".jpeg")
                || name.endsWith(".bmp")
                || name.endsWith(".gif")
                || name.endsWith(".webp")
                || name.endsWith(".tif")
                || name.endsWith(".tiff");
    }

    private String cleanAndLimit(String value, int maxChars) {
        if (value == null || value.isBlank()) {
            return "";
        }

        String clean = value
                .replace("\u0000", " ")
                .replaceAll("[ \\t\\x0B\\f\\r]+", " ")
                .replaceAll("\\n{3,}", "\n\n")
                .trim();

        if (clean.length() <= maxChars) {
            return clean;
        }

        return clean.substring(0, maxChars);
    }
}