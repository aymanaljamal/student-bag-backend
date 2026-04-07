package com.studentbag.backend;

import com.studentbag.backend.courses.sync.helper.RoomParsingHelper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RoomParsingHelperTest {

    private final RoomParsingHelper helper = new RoomParsingHelper();

    @Test
    @DisplayName("فحص الحالات المعقدة والملتصقة")
    void testComplexRoomParsing() {
        // 1. حالة الالتصاق التام (Aggad225)
        // ملاحظة: الـ Regex الحالي (\\d+)$ سيفصل الرقم 225 عما قبله بنجاح
        var res1 = helper.parse("Aggad225");
        assertEquals("Aggad", res1.buildingCode(), "فشل في فصل اسم المبنى المتلاصق");
        assertEquals("225", res1.room(), "فشل في فصل رقم القاعة المتلاصق");

        // 2. حالة وجود نقطة ومسافات غريبة (Sh.Shaheen105)
        var res2 = helper.parse("Sh.Shaheen105");
        assertEquals("Sh.Shaheen", res2.buildingCode());
        assertEquals("105", res2.room());

        // 3. حالة وجود شرطة أو رموز (B.A-202)
        // سيقوم الـ Regex بأخذ الـ 202 كرقم وكل ما يسبقها كمبنى
        var res3 = helper.parse("B.A-202");
        assertTrue(res3.buildingCode().contains("B.A-") || res3.buildingCode().equals("B.A"), "تأكد من فصل المبنى عن الرقم");
        assertEquals("202", res3.room());

        // 4. حالة وجود فراغ صلب (Non-breaking space \u00A0)
        var res4 = helper.parse("Aggad\u00A0210");
        assertEquals("Aggad", res4.buildingCode());
        assertEquals("210", res4.room());
    }

    @Test
    @DisplayName("فحص حالات الأونلاين والقاعات الخاصة")
    void testSpecialCases() {
        // 1. حالة أونلاين بالعربي
        var res1 = helper.parse("محاضرة عن بعد");
        assertEquals("ONLINE", res1.buildingCode());
        assertEquals("ONLINE", res1.room());

        // 2. قاعة عبارة عن نص فقط (بدون أرقام في النهاية)
        var res2 = helper.parse("IOL");
        assertEquals("IOL", res2.buildingCode());
        assertEquals("N/A", res2.room());

        // 3. رقم قاعة فقط (بدون اسم مبنى)
        // حسب منطق الكود الأخير، سيعطي "مبنى غير محدد"
        var res3 = helper.parse("322");
        assertEquals("مبنى غير محدد", res3.buildingCode());
        assertEquals("322", res3.room());
    }

    @Test
    @DisplayName("فحص استخراج الطابق")
    void testFloorExtraction() {
        // قاعة 305 -> طابق 3
        var res1 = helper.parse("Aggad305");
        assertEquals(3, res1.floor());

        // قاعة 102 -> طابق 1
        var res2 = helper.parse("Shaheen102");
        assertEquals(1, res2.floor());

        // قاعة مكونة من 4 أرقام (مثل 1105) تعني طابق 11
        var res3 = helper.parse("Main1105");
        assertEquals(11, res3.floor());

        // رقم قاعة صغير (أقل من 3 خانات) يعطي طابق 0
        var res4 = helper.parse("Office22");
        assertEquals(0, res4.floor());
    }

    @Test
    @DisplayName("فحص المدخلات الفارغة")
    void testEmptyInputs() {
        var res1 = helper.parse("");
        assertEquals("N/A", res1.buildingCode());

        var res2 = helper.parse(null);
        assertEquals("N/A", res2.buildingCode());
    }
}