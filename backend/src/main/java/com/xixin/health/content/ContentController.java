package com.xixin.health.content;

import com.xixin.health.common.api.ApiResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class ContentController {

    private final JdbcTemplate jdbc;

    public ContentController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @GetMapping("/content/{key}")
    public ApiResponse<String> getContent(@PathVariable String key) {
        List<String> results = jdbc.query(
            "SELECT content_value FROM t_site_content WHERE content_key = ?",
            (rs, row) -> rs.getString(1), key);
        return ApiResponse.success(results.isEmpty() ? "" : results.get(0));
    }

    @GetMapping("/content/batch")
    public ApiResponse<Map<String, String>> getBatchContent(@RequestParam List<String> keys) {
        if (keys == null || keys.isEmpty()) return ApiResponse.success(Map.of());
        String placeholders = String.join(",", keys.stream().map(k -> "?").toList());
        List<Map<String, Object>> rows = jdbc.queryForList(
            "SELECT content_key, content_value FROM t_site_content WHERE content_key IN (" + placeholders + ")", keys.toArray());
        Map<String, String> result = new java.util.LinkedHashMap<>();
        for (Map<String, Object> row : rows) result.put((String) row.get("content_key"), (String) row.get("content_value"));
        return ApiResponse.success(result);
    }

    @PutMapping("/admin/content/{key}")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ApiResponse<Void> updateContent(@PathVariable String key, @RequestBody Map<String, String> body) {
        jdbc.update("INSERT INTO t_site_content (content_key, content_value) VALUES (?, ?) ON DUPLICATE KEY UPDATE content_value = VALUES(content_value)", key, body.get("value"));
        return ApiResponse.success(null);
    }

    @PostMapping("/contact")
    public ApiResponse<Void> submitContact(@RequestBody Map<String, String> body) {
        Long userId = null;
        if (body.get("userId") != null && !body.get("userId").isBlank()) {
            try { userId = Long.parseLong(body.get("userId")); } catch (Exception ignored) {}
        }
        jdbc.update("INSERT INTO t_contact_message (name, phone, user_id, content) VALUES (?, ?, ?, ?)", body.get("name"), body.get("phone"), userId, body.get("content"));
        return ApiResponse.success(null);
    }

    @GetMapping("/admin/contact-messages")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'OPERATIONS_ADMIN')")
    public ApiResponse<List<Map<String, Object>>> getMessages() {
        return ApiResponse.success(jdbc.queryForList("SELECT id, name, phone, content, reply, replied_at, is_read, created_at FROM t_contact_message ORDER BY created_at DESC"));
    }

    @GetMapping("/contact/messages")
    public ApiResponse<List<Map<String, Object>>> getUserMessages(@RequestParam String phone) {
        return ApiResponse.success(jdbc.queryForList("SELECT id, name, content, reply, replied_at, created_at FROM t_contact_message WHERE phone = ? ORDER BY created_at DESC", phone));
    }

    @PutMapping("/admin/contact-messages/{id}/reply")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'OPERATIONS_ADMIN')")
    public ApiResponse<Void> replyMessage(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String reply = body.get("reply");
        jdbc.update("UPDATE t_contact_message SET reply = ?, replied_at = NOW(), is_read = TRUE WHERE id = ?", reply, id);
        List<Long> userIds = jdbc.query("SELECT user_id FROM t_contact_message WHERE id = ? AND user_id IS NOT NULL", (rs, row) -> rs.getLong(1), id);
        if (!userIds.isEmpty()) {
            Long userId = userIds.get(0);
            String content = "您有一条客服回复留言：\n" + reply;
            jdbc.update("INSERT INTO t_notification (user_id, notification_type, title, content, read_flag, created_at) VALUES (?, 'SYSTEM', ?, ?, FALSE, NOW())", userId, "客服回复通知", content);
        }
        return ApiResponse.success(null);
    }

    @PutMapping("/admin/contact-messages/{id}/read")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'OPERATIONS_ADMIN')")
    public ApiResponse<Void> markRead(@PathVariable Long id) {
        jdbc.update("UPDATE t_contact_message SET is_read = TRUE WHERE id = ?", id);
        return ApiResponse.success(null);
    }

    @DeleteMapping("/admin/contact-messages/{id}")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'OPERATIONS_ADMIN')")
    public ApiResponse<Void> deleteMessage(@PathVariable Long id) {
        jdbc.update("DELETE FROM t_contact_message WHERE id = ?", id);
        return ApiResponse.success(null);
    }
}
