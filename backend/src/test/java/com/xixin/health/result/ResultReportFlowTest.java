package com.xixin.health.result;

import com.xixin.health.exam.ExamRecord;
import com.xixin.health.exam.ExamRecordRepository;
import com.xixin.health.report.ExamReport;
import com.xixin.health.report.ReportService;
import com.xixin.health.report.ReportStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
    "spring.flyway.enabled=false",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
@Transactional
class ResultReportFlowTest {

    @Autowired
    ResultService resultService;

    @Autowired
    ReportService reportService;

    @Autowired
    ExamRecordRepository examRecordRepository;

    @Test
    void marksAbnormalResultAndGeneratesReportAfterReview() {
        ExamRecord examRecord = examRecordRepository.save(new ExamRecord("TEST-" + System.currentTimeMillis(), 1L, 1L, 1L, 1L));

        CheckResult result = resultService.recordNumericResult(examRecord.getId(), 1L, "Blood Glucose", "mmol/L", 8.2, 3.9, 6.1, 11.1, Double.NaN, 2L);
        assertThat(result.getAbnormalLevel()).isEqualTo(AbnormalLevel.ABNORMAL);

        resultService.reviewResult(result.getId(), 3L, true, "Confirmed abnormal");
        ExamReport report = reportService.generateReport(examRecord.getId(), 3L);

        assertThat(report.getReportStatus()).isEqualTo(ReportStatus.CONFIRMED);
        assertThat(report.getAbnormalStatus()).isEqualTo(AbnormalLevel.ABNORMAL);
    }
}
