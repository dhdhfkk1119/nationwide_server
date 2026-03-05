package com.nationwide.nationwide_server._core._enum;

public enum ResourceType {
    BOARD("게시물"),
    MEMBER("회원"),
    TERM("약관"),
    COMMENT("댓글");

    private final String displayName;

    ResourceType(String displayName) {
        this.displayName = displayName;
    }

    public String getSaveSuccess() {
        return displayName + " 작성에 성공했습니다";
    }

    public String getToggleLike(boolean isLike) {
        if(isLike) {
            return displayName + " 좋아요 누르셨습니다";
        } else {
            return displayName + " 좋아요 취소하셨습니다";
        }

    }

    public String getDeleteSuccess() {
        return displayName + " 삭제에 성공했습니다";
    }

    public String getUpdateSuccess() {
        return displayName + " 수정에 성공했습니다";
    }
}
