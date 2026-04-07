package com.studentbag.backend.courses.sync.service.impl;

import com.studentbag.backend.courses.sync.dto.RitajClassSessionDto;
import com.studentbag.backend.courses.sync.dto.RitajCourseDto;
import com.studentbag.backend.courses.sync.dto.RitajSectionDto;
import com.studentbag.backend.courses.sync.helper.*;
import com.studentbag.backend.courses.sync.service.RitajParserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class RitajParserServiceImpl implements RitajParserService {

    private final RitajDayTimeParserHelper dateTimeHelper;
    private final RoomParsingHelper roomHelper;
    private final CourseCodeMetadataHelper metadataHelper;

    private static final Pattern COURSE_CODE_PATTERN = Pattern.compile("^[A-Z]{4}\\d{3,4}$");
    private static final Pattern TIME_PATTERN = Pattern.compile("(\\d{2}:\\d{2}\\s*-\\s*\\d{2}:\\d{2})");
    private static final Pattern DAY_CODE_PATTERN = Pattern.compile("[SUMTWR]");
    private static final Pattern FACULTY_PATTERN = Pattern.compile("^كلية\\s+.*");
    private static final Pattern DEPT_PATTERN = Pattern.compile("^(دائرة|تخصص|قسم)\\s+.*");

    // يشمل جميع أنواع الشعب لضمان التعرف عليها كأبناء للمساق الحالي
    private static final String SECTION_START_REGEX = "^(محاضرة|مختبر|نقاش|حلقة بحث|تدريب|مشروع|Lecture|Lab|Discussion|Seminar|Training|Project).*";

    private static final String[] RITAJ_NOTES = {
            "المساقات التي تظهر باللون بنفسجي",
            "يتم تدريسها بلقاءات وجاهية",
            "إخفاء المساقات",
            "نوع الشعبة"
    };

    private record DepartmentInfo(String facultyAr, String facultyEn, String deptAr, String deptEn, int facultyId) {}
    private static final Map<String, DepartmentInfo> DEPT_MAP = new HashMap<>();
    static {
        // --- 1. كلية الهندسة والتكنولوجيا (ID: 1) ---
        addDept("ENCS", "كلية الهندسة والتكنولوجيا", "Faculty of Engineering and Technology", "دائرة هندسة أنظمة الحاسوب", "Computer Systems Engineering", 1);
        addDept("SWEN", "كلية الهندسة والتكنولوجيا", "Faculty of Engineering and Technology", "دائرة هندسة البرمجيات", "Software Engineering", 1);
        addDept("ENEE", "كلية الهندسة والتكنولوجيا", "Faculty of Engineering and Technology", "دائرة الهندسة الكهربائية والطبية الحيوية", "Electrical & Biomedical Engineering", 1);
        addDept("ENCE", "كلية الهندسة والتكنولوجيا", "Faculty of Engineering and Technology", "دائرة الهندسة المدنية والبيئية", "Civil & Environmental Engineering", 1);
        addDept("ENME", "كلية الهندسة والتكنولوجيا", "Faculty of Engineering and Technology", "دائرة الهندسة الميكانيكية والميكاترونكس", "Mechanical & Mechatronics Engineering", 1);
        addDept("ENMC", "كلية الهندسة والتكنولوجيا", "Faculty of Engineering and Technology", "تخصص هندسة الميكاترونكس", "Mechatronics Engineering", 1);
        addDept("ENAR", "كلية الهندسة والتكنولوجيا", "Faculty of Engineering and Technology", "دائرة الهندسة المعمارية", "Architecture", 1);
        addDept("ENPL", "كلية الهندسة والتكنولوجيا", "Faculty of Engineering and Technology", "دائرة هندسة التخطيط العمراني", "Urban Planning Engineering", 1);
        addDept("CSEC", "كلية الهندسة والتكنولوجيا", "Faculty of Engineering and Technology", "تخصص الأمن السيبراني", "Cyber Security", 1);
        addDept("DSIN", "كلية الهندسة والتكنولوجيا", "Faculty of Engineering and Technology", "دائرة التصميم الرقمي", "Digital Design", 1);
        addDept("COMP", "كلية الهندسة والتكنولوجيا", "Faculty of Engineering and Technology", "دائرة علم الحاسوب", "Computer Science", 1);
        addDept("GENG", "كلية الهندسة والتكنولوجيا", "Faculty of Engineering and Technology", "مساقات الهندسة العامة", "General Engineering", 1);
        addDept("INED", "كلية الهندسة والتكنولوجيا", "Faculty of Engineering and Technology", "التعليم المهني والتقني", "Industrial Education", 1);
        addDept("ENET", "كلية الهندسة والتكنولوجيا", "Faculty of Engineering and Technology", "ماجستير هندسة تكنولوجيا المعلومات", "Information Technology Engineering (MSc)", 1);
        addDept("MSCE", "كلية الهندسة والتكنولوجيا", "Faculty of Engineering and Technology", "ماجستير هندسة الإنشاءات", "Civil Engineering (MSc)", 1);
        addDept("WEEN", "كلية الهندسة والتكنولوجيا", "Faculty of Engineering and Technology", "ماجستير هندسة المياه والبيئة", "Water & Environmental Engineering (MSc)", 1);
        addDept("JMEE", "كلية الهندسة والتكنولوجيا", "Faculty of Engineering and Technology", "ماجستير الهندسة الكهربائية المشترك", "Joint Electrical Engineering (MSc)", 1);

        // --- 2. كلية الآداب (ID: 2) ---
        addDept("ARSK", "كلية الآداب", "Faculty of Arts", "مهارات اللغة العربية", "Arabic Language Skills", 2);
        addDept("ENGC", "كلية الآداب", "Faculty of Arts", "مهارات اللغة الإنجليزية", "English Language Skills", 2);
        addDept("MDJO", "كلية الآداب", "Faculty of Arts", "دائرة الإعلام", "Media", 2);
        addDept("COMM", "كلية الآداب", "Faculty of Arts", "مساقات الاتصال والإعلام", "Communication", 2);
        addDept("TRAN", "كلية الآداب", "Faculty of Arts", "دائرة اللغات والترجمة", "Languages & Translation", 2);
        addDept("SOCI", "كلية الآداب", "Faculty of Arts", "دائرة العلوم الاجتماعية والسلوكية", "Social & Behavioral Sciences", 2);
        addDept("PSYC", "كلية الآداب", "Faculty of Arts", "دائرة علم النفس", "Psychology", 2);
        addDept("GEOG", "كلية الآداب", "Faculty of Arts", "دائرة الجغرافيا", "Geography", 2);
        addDept("GEOI", "كلية الآداب", "Faculty of Arts", "نظم المعلومات الجغرافية", "GIS", 2);
        addDept("WOMS", "كلية الآداب", "Faculty of Arts", "برنامج دراسات المرأة", "Women Studies", 2);
        addDept("ANTH", "كلية الآداب", "Faculty of Arts", "تخصص الأنثروبولوجيا", "Anthropology", 2);
        addDept("CULS", "كلية الآداب", "Faculty of Arts", "مساقات الدراسات الثقافية", "Cultural Studies", 2);
        addDept("SPAN", "كلية الآداب", "Faculty of Arts", "اللغة الإسبانية", "Spanish Language", 2);
        addDept("TURK", "كلية الآداب", "Faculty of Arts", "اللغة التركية", "Turkish Language", 2);
        addDept("GERM", "كلية الآداب", "Faculty of Arts", "اللغة الألمانية", "German Language", 2);
        addDept("HEBR", "كلية الآداب", "Faculty of Arts", "اللغة العبرية", "Hebrew Language", 2);
        addDept("TIFR", "كلية الآداب", "Faculty of Arts", "اللغة الفرنسية", "French Language", 2);

        // --- 4. كلية الأعمال والاقتصاد (ID: 4) ---
        addDept("MKET", "كلية الأعمال والاقتصاد", "Faculty of Business and Economics", "دائرة التسويق", "Marketing", 4);
        addDept("FINN", "كلية الأعمال والاقتصاد", "Faculty of Business and Economics", "دائرة العلوم المالية والمصرفية", "Finance & Banking", 4);
        addDept("ACCT", "كلية الأعمال والاقتصاد", "Faculty of Business and Economics", "دائرة المحاسبة", "Accounting", 4);
        addDept("ACFI", "كلية الأعمال والاقتصاد", "Faculty of Business and Economics", "المحاسبة والتمويل", "Accounting & Finance", 4);

        // --- 6. كلية العلوم (ID: 6) ---
        addDept("STAT", "كلية العلوم", "Faculty of Science", "دائرة الإحصاء", "Statistics", 6);
        addDept("MATH", "كلية العلوم", "Faculty of Science", "دائرة الرياضيات", "Mathematics", 6);
        addDept("PHYS", "كلية العلوم", "Faculty of Science", "دائرة الفيزياء", "Physics", 6);
        addDept("CHEM", "كلية العلوم", "Faculty of Science", "دائرة الكيمياء", "Chemistry", 6);
        addDept("BIOC", "كلية العلوم", "Faculty of Science", "دائرة الكيمياء الحيوية", "Biochemistry", 6);
        addDept("BIOL", "كلية العلوم", "Faculty of Science", "دائرة الأحياء", "Biology", 6);
        addDept("BIOT", "كلية العلوم", "Faculty of Science", "التقنيات الحيوية", "Biotechnology", 6);
        addDept("FRSC", "كلية العلوم", "Faculty of Science", "العلوم الجنائية", "Forensic Science", 6);

        // --- 7. كلية الدراسات العليا (ID: 7) ---
        addDept("MSAI", "كلية الدراسات العليا", "Faculty of Graduate Studies", "ماجستير الذكاء الاصطناعي", "Master of AI", 7);
        addDept("ASDS", "كلية الدراسات العليا", "Faculty of Graduate Studies", "الإحصاء التطبيقي وتحليل البيانات", "Applied Statistics & Data Science", 7);
        addDept("MSCM", "كلية الدراسات العليا", "Faculty of Graduate Studies", "ماجستير الحوسبة", "Computing (MSc)", 7);
        addDept("MDGD", "كلية الدراسات العليا", "Faculty of Graduate Studies", "النوع الاجتماعي والتنمية", "Gender & Development", 7);
        addDept("CCST", "كلية الدراسات العليا", "Faculty of Graduate Studies", "الدراسات الثقافية العليا", "Cultural Studies (MA)", 7);
        addDept("CODE", "كلية الدراسات العليا", "Faculty of Graduate Studies", "التنمية المجتمعية", "Community Development", 7);
        addDept("ISST", "كلية الدراسات العليا", "Faculty of Graduate Studies", "الدراسات الإسرائيلية", "Israeli Studies", 7);
        addDept("INST", "كلية الدراسات العليا", "Faculty of Graduate Studies", "الدراسات الدولية العليا", "International Studies (MA)", 7);
        addDept("DMHR", "كلية الدراسات العليا", "Faculty of Graduate Studies", "الديمقراطية وحقوق الإنسان", "Democracy & Human Rights", 7);
        addDept("HAIC", "كلية الدراسات العليا", "Faculty of Graduate Studies", "التاريخ العربي الإسلامي", "Arab Islamic History", 7);
        addDept("LAIT", "كلية الدراسات العليا", "Faculty of Graduate Studies", "الأدب واللغة العربية عالي", "Arabic Literature (MA)", 7);
        addDept("GADS", "كلية الدراسات العليا", "Faculty of Graduate Studies", "الإدارة العامة والحوكمة", "Public Administration & Governance", 7);
        addDept("MPAS", "كلية الدراسات العليا", "Faculty of Graduate Studies", "ماجستير الإدارة العامة", "Public Administration (MA)", 7);
        addDept("IMRS", "كلية الدراسات العليا", "Faculty of Graduate Studies", "الهجرة واللاجئين", "Migration & Refugees", 7);
        addDept("MCLS", "كلية الدراسات العليا", "Faculty of Graduate Studies", "ماجستير العلوم الطبية المخبرية", "Medical Lab Sciences (MSc)", 7);
        addDept("MIPT", "كلية الدراسات العليا", "Faculty of Graduate Studies", "ماجستير المهن الصحية", "Health Professions (MSc)", 7);
        addDept("WOHE", "كلية الدراسات العليا", "Faculty of Graduate Studies", "ماجستير صحة المرأة", "Women's Health (MSc)", 7);
        addDept("MONS", "كلية الدراسات العليا", "Faculty of Graduate Studies", "ماجستير القبالة", "Midwifery (MSc)", 7);
        addDept("MDAB", "كلية الدراسات العليا", "Faculty of Graduate Studies", "ماجستير التنمية وبناء المؤسسات", "Development & Institution Building", 7);
        addDept("GOVS", "كلية الدراسات العليا", "Faculty of Graduate Studies", "برنامج الحوكمة وبناء المؤسسات", "Governance & Institution Building", 7);
        addDept("IRDS", "كلية الدراسات العليا", "Faculty of Graduate Studies", "ماجستير العلاقات الدولية", "International Relations (MA)", 7);
        addDept("RENE", "كلية الدراسات العليا", "Faculty of Graduate Studies", "الطاقة المتجددة", "Renewable Energy", 7);
        addDept("ACCA", "كلية الدراسات العليا", "Faculty of Graduate Studies", "المحاسبة والتدقيق", "Accounting & Auditing", 7);
        addDept("CPSY", "كلية الدراسات العليا", "Faculty of Graduate Studies", "علم النفس الإرشادي", "Community Psychology", 7);

        // --- 8. كلية الصيدلة والتمريض والمهن الصحية (ID: 8) ---
        addDept("NUTD", "كلية الصيدلة والتمريض والمهن الصحية", "Faculty of Pharmacy, Nursing and Health Professions", "دائرة التغذية والحمية", "Nutrition & Dietetics", 8);
        addDept("BCLS", "كلية الصيدلة والتمريض والمهن الصحية", "Faculty of Pharmacy, Nursing and Health Professions", "دائرة العلوم الطبية المخبرية", "Medical Lab Sciences", 8);
        addDept("SPAU", "كلية الصيدلة والتمريض والمهن الصحية", "Faculty of Pharmacy, Nursing and Health Professions", "دائرة السمع والنطق", "Audiology & Speech Therapy", 8);
        addDept("PUBH", "كلية الصيدلة والتمريض والمهن الصحية", "Faculty of Pharmacy, Nursing and Health Professions", "دائرة الصحة العامة", "Public Health", 8);

        // --- 9. كلية الحقوق والإدارة العامة (ID: 9) ---
        addDept("JURI", "كلية الحقوق والإدارة العامة", "Faculty of Law and Public Administration", "دائرة القانون", "Law", 9);
        addDept("PUBA", "كلية الحقوق والإدارة العامة", "Faculty of Law and Public Administration", "دائرة الإدارة العامة", "Public Administration", 9);
        addDept("INRE", "كلية الحقوق والإدارة العامة", "Faculty of Law and Public Administration", "دائرة الدراسات الدولية", "International Studies", 9);
        addDept("PACS", "كلية الحقوق والإدارة العامة", "Faculty of Law and Public Administration", "دراسات التنمية", "Development Studies", 9);
        addDept("PRLW", "كلية الحقوق والإدارة العامة", "Faculty of Law and Public Administration", "القانون الخاص", "Private Law (MA)", 9);
        addDept("LECO", "كلية الحقوق والإدارة العامة", "Faculty of Law and Public Administration", "القانون والاقتصاد", "Law & Economics (MA)", 9);

        // --- 10. كلية الفنون والموسيقى والتصميم (ID: 10) ---
        addDept("GRDN", "كلية الفنون والموسيقى والتصميم", "Faculty of Art, Music & Design", "دائرة التصميم الجرافيكي", "Graphic Design", 10);
        addDept("VAAR", "كلية الفنون والموسيقى والتصميم", "Faculty of Art, Music & Design", "دائرة الفنون البصرية", "Visual Arts", 10);
        addDept("MUSI", "كلية الفنون والموسيقى والتصميم", "Faculty of Art, Music & Design", "دائرة الموسيقى", "Music", 10);

        // --- أخرى ---
        addDept("GENS", "متطلبات عامة", "General Requirements", "مساقات عامة", "General Studies", 3);
    }
    private static void addDept(String prefix, String facAr, String facEn, String deptAr, String deptEn, int facId) {
        DEPT_MAP.put(prefix, new DepartmentInfo(facAr, facEn, deptAr, deptEn, facId));
    }

    @Override
    public List<RitajCourseDto> parseCourses(String arContent, String enContent) {
        // خريطة تحتوي على كود المساق -> { اسم المساق، أسماء المدرسين لكل شعبة }
        Map<String, Map<String, String>> enDataMap = parseEnglishContent(enContent);

        List<RitajCourseDto> courses = new ArrayList<>();
        if (arContent == null || arContent.isBlank()) return courses;

        String[] lines = arContent.split("\\r?\\n");
        RitajCourseDto currentCourse = null;

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.isBlank() || isNoteLine(line)) continue;

            if (COURSE_CODE_PATTERN.matcher(line).matches()) {
                currentCourse = new RitajCourseDto();
                currentCourse.setCode(line);
                if (i + 1 < lines.length) currentCourse.setNameArabic(lines[++i].trim());

                Map<String, String> enData = enDataMap.getOrDefault(line, new HashMap<>());
                currentCourse.setNameEnglish(enData.getOrDefault("courseName", line));

                String prefix = line.substring(0, 4).toUpperCase();
                DepartmentInfo info = DEPT_MAP.get(prefix);

                if (info != null) {
                    currentCourse.setFacultyNameArabic(info.facultyAr());
                    currentCourse.setFacultyNameEnglish(info.facultyEn());
                    currentCourse.setDepartmentNameArabic(info.deptAr());
                    currentCourse.setDepartmentNameEnglish(info.deptEn());
                } else {
                    currentCourse.setFacultyNameArabic("كليات أخرى");
                    currentCourse.setDepartmentNameArabic("N/A");
                    currentCourse.setFacultyNameEnglish("Other Faculties");
                    currentCourse.setDepartmentNameEnglish("N/A");
                }

                currentCourse.setAcademicLevel(metadataHelper.extractAcademicLevel(line).name());
                currentCourse.setCreditHours(metadataHelper.extractCreditHours(line));
                currentCourse.setSections(new ArrayList<>());
                courses.add(currentCourse);
                continue;
            }

            if (currentCourse != null && isSectionStart(line)) {
                StringBuilder fullSectionData = new StringBuilder(line);
                while (i + 1 < lines.length && isDataExtension(lines[i+1])) {
                    fullSectionData.append(" ").append(lines[++i].trim());
                }

                String sectionNum = extractSectionNumber(fullSectionData.toString());
                String instructorEn = enDataMap.getOrDefault(currentCourse.getCode(), new HashMap<>())
                        .get("instructor_" + sectionNum);

                parseAndAddSection(fullSectionData.toString(), currentCourse, instructorEn);
            }
        }
        return courses;
    }

    private void parseAndAddSection(String data, RitajCourseDto course, String instructorEn) {
        String cleanData = data.replace("\t", " ").replaceAll("\\s{2,}", " ").trim();
        for (String note : RITAJ_NOTES) {
            if (cleanData.contains(note)) cleanData = cleanData.split(note)[0].trim();
        }

        RitajSectionDto section = new RitajSectionDto();
        section.setCourseCode(course.getCode());
        section.setSessions(new ArrayList<>());

        String[] parts = cleanData.split(" ");
        if (parts.length < 2) return;
        section.setSectionType(parts[0]);
        section.setSectionNumber(parts[1]);
        section.setInstructorNameEnglish(instructorEn != null ? instructorEn : "TBD");

        Matcher timeMatcher = TIME_PATTERN.matcher(cleanData);
        List<Integer> tStarts = new ArrayList<>();
        List<Integer> tEnds = new ArrayList<>();
        while (timeMatcher.find()) {
            tStarts.add(timeMatcher.start());
            tEnds.add(timeMatcher.end());
        }

        if (tStarts.isEmpty()) {
            // معالجة الشعب التي لا تمتلك وقتاً (مثل الرسائل N/A)
            section.setInstructorNameArabic(cleanInstructorName(cleanData));
            course.getSections().add(section);
            return;
        }

        String beforeFirstTime = cleanData.substring(0, tStarts.get(0));
        section.setInstructorNameArabic(cleanInstructorName(beforeFirstTime));

        for (int i = 0; i < tStarts.size(); i++) {
            int currentEnd = tEnds.get(i);
            int nextStart = (i + 1 < tStarts.size()) ? tStarts.get(i + 1) : cleanData.length();

            String prevPart = (i == 0) ? beforeFirstTime : cleanData.substring(tEnds.get(i-1), tStarts.get(i));
            String rawRoomPart = cleanData.substring(currentEnd, nextStart).trim();

            String roomToParse = rawRoomPart.split("(?i)(محاضرة|نقاش|مختبر|حلقة بحث|تدريب)")[0].trim();

            if (roomToParse.isEmpty() || roomToParse.matches("^\\d+$")) {
                String potentialBuilding = prevPart.replaceAll("[SUMTWR\\s,]+$", "").trim();
                String[] bParts = potentialBuilding.split(" ");
                if (bParts.length > 0) {
                    roomToParse = bParts[bParts.length - 1] + roomToParse;
                }
            }

            if (roomToParse.length() > 25 || (roomToParse.matches(".*[\\u0600-\\u06FF].*") && !roomToParse.matches(".*\\d.*"))) {
                roomToParse = "";
            }

            if (roomToParse.isEmpty()) {
                String potential = prevPart.replaceAll("[SUMTWR\\s,]+$", "").trim();
                String[] roomParts = potential.split(" ");
                roomToParse = roomParts[roomParts.length - 1];
            }

            RoomParsingHelper.ParsedRoom parsedRoom = roomHelper.parse(roomToParse);

            Set<DayOfWeek> days = new LinkedHashSet<>();
            Matcher dayMatcher = DAY_CODE_PATTERN.matcher(prevPart);
            while (dayMatcher.find()) {
                days.addAll(dateTimeHelper.parseDays(dayMatcher.group()));
            }

            String timeStr = cleanData.substring(tStarts.get(i), tEnds.get(i));
            RitajDayTimeParserHelper.TimeRange range = dateTimeHelper.parseTimeRange(timeStr);

            for (DayOfWeek day : days) {
                RitajClassSessionDto sess = new RitajClassSessionDto();
                sess.setDayOfWeek(day);
                if (range != null) {
                    sess.setStartTime(range.start());
                    sess.setEndTime(range.end());
                }
                sess.setRoom(parsedRoom.room());
                sess.setBuilding(parsedRoom.buildingCode());
                section.getSessions().add(sess);
            }
        }
        course.getSections().add(section);
    }

    private String cleanInstructorName(String text) {
        String res = text.replaceAll("^(محاضرة|مختبر|نقاش|حلقة بحث|تدريب|مشروع|Lecture|Lab|Discussion|Seminar|Training|Project)\\s+\\d+", "").trim();
        res = res.replaceAll("\\b\\d{1,3}\\b(?=\\s*[SUMTWR])", "");
        res = res.replaceAll("[SUMTWR\\s,]+$", "").trim();
        return res.isEmpty() ? "غير محدد" : res;
    }

    private String extractSectionNumber(String data) {
        String[] parts = data.trim().split("\\s+");
        return parts.length > 1 ? parts[1] : "1";
    }

    private boolean isSectionStart(String line) {
        return line.matches(SECTION_START_REGEX);
    }

    private boolean isDataExtension(String line) {
        String l = line.trim();
        if (l.isEmpty() || isNoteLine(l)) return false;
        return !isSectionStart(l) &&
                !COURSE_CODE_PATTERN.matcher(l).matches() &&
                !FACULTY_PATTERN.matcher(l).matches() &&
                !DEPT_PATTERN.matcher(l).matches();
    }

    private boolean isNoteLine(String line) {
        for (String note : RITAJ_NOTES) {
            if (line.contains(note)) return true;
        }
        return false;
    }

    private Map<String, Map<String, String>> parseEnglishContent(String enContent) {
        Map<String, Map<String, String>> masterMap = new HashMap<>();
        if (enContent == null || enContent.isBlank()) return masterMap;

        String[] lines = enContent.split("\\r?\\n");
        String lastCode = null;

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            if (COURSE_CODE_PATTERN.matcher(line).matches()) {
                lastCode = line;
                Map<String, String> data = new HashMap<>();
                if (i + 1 < lines.length) data.put("courseName", lines[++i].trim());
                masterMap.put(lastCode, data);
            } else if (lastCode != null && line.matches("^(Lecture|Seminar|Lab|Discussion|Project|Training).*")) {
                String[] parts = line.split("\\s+");
                if (parts.length >= 3) {
                    String secNum = parts[1];
                    StringBuilder nameBuilder = new StringBuilder();
                    for (int k = 2; k < parts.length; k++) {
                        if (parts[k].matches("\\d+") || parts[k].matches("[SUMTWR]")) break;
                        nameBuilder.append(parts[k]).append(" ");
                    }
                    masterMap.get(lastCode).put("instructor_" + secNum, nameBuilder.toString().trim());
                }
            }
        }
        return masterMap;
    }

    private Map<String, String> parseEnglishNames(String enContent) {
        Map<String, String> map = new HashMap<>();
        if (enContent == null || enContent.isBlank()) return map;
        String[] lines = enContent.split("\\r?\\n");
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            if (COURSE_CODE_PATTERN.matcher(line).matches() && i + 1 < lines.length) {
                map.put(line, lines[i + 1].trim());
            }
        }
        return map;
    }
}