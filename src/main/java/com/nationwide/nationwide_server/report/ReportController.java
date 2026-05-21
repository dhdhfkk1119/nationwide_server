package com.nationwide.nationwide_server.report;

import com.nationwide.nationwide_server._core._custom_annotation.LoginUser;
import com.nationwide.nationwide_server._core.util.ApiUtil;
import com.nationwide.nationwide_server._core.util.SessionUser;
import com.nationwide.nationwide_server.report.dto.ReportRequestDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class ReportController {
    private final ReportService reportService;

    @PostMapping("/reports/boards/{boardId}")
    public ResponseEntity<?> reportBoard(
            @LoginUser SessionUser sessionUser,
            @PathVariable("boardId") Long boardId,
            @RequestBody ReportRequestDTO.CreateDTO dto
    ) {
        reportService.reportBoard(sessionUser, boardId, dto);
        return ResponseEntity.ok(ApiUtil.success("게시물 신고가 접수되었습니다."));
    }

    @PostMapping("/reports/comments/{commentId}")
    public ResponseEntity<?> reportComment(
            @LoginUser SessionUser sessionUser,
            @PathVariable("commentId") Long commentId,
            @RequestBody ReportRequestDTO.CreateDTO dto
    ) {
        reportService.reportComment(sessionUser, commentId, dto);
        return ResponseEntity.ok(ApiUtil.success("댓글 신고가 접수되었습니다."));
    }

    @GetMapping("/reports/me")
    public ResponseEntity<?> myReports(
            @LoginUser SessionUser sessionUser,
            @RequestParam(value = "targetType", required = false) ReportTargetType targetType
    ) {
        return ResponseEntity.ok(ApiUtil.success(reportService.myReports(sessionUser, targetType)));
    }

    @GetMapping("/admin/reports")
    public ResponseEntity<?> adminReports(
            @LoginUser SessionUser sessionUser,
            @RequestParam(value = "status", required = false) ReportStatus status,
            @RequestParam(value = "targetType", required = false) ReportTargetType targetType
    ) {
        return ResponseEntity.ok(ApiUtil.success(reportService.adminReports(sessionUser, status, targetType)));
    }

    @PutMapping("/admin/reports/{reportId}/approve")
    public ResponseEntity<?> approve(
            @LoginUser SessionUser sessionUser,
            @PathVariable("reportId") Long reportId,
            @RequestBody ReportRequestDTO.ProcessDTO dto
    ) {
        return ResponseEntity.ok(ApiUtil.success(reportService.approve(sessionUser, reportId, dto)));
    }

    @PutMapping("/admin/reports/{reportId}/reject")
    public ResponseEntity<?> reject(
            @LoginUser SessionUser sessionUser,
            @PathVariable("reportId") Long reportId,
            @RequestBody ReportRequestDTO.ProcessDTO dto
    ) {
        return ResponseEntity.ok(ApiUtil.success(reportService.reject(sessionUser, reportId, dto)));
    }
}
