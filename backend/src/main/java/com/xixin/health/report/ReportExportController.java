package com.xixin.health.report;

import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.OutputStream;

/**
 * 报告导出控制器 —— 支持将体检报告导出为 PDF
 */
@RestController
@RequestMapping("/api/v1/reports")
public class ReportExportController {

    private static final Logger log = LoggerFactory.getLogger(ReportExportController.class);

    private final ReportPdfService reportPdfService;

    public ReportExportController(ReportPdfService reportPdfService) {
        this.reportPdfService = reportPdfService;
    }

    /**
     * 导出体检报告 PDF（支持中文）
     * GET /api/v1/reports/{reportId}/pdf
     */
    @GetMapping("/{reportId}/pdf")
    public void exportPdf(@PathVariable Long reportId, HttpServletResponse response) throws Exception {
        try {
            // 生成 PDF
            byte[] pdfBytes = reportPdfService.generatePdf(reportId);

            // 设置响应头
            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", "attachment; filename=report_" + reportId + ".pdf");
            response.setContentLength(pdfBytes.length);

            // 输出
            try (OutputStream os = response.getOutputStream()) {
                os.write(pdfBytes);
                os.flush();
            }
        } catch (Exception e) {
            log.error("PDF生成失败 reportId={}", reportId, e);
            response.sendError(500, "PDF生成失败: " + e.getMessage());
        }
    }
}
