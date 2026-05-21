package com.nationwide.nationwide_server.report;

import com.nationwide.nationwide_server._core.errors.exception.Exception400;
import com.nationwide.nationwide_server._core.errors.exception.Exception401;
import com.nationwide.nationwide_server._core.errors.exception.Exception403;
import com.nationwide.nationwide_server._core.errors.exception.Exception404;
import com.nationwide.nationwide_server._core.util.SessionUser;
import com.nationwide.nationwide_server.board.Board;
import com.nationwide.nationwide_server.board.BoardRepository;
import com.nationwide.nationwide_server.board_comment.BoardComment;
import com.nationwide.nationwide_server.board_comment.BoardCommentRepository;
import com.nationwide.nationwide_server.member.Member;
import com.nationwide.nationwide_server.member.MemberService;
import com.nationwide.nationwide_server.member.m_enum.UserRole;
import com.nationwide.nationwide_server.report.dto.ReportRequestDTO;
import com.nationwide.nationwide_server.report.dto.ReportResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportService {
    private final ReportRepository reportRepository;
    private final MemberService memberService;
    private final BoardRepository boardRepository;
    private final BoardCommentRepository boardCommentRepository;

    @Transactional
    public void reportBoard(SessionUser sessionUser, Long boardId, ReportRequestDTO.CreateDTO dto) {
        Member reporter = validateReporter(sessionUser);
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new Exception404("신고할 게시물을 찾을 수 없습니다."));

        if (board.getMember().getId().equals(reporter.getId())) {
            throw new Exception400("본인 게시물은 신고할 수 없습니다.");
        }
        validateReporterComment(dto.getReporterComment());
        validateDuplicate(
                reporter.getId(),
                ReportTargetType.BOARD,
                boardId,
                "본인이 이미 해당 게시물에 대해서 신고를 했습니다.\n신고 처리 완료 이후 가능합니다."
        );

        reportRepository.save(
                Report.builder()
                        .reporter(reporter)
                        .targetType(ReportTargetType.BOARD)
                        .targetId(boardId)
                        .reporterComment(dto.getReporterComment().trim())
                        .status(ReportStatus.RECEIVED)
                        .build()
        );
    }

    @Transactional
    public void reportComment(SessionUser sessionUser, Long commentId, ReportRequestDTO.CreateDTO dto) {
        Member reporter = validateReporter(sessionUser);
        BoardComment comment = boardCommentRepository.findById(commentId)
                .orElseThrow(() -> new Exception404("신고할 댓글을 찾을 수 없습니다."));

        if (comment.getMember().getId().equals(reporter.getId())) {
            throw new Exception400("본인 댓글은 신고할 수 없습니다.");
        }
        validateReporterComment(dto.getReporterComment());
        validateDuplicate(
                reporter.getId(),
                ReportTargetType.COMMENT,
                commentId,
                "이미 신고가 접수된 댓글입니다.\n신고 처리 완료 이후 가능합니다."
        );

        reportRepository.save(
                Report.builder()
                        .reporter(reporter)
                        .targetType(ReportTargetType.COMMENT)
                        .targetId(commentId)
                        .reporterComment(dto.getReporterComment().trim())
                        .status(ReportStatus.RECEIVED)
                        .build()
        );
    }

    public ReportResponseDTO.ReportListDTO myReports(SessionUser sessionUser, ReportTargetType targetType) {
        if (sessionUser == null) {
            throw new Exception401("로그인이 필요합니다.");
        }

        List<ReportResponseDTO.ItemDTO> items = reportRepository.findByReporterId(sessionUser.getId(), targetType)
                .stream()
                .map(this::mapReport)
                .toList();

        return ReportResponseDTO.ReportListDTO.of(items);
    }

    public ReportResponseDTO.ReportListDTO adminReports(
            SessionUser sessionUser,
            ReportStatus status,
            ReportTargetType targetType
    ) {
        Member admin = validateAdmin(sessionUser);
        List<ReportResponseDTO.ItemDTO> items = reportRepository.findForAdmin(status, targetType)
                .stream()
                .map(this::mapReport)
                .toList();
        return ReportResponseDTO.ReportListDTO.of(items);
    }

    @Transactional
    public ReportResponseDTO.ItemDTO approve(SessionUser sessionUser, Long reportId, ReportRequestDTO.ProcessDTO dto) {
        Member admin = validateAdmin(sessionUser);
        Report report = findReport(reportId);
        validateAdminComment(dto.getAdminComment());

        if (report.getStatus() != ReportStatus.RECEIVED) {
            throw new Exception400("이미 처리된 신고입니다.");
        }

        if (report.getTargetType() == ReportTargetType.BOARD) {
            Board board = boardRepository.findById(report.getTargetId())
                    .orElseThrow(() -> new Exception404("게시물을 찾을 수 없습니다."));
            board.setDelDate(new Timestamp(System.currentTimeMillis()));
            boardCommentRepository.deleteByBoardIdSoft(board.getId());
        } else {
            BoardComment comment = boardCommentRepository.findById(report.getTargetId())
                    .orElseThrow(() -> new Exception404("댓글을 찾을 수 없습니다."));
            comment.setDelDate(new Timestamp(System.currentTimeMillis()));
        }

        report.approve(admin, dto.getAdminComment().trim());
        return mapReport(report);
    }

    @Transactional
    public ReportResponseDTO.ItemDTO reject(SessionUser sessionUser, Long reportId, ReportRequestDTO.ProcessDTO dto) {
        Member admin = validateAdmin(sessionUser);
        Report report = findReport(reportId);
        validateAdminComment(dto.getAdminComment());

        if (report.getStatus() != ReportStatus.RECEIVED) {
            throw new Exception400("이미 처리된 신고입니다.");
        }

        report.reject(admin, dto.getAdminComment().trim());
        return mapReport(report);
    }

    private ReportResponseDTO.ItemDTO mapReport(Report report) {
        if (report.getTargetType() == ReportTargetType.BOARD) {
            Board board = boardRepository.findById(report.getTargetId())
                    .orElseThrow(() -> new Exception404("게시물을 찾을 수 없습니다."));
            return ReportResponseDTO.ItemDTO.ofBoard(report, board);
        }

        BoardComment comment = boardCommentRepository.findById(report.getTargetId())
                .orElseThrow(() -> new Exception404("댓글을 찾을 수 없습니다."));
        return ReportResponseDTO.ItemDTO.ofComment(report, comment);
    }

    private Member validateReporter(SessionUser sessionUser) {
        if (sessionUser == null) {
            throw new Exception401("로그인이 필요합니다.");
        }

        memberService.validateActiveMember(sessionUser.getId());
        return memberService.findById(sessionUser.getId());
    }

    private Member validateAdmin(SessionUser sessionUser) {
        Member member = validateReporter(sessionUser);
        if (member.getUserRole() != UserRole.ADMIN) {
            throw new Exception403("관리자만 처리할 수 있습니다.");
        }
        return member;
    }

    private void validateDuplicate(Long reporterId, ReportTargetType targetType, Long targetId, String message) {
        if (reportRepository.existsByReporterIdAndTargetTypeAndTargetIdAndStatus(
                reporterId,
                targetType,
                targetId,
                ReportStatus.RECEIVED
        )) {
            throw new Exception400(message);
        }
    }

    private void validateReporterComment(String reporterComment) {
        if (reporterComment == null || reporterComment.trim().isBlank()) {
            throw new Exception400("신고 내용을 입력해 주세요.");
        }
    }

    private void validateAdminComment(String adminComment) {
        if (adminComment == null || adminComment.trim().isBlank()) {
            throw new Exception400("처리 사유를 입력해 주세요.");
        }
    }

    private Report findReport(Long reportId) {
        return reportRepository.findDetailById(reportId)
                .orElseThrow(() -> new Exception404("신고 접수 내역을 찾을 수 없습니다."));
    }
}
