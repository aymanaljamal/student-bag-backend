package com.studentbag.backend.courses.sync.helper;

import org.springframework.stereotype.Component;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class RoomParsingHelper {

    // نمط يبحث عن الأرقام في نهاية النص (قد تسبقها مسافة أو لا)
    private static final Pattern ROOM_NUMBER_PATTERN = Pattern.compile("(.*?)\\s*(\\d+)$");

    public ParsedRoom parse(String rawRoom) {
        if (rawRoom == null || rawRoom.isBlank()) {
            return new ParsedRoom("N/A", "N/A", "N/A", "N/A", null);
        }

        // 1. تنظيف النص
        String value = rawRoom.replace("\u00A0", " ").replace("\t", " ").trim();

        // 2. فحص الأونلاين
        if (isOnline(value)) {
            return new ParsedRoom("ONLINE", "عن بعد", "ONLINE", "ONLINE", 0);
        }

        String buildingName = value; // الافتراضي هو النص كامل
        String roomNumber = "N/A";

        // 3. محاولة الفصل بين الاسم والرقم
        Matcher matcher = ROOM_NUMBER_PATTERN.matcher(value);
        if (matcher.find()) {
            buildingName = matcher.group(1).trim(); // الجزء النصي (المبنى)
            roomNumber = matcher.group(2).trim();   // الجزء الرقمي (القاعة)
        }

        // في حال كان النص عبارة عن رقم فقط (مثل "220")
        if (buildingName.isEmpty() && !roomNumber.equals("N/A")) {
            buildingName = "مبنى غير محدد";
        }

        // 4. بناء النتيجة النهائية
        ParsedRoom result = new ParsedRoom(
                buildingName,             // كود المبنى (الاسم فقط)
                buildingName,             // الاسم العربي
                buildingName,             // الاسم الإنجليزي (مؤقتاً نفس العربي)
                roomNumber,               // رقم القاعة المنفصل
                extractFloor(roomNumber)  // استخراج الطابق
        );

        System.out.println(String.format(">>> [RoomHelper] Full: '%s' -> Building: '%s', Room: '%s'",
                value, result.buildingArabic(), result.room()));

        return result;
    }

    private Integer extractFloor(String roomNumber) {
        if (roomNumber != null && !roomNumber.equals("N/A") && roomNumber.length() >= 3) {
            // إذا كان الرقم 220، الطابق هو 2. إذا كان 1015، الطابق هو 10.
            try {
                if (roomNumber.length() == 3) {
                    return Character.getNumericValue(roomNumber.charAt(0));
                } else if (roomNumber.length() >= 4) {
                    return Integer.parseInt(roomNumber.substring(0, roomNumber.length() - 2));
                }
            } catch (Exception e) {
                return 0;
            }
        }
        return 0;
    }

    private boolean isOnline(String value) {
        String v = value.toLowerCase();
        return v.contains("online") || v.contains("zoom") || v.contains("عن بعد") || v.contains("بيت لحم");
    }

    public record ParsedRoom(
            String buildingCode,
            String buildingArabic,
            String buildingEnglish,
            String room,
            Integer floor
    ) {}
}