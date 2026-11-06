package com.xixin.health.statistics;

import com.xixin.health.appointment.Appointment;
import com.xixin.health.appointment.AppointmentRepository;
import com.xixin.health.appointment.AppointmentStatus;
import com.xixin.health.common.error.BusinessException;
import com.xixin.health.common.error.ErrorCode;
import com.xixin.health.exam.ExamRecord;
import com.xixin.health.exam.ExamRecordRepository;
import com.xixin.health.order.Order;
import com.xixin.health.order.OrderRepository;
import com.xixin.health.order.OrderStatus;
import com.xixin.health.report.ExamReport;
import com.xixin.health.report.ExamReportRepository;
import com.xixin.health.result.AbnormalLevel;
import com.xixin.health.result.CheckResultRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class StatisticsService {

    private final AppointmentRepository appointmentRepository;
    private final OrderRepository orderRepository;
    private final ExamRecordRepository examRecordRepository;
    private final ExamReportRepository examReportRepository;
    private final CheckResultRepository checkResultRepository;
    private final JdbcTemplate jdbc;

    public StatisticsService(
        AppointmentRepository appointmentRepository,
        OrderRepository orderRepository,
        ExamRecordRepository examRecordRepository,
        ExamReportRepository examReportRepository,
        CheckResultRepository checkResultRepository,
        JdbcTemplate jdbc
    ) {
        this.appointmentRepository = appointmentRepository;
        this.orderRepository = orderRepository;
        this.examRecordRepository = examRecordRepository;
        this.examReportRepository = examReportRepository;
        this.checkResultRepository = checkResultRepository;
        this.jdbc = jdbc;
    }

    public BusinessStatistics business(LocalDate startDate, LocalDate endDate, Long branchId, String dimension) {
        validateRange(startDate, endDate);
        List<Appointment> appointments = appointmentRepository.findAll().stream()
            .filter(appointment -> startDate == null || !appointment.getAppointmentDate().isBefore(startDate))
            .filter(appointment -> endDate == null || !appointment.getAppointmentDate().isAfter(endDate))
            .filter(appointment -> branchId == null || branchId.equals(appointment.getBranchId()))
            .toList();
        Set<Long> appointmentIds = appointments.stream()
            .map(Appointment::getId)
            .collect(Collectors.toSet());
        List<Order> orders = orderRepository.findAll().stream()
            .filter(order -> appointmentIds.contains(order.getAppointmentId()))
            .filter(order -> order.getStatus() == OrderStatus.PAID)
            .toList();

        long checkedInAppointments = appointments.stream()
            .filter(appointment -> appointment.getStatus() == AppointmentStatus.CHECKED_IN)
            .count();
        long completedAppointments = appointments.stream()
            .filter(appointment -> appointment.getStatus() == AppointmentStatus.COMPLETED)
            .count();
        long paidAppointments = appointments.stream()
            .filter(appointment -> appointment.getStatus() == AppointmentStatus.PAID)
            .count();
        long totalRevenueCent = orders.stream()
            .mapToLong(Order::getAmountCent)
            .sum();

        return new BusinessStatistics(
            appointments.size(),
            paidAppointments,
            checkedInAppointments,
            completedAppointments,
            orders.size(),
            totalRevenueCent,
            countBy(appointments, Appointment::getPackageId),
            countBy(appointments, Appointment::getBranchId),
            dimension == null || dimension.isBlank() ? "DAY" : dimension
        );
    }

    public HealthStatistics health(LocalDate startDate, LocalDate endDate, Long branchId) {
        validateRange(startDate, endDate);
        Set<Long> branchExamRecordIds = examRecordRepository.findAll().stream()
            .filter(record -> branchId == null || branchId.equals(record.getBranchId()))
            .map(ExamRecord::getId)
            .collect(Collectors.toSet());
        List<ExamReport> reports = examReportRepository.findAll().stream()
            .filter(report -> branchId == null || branchExamRecordIds.contains(report.getExamRecordId()))
            .filter(report -> startDate == null || !report.getGeneratedAt().toLocalDate().isBefore(startDate))
            .filter(report -> endDate == null || !report.getGeneratedAt().toLocalDate().isAfter(endDate))
            .toList();
        Map<AbnormalLevel, Long> reportCounts = new EnumMap<>(AbnormalLevel.class);
        reports.forEach(report -> reportCounts.merge(report.getAbnormalStatus(), 1L, Long::sum));

        Map<AbnormalLevel, Long> resultCounts = new EnumMap<>(AbnormalLevel.class);
        checkResultRepository.findAll().stream()
            .filter(result -> branchId == null || branchExamRecordIds.contains(result.getExamRecordId()))
            .forEach(result -> resultCounts.merge(result.getAbnormalLevel(), 1L, Long::sum));

        long abnormalReports = reports.stream()
            .filter(report -> report.getAbnormalStatus() == AbnormalLevel.ABNORMAL)
            .count();
        long criticalReports = reports.stream()
            .filter(report -> report.getAbnormalStatus() == AbnormalLevel.CRITICAL)
            .count();

        return new HealthStatistics(
            reports.size(),
            abnormalReports,
            criticalReports,
            toEnumCounts(resultCounts),
            toEnumCounts(reportCounts),
            branchId
        );
    }

    public ExportFile export(String type, String format, LocalDate startDate, LocalDate endDate, Long branchId) {
        validateRange(startDate, endDate);
        String normalizedType = type == null ? "all" : type.trim().toLowerCase(Locale.ROOT);
        String normalizedFormat = format == null ? "EXCEL" : format.trim().toUpperCase(Locale.ROOT);

        if ("EXCEL".equals(normalizedFormat)) {
            BusinessStatistics biz = business(startDate, endDate, branchId, "TOTAL");
            HealthStatistics hlth = health(startDate, endDate, branchId);
            byte[] excelBytes;
            String excelName;
            if ("health".equals(normalizedType)) {
                excelName = "健康统计报表.xlsx";
                excelBytes = xlsxBytes(cnSheetXml(hlth));
            } else if ("business".equals(normalizedType)) {
                excelName = "业务统计报表.xlsx";
                excelBytes = xlsxBytes(cnSheetXml(biz));
            } else {
                excelName = "综合统计报表.xlsx";
                excelBytes = xlsxBytesTwoSheets(biz, hlth);
            }
            return new ExportFile(excelName,
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", excelBytes);
        }

        if ("health".equals(normalizedType)) {
            return exportHealth(normalizedFormat, health(startDate, endDate, branchId));
        }
        return exportBusiness(normalizedType, normalizedFormat, business(startDate, endDate, branchId, "TOTAL"));
    }

    private ExportFile exportBusiness(String type, String normalizedFormat, BusinessStatistics business) {
        String fn = "business".equals(type) ? "业务统计" : "健康统计";
        if ("PDF".equals(normalizedFormat)) {
            return new ExportFile(
                fn + ".pdf",
                "application/pdf",
                pdfBytes(type, business)
            );
        }
        if ("CSV".equals(normalizedFormat)) {
            return new ExportFile(
                fn + ".csv",
                "text/csv; charset=UTF-8",
                csvText(type, normalizedFormat, business).getBytes(StandardCharsets.UTF_8)
            );
        }
        return new ExportFile(
                fn + ".xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                xlsxBytes(type, business)
        );
    }

    private ExportFile exportHealth(String normalizedFormat, HealthStatistics health) {
        if ("PDF".equals(normalizedFormat)) {
            return new ExportFile(
                "健康统计.pdf",
                "application/pdf",
                pdfBytes("health", health)
            );
        }
        if ("CSV".equals(normalizedFormat)) {
            return new ExportFile(
                "健康统计.csv",
                "text/csv; charset=UTF-8",
                csvText("health", normalizedFormat, health).getBytes(StandardCharsets.UTF_8)
            );
        }
        return new ExportFile(
            "健康统计.xlsx",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            xlsxBytes("health", health)
        );
    }

    private void validateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            return;
        }
        if (startDate.isAfter(endDate)) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "Start date must be before end date");
        }
        if (ChronoUnit.DAYS.between(startDate, endDate) > 3660) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "Statistics date range cannot exceed ten years");
        }
    }

    private String csvText(String type, String format, BusinessStatistics business) {
        return "类型,总预约数,已签到数,已完成数,已支付订单,总收入(元)\n"
            + safe(type) + "," + safe(format) + "," + business.totalAppointments() + ","
            + business.totalRevenueCent() + "," + business.paidOrders() + ","
            + business.checkedInAppointments() + "," + business.completedAppointments() + "\n";
    }

    private String csvText(String type, String format, HealthStatistics health) {
        return "类型,总报告数,异常报告,危急报告,分院ID\n"
            + safe(type) + "," + safe(format) + "," + health.totalReports() + ","
            + health.abnormalReports() + "," + health.criticalReports() + ","
            + (health.branchId() == null ? "" : health.branchId()) + "\n";
    }

    private byte[] xlsxBytes(String type, BusinessStatistics business) {
        return xlsxBytes(sheetXml(type, business));
    }

    private byte[] xlsxBytesTwoSheets(BusinessStatistics business, HealthStatistics health) {
        try {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            try (ZipOutputStream zip = new ZipOutputStream(output, StandardCharsets.UTF_8)) {
                zip(zip, "[Content_Types].xml",
                    "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                    + "<Types xmlns=\"http://schemas.openxmlformats.org/package/2006/content-types\">"
                    + "<Default Extension=\"rels\" ContentType=\"application/vnd.openxmlformats-package.relationships+xml\"/>"
                    + "<Default Extension=\"xml\" ContentType=\"application/xml\"/>"
                    + "<Override PartName=\"/xl/workbook.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml\"/>"
                    + "<Override PartName=\"/xl/worksheets/sheet1.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml\"/>"
                    + "<Override PartName=\"/xl/worksheets/sheet2.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml\"/>"
                    + "</Types>");
                zip(zip, "_rels/.rels",
                    "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                    + "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">"
                    + "<Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument\" Target=\"xl/workbook.xml\"/>"
                    + "</Relationships>");
                zip(zip, "xl/workbook.xml",
                    "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                    + "<workbook xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\" xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\">"
                    + "<sheets><sheet name=\"业务统计\" sheetId=\"1\" r:id=\"rId1\"/>"
                    + "<sheet name=\"健康统计\" sheetId=\"2\" r:id=\"rId2\"/></sheets></workbook>");
                zip(zip, "xl/_rels/workbook.xml.rels",
                    "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                    + "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">"
                    + "<Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet\" Target=\"worksheets/sheet1.xml\"/>"
                    + "<Relationship Id=\"rId2\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet\" Target=\"worksheets/sheet2.xml\"/>"
                    + "</Relationships>");
                zip(zip, "xl/worksheets/sheet1.xml", cnSheetXml(business));
                zip(zip, "xl/worksheets/sheet2.xml", cnSheetXml(health));
            }
            return output.toByteArray();
        } catch (IOException exception) {
            throw new UncheckedIOException(exception);
        }
    }

    private byte[] xlsxBytes(String type, HealthStatistics health) {
        return xlsxBytes(sheetXml(type, health));
    }

    private byte[] xlsxBytes(String sheetXml) {
        try {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            try (ZipOutputStream zip = new ZipOutputStream(output, StandardCharsets.UTF_8)) {
                zip(zip, "[Content_Types].xml", """
                    <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                    <Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
                      <Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
                      <Default Extension="xml" ContentType="application/xml"/>
                      <Override PartName="/xl/workbook.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml"/>
                      <Override PartName="/xl/worksheets/sheet1.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/>
                    </Types>
                    """.stripIndent());
                zip(zip, "_rels/.rels", """
                    <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                    <Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
                      <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="xl/workbook.xml"/>
                    </Relationships>
                    """.stripIndent());
                zip(zip, "xl/workbook.xml", """
                    <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                    <workbook xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships">
                      <sheets><sheet name="Statistics" sheetId="1" r:id="rId1"/></sheets>
                    </workbook>
                    """.stripIndent());
                zip(zip, "xl/_rels/workbook.xml.rels", """
                    <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                    <Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
                      <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet1.xml"/>
                    </Relationships>
                    """.stripIndent());
                zip(zip, "xl/worksheets/sheet1.xml", sheetXml);
            }
            return output.toByteArray();
        } catch (IOException exception) {
            throw new UncheckedIOException(exception);
        }
    }

    private String cnSheetXml(BusinessStatistics business) {
        return buildSheet(
            java.util.List.of("类型", "总预约数", "已签到数", "已完成数", "已支付订单", "总收入(元)"),
            java.util.List.of("业务统计",
                String.valueOf(business.totalAppointments()),
                String.valueOf(business.checkedInAppointments()),
                String.valueOf(business.completedAppointments()),
                String.valueOf(business.paidOrders()),
                String.valueOf(business.totalRevenueCent())));
    }

    private String cnSheetXml(HealthStatistics health) {
        return buildSheet(
            java.util.List.of("类型", "总报告数", "异常报告", "危急报告", "分院ID"),
            java.util.List.of("健康统计",
                String.valueOf(health.totalReports()),
                String.valueOf(health.abnormalReports()),
                String.valueOf(health.criticalReports()),
                health.branchId() == null ? "" : String.valueOf(health.branchId())));
    }

    private String buildSheet(java.util.List<String> headers, java.util.List<String> values) {
        StringBuilder data = new StringBuilder();
        data.append("<row r=\"1\">");
        for (int i = 0; i < headers.size(); i++) {
            data.append("<c r=\"").append((char)('A'+i)).append("1\" t=\"inlineStr\"><is><t>")
                .append(xml(headers.get(i))).append("</t></is></c>");
        }
        data.append("</row>");
        data.append("<row r=\"2\">");
        for (int i = 0; i < values.size(); i++) {
            data.append("<c r=\"").append((char)('A'+i)).append("2\" t=\"inlineStr\"><is><t>")
                .append(xml(values.get(i))).append("</t></is></c>");
        }
        data.append("</row>");
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
            + "<worksheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\">"
            + "<sheetData>" + data + "</sheetData></worksheet>";
    }

    private String sheetXml(String type, BusinessStatistics business) {
        List<List<String>> rows = List.of(
            List.of("type", "totalAppointments", "checkedInAppointments", "completedAppointments", "paidOrders", "totalRevenueCent"),
            List.of(
                safe(type),
                String.valueOf(business.totalAppointments()),
                String.valueOf(business.checkedInAppointments()),
                String.valueOf(business.completedAppointments()),
                String.valueOf(business.paidOrders()),
                String.valueOf(business.totalRevenueCent())
            )
        );
        StringBuilder sheetData = new StringBuilder();
        for (int rowIndex = 0; rowIndex < rows.size(); rowIndex++) {
            sheetData.append("<row r=\"").append(rowIndex + 1).append("\">");
            List<String> row = rows.get(rowIndex);
            for (int columnIndex = 0; columnIndex < row.size(); columnIndex++) {
                sheetData.append("<c r=\"")
                    .append((char) ('A' + columnIndex))
                    .append(rowIndex + 1)
                    .append("\" t=\"inlineStr\"><is><t>")
                    .append(xml(row.get(columnIndex)))
                    .append("</t></is></c>");
            }
            sheetData.append("</row>");
        }
        return """
            <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
            <worksheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">
              <sheetData>%s</sheetData>
            </worksheet>
            """.formatted(sheetData);
    }

    private String sheetXml(String type, HealthStatistics health) {
        List<List<String>> rows = List.of(
            List.of("type", "totalReports", "abnormalReports", "criticalReports", "branchId"),
            List.of(
                safe(type),
                String.valueOf(health.totalReports()),
                String.valueOf(health.abnormalReports()),
                String.valueOf(health.criticalReports()),
                health.branchId() == null ? "" : String.valueOf(health.branchId())
            )
        );
        StringBuilder sheetData = new StringBuilder();
        for (int rowIndex = 0; rowIndex < rows.size(); rowIndex++) {
            sheetData.append("<row r=\"").append(rowIndex + 1).append("\">");
            List<String> row = rows.get(rowIndex);
            for (int columnIndex = 0; columnIndex < row.size(); columnIndex++) {
                sheetData.append("<c r=\"")
                    .append((char) ('A' + columnIndex))
                    .append(rowIndex + 1)
                    .append("\" t=\"inlineStr\"><is><t>")
                    .append(xml(row.get(columnIndex)))
                    .append("</t></is></c>");
            }
            sheetData.append("</row>");
        }
        return """
            <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
            <worksheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">
              <sheetData>%s</sheetData>
            </worksheet>
            """.formatted(sheetData);
    }

    private void zip(ZipOutputStream zip, String path, String content) throws IOException {
        zip.putNextEntry(new ZipEntry(path));
        zip.write(content.getBytes(StandardCharsets.UTF_8));
        zip.closeEntry();
    }

    private byte[] pdfBytes(String type, BusinessStatistics business) {
        return writeMultiLinePdf(buildBusinessLines(business));
    }

    private byte[] pdfBytes(String type, HealthStatistics health) {
        return writeMultiLinePdf(buildHealthLines(health));
    }

    private String[] buildBusinessLines(BusinessStatistics business) {
        String dateStr = java.time.LocalDate.now().toString();
        String sep = "----------------------------------------";
        String rpt = "Business Statistics Report";
        return new String[] {
            rpt,
            "Date: " + dateStr,
            sep,
            "Total Appointments: " + business.totalAppointments(),
            "Checked In: " + business.checkedInAppointments(),
            "Completed: " + business.completedAppointments(),
            "Paid Orders: " + business.paidOrders(),
            "Revenue: " + String.format("%.2f", business.totalRevenueCent() / 100.0),
            sep,
            "Report generated by Xixin Health System",
            "All data is for reference only."
        };
    }

    private String[] buildHealthLines(HealthStatistics health) {
        String dateStr = java.time.LocalDate.now().toString();
        String sep = "----------------------------------------";
        java.util.List<String> list = new java.util.ArrayList<>();
        list.add("Health Statistics Report");
        list.add("Date: " + dateStr);
        list.add(sep);
        list.add("Total Reports: " + health.totalReports());
        list.add("Abnormal: " + health.abnormalReports());
        if (health.criticalReports() > 0) {
            list.add("CRITICAL: " + health.criticalReports() + " (IMMEDIATE ATTENTION)");
        }
        list.add(sep);
        list.add("Report generated by Xixin Health System");
        list.add("All data is for reference only.");
        return list.toArray(new String[0]);
    }

    private byte[] writeMultiLinePdf(String[] lines) {
        try {
            StringBuilder streamBuilder = new StringBuilder();
            int yPos = 750;
            streamBuilder.append("BT /F1 16 Tf 50 ").append(yPos).append(" Td (")
                .append(pdf(lines[0])).append(") Tj ET\n");
            yPos -= 30;
            if (lines.length > 1) {
                streamBuilder.append("BT /F1 9 Tf 50 ").append(yPos).append(" Td (")
                    .append(pdf(lines[1])).append(") Tj ET\n");
                yPos -= 22;
            }
            for (int i = 2; i < lines.length; i++) {
                streamBuilder.append("BT /F1 11 Tf 50 ").append(yPos).append(" Td (")
                    .append(pdf(lines[i])).append(") Tj ET\n");
                yPos -= 18;
            }
            String stream = streamBuilder.toString();
            byte[] streamBytes = stream.getBytes(StandardCharsets.US_ASCII);

            java.util.List<byte[]> objects = new java.util.ArrayList<>();
            objects.add("<< /Type /Catalog /Pages 2 0 R >>".getBytes(StandardCharsets.US_ASCII));
            objects.add("<< /Type /Pages /Kids [3 0 R] /Count 1 >>".getBytes(StandardCharsets.US_ASCII));
            objects.add(("<< /Type /Page /Parent 2 0 R /MediaBox [0 0 612 792]"
                + " /Resources << /Font << /F1 4 0 R >> >> /Contents 5 0 R >>").getBytes(StandardCharsets.US_ASCII));
            objects.add("<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>".getBytes(StandardCharsets.US_ASCII));
            objects.add(("<< /Length " + streamBytes.length + " >>\nstream\n").getBytes(StandardCharsets.US_ASCII));

            ByteArrayOutputStream obj5 = new ByteArrayOutputStream();
            obj5.write(objects.get(4));
            obj5.write(streamBytes);
            obj5.write("\nendstream".getBytes(StandardCharsets.US_ASCII));
            objects.set(4, obj5.toByteArray());

            ByteArrayOutputStream output = new ByteArrayOutputStream();
            writeAscii(output, "%PDF-1.4\n");
            java.util.List<Integer> offsets = new java.util.ArrayList<>();
            for (int i = 0; i < objects.size(); i++) {
                offsets.add(output.size());
                writeAscii(output, (i + 1) + " 0 obj\n");
                output.writeBytes(objects.get(i));
                writeAscii(output, "\nendobj\n");
            }
            int xrefOffset = output.size();
            writeAscii(output, "xref\n0 " + (objects.size() + 1) + "\n");
            writeAscii(output, "0000000000 65535 f \n");
            for (Integer offset : offsets) {
                writeAscii(output, String.format(java.util.Locale.ROOT, "%010d 00000 n \n", offset));
            }
            writeAscii(output, "trailer\n<< /Size " + (objects.size() + 1) + " /Root 1 0 R >>\nstartxref\n" + xrefOffset + "\n%%EOF\n");
            return output.toByteArray();
        } catch (java.io.IOException e) {
            throw new java.io.UncheckedIOException(e);
        }
    }

    private void writeAscii(ByteArrayOutputStream output, String value) {
        output.writeBytes(value.getBytes(StandardCharsets.US_ASCII));
    }

    private String xml(String value) {
        return value == null ? "" : value
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&apos;");
    }

    private String pdf(String value) {
        return value == null ? "" : value
            .replace("\\", "\\\\")
            .replace("(", "\\(")
            .replace(")", "\\)");
    }

    private <T> List<CountItem> countBy(List<T> items, Function<T, Long> classifier) {
        return items.stream()
            .collect(Collectors.groupingBy(classifier, Collectors.counting()))
            .entrySet()
            .stream()
            .map(entry -> new CountItem(String.valueOf(entry.getKey()), entry.getValue()))
            .sorted(Comparator.comparing(CountItem::name))
            .toList();
    }

    private List<CountItem> toEnumCounts(Map<AbnormalLevel, Long> counts) {
        return counts.entrySet().stream()
            .map(entry -> new CountItem(entry.getKey().name(), entry.getValue()))
            .sorted(Comparator.comparing(CountItem::name))
            .toList();
    }

    private String safe(String value) {
        return value == null ? "" : value.replace(",", " ");
    }

    public record BusinessStatistics(
        long totalAppointments,
        long paidAppointments,
        long checkedInAppointments,
        long completedAppointments,
        long paidOrders,
        long totalRevenueCent,
        List<CountItem> packageSales,
        List<CountItem> branchPerformance,
        String dimension
    ) {
    }

    public record HealthStatistics(
        long totalReports,
        long abnormalReports,
        long criticalReports,
        List<CountItem> abnormalDistribution,
        List<CountItem> riskDistribution,
        Long branchId
    ) {
    }

    // ========== 医护按分院统计 ==========

    /**
     * 从手机号推导分院ID
     * 规则：1380000{branch}XXX -> branch digit at phone[7] (0-indexed)
     * 其他情况默认返回 1（总院）
     */
    private Long deriveBranchId(String phone) {
        if (phone == null || phone.length() < 8) return 1L;
        char branchChar = phone.charAt(7);
        if (branchChar >= '1' && branchChar <= '4') {
            return (long) (branchChar - '0');
        }
        return 1L;
    }

    public List<BranchStaff> getStaffByBranch() {
        String sql = """
            SELECT u.id, u.phone, r.code AS role_code
            FROM sys_user u
            JOIN sys_user_role ur ON u.id = ur.user_id
            JOIN sys_role r ON ur.role_id = r.id
            WHERE u.deleted = false
              AND u.enabled = true
              AND r.code IN ('NURSE', 'REVIEW_DOCTOR',
                             'EXAM_DOCTOR', 'EXAM_DOCTOR_LAB', 'EXAM_DOCTOR_IMAGING',
                             'EXAM_DOCTOR_INTERNAL', 'EXAM_DOCTOR_SURGERY',
                             'EXAM_DOCTOR_OPHTHALM', 'EXAM_DOCTOR_ENT',
                             'EXAM_DOCTOR_DENTAL', 'EXAM_DOCTOR_ECG',
                             'EXAM_DOCTOR_ULTRASOUND', 'EXAM_DOCTOR_BMD',
                             'EXAM_DOCTOR_BLOOD')
            ORDER BY u.id
            """;
        List<Map<String, Object>> rows = jdbc.queryForList(sql);
        // Group by branch and role in Java (no DB column dependency)
        java.util.Map<Long, java.util.Map<String, Integer>> branchRoleCount = new java.util.HashMap<>();
        for (Map<String, Object> row : rows) {
            String phone = (String) row.get("phone");
            String roleCode = (String) row.get("role_code");
            Long branchId = deriveBranchId(phone);
            branchRoleCount.computeIfAbsent(branchId, k -> new java.util.HashMap<>());
            branchRoleCount.get(branchId).merge(roleCode, 1, Integer::sum);
        }
        List<BranchStaff> result = new ArrayList<>();
        for (java.util.Map.Entry<Long, java.util.Map<String, Integer>> entry : branchRoleCount.entrySet()) {
            Long branchId = entry.getKey();
            for (java.util.Map.Entry<String, Integer> roleEntry : entry.getValue().entrySet()) {
                result.add(new BranchStaff(branchId, roleEntry.getKey(), roleEntry.getValue()));
            }
        }
        result.sort(java.util.Comparator.comparingLong(BranchStaff::branchId)
            .thenComparing(BranchStaff::roleCode));
        return result;
    }

    public record BranchStaff(Long branchId, String roleCode, int count) {
    }

    public record CountItem(String name, long value) {
    }

    public record ExportFile(String filename, String contentType, byte[] content) {
    }
}
