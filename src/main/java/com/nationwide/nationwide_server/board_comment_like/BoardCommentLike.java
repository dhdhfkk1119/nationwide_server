package com.nationwide.nationwide_server.board_comment_like;

import com.nationwide.nationwide_server.board.Board;
import com.nationwide.nationwide_server.board_comment.BoardComment;
import com.nationwide.nationwide_server.member.Member;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.sql.Timestamp;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "board_comment_like_tb")
public class BoardCommentLike {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long boardCommentLikeIdx;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_idx")
    private Board board;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_comment_idx")
    private BoardComment boardComment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_idx")
    private Member member;

    @CreationTimestamp
    private Timestamp createdAt;

    public String getTime() {
        return com.nationwide.nationwide_server._core.util.TimeFormatUtil.timestampFormat(createdAt);
    }


}
