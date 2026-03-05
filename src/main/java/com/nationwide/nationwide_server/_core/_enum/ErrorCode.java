package com.nationwide.nationwide_server._core._enum;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    BOARD_NOT_FOUND("B001", "해당 게시물을 찾을 수 없습니다"),
    MEMBER_NOT_FOUND("M001", "해당 회원을 찾을 수 없습니다"),
    MEMBER_ID_NOT_FOUND("M002", "아이디를 찾을 수없습니다"),
    MEMBER_NOT_MINE("M003","자신의 회원 정보가 아닙니다"),
    MEMBER_PASS_NOT_MISMATCH("M003", "비밀번호가 일치하지 않습니다"),
    MEMBER_ALREADY_EXISTS("M004", "아이디를 찾을 수없습니다"),
    TERMS_IS_AGREED("T001", "필수 약관에 동의해야 합니다"),
    EMAIL_NOT_VERIFIED("T002", "이메일 인증이 완료 되지 않았습니다"),
    COMMENT_NOT_FOUND("C001", "해당 댓글을 찾을 수 없습니다"),
    COMMENT_NOT_MINE("C002", "자신의 댓글만 수정할 수 있습니다");

    private final String code;
    private final String message;
}

