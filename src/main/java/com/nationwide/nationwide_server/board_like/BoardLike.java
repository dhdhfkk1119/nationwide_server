package com.nationwide.nationwide_server.board_like;

import com.nationwide.nationwide_server.board.Board;
import com.nationwide.nationwide_server._core.util.TimeFormatUtil;
import com.nationwide.nationwide_server.member.Member;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;

import java.sql.Timestamp;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"board", "member"})
@Table(name = "board_like_tb")
public class BoardLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long boardLikeIdx;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_idx")
    private Board board;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_idx")
    private Member member;

    @CreationTimestamp
    private Timestamp createdAt;

    public String getTime(){
        return TimeFormatUtil.timestampFormat(createdAt);
    }
}
