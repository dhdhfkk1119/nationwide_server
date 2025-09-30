package com.nationwide.nationwide_server.member;

import com.nationwide.nationwide_server.core.util.TimeFormatUtil;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.sql.Timestamp;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "member_tb")
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String nickName;

    private String MemberId;
    private String password;

    private String phoneNumber;
    private boolean phoneVerified;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    private String birth; // 생년
    private String date; // 월 일

    private String addressNumber;
    private String address;
    private String addressDetail;

    private String profileImage;

    @CreationTimestamp
    private Timestamp createdAt;

    public String getTime(){
        return TimeFormatUtil.timestampFormat(createdAt);
    }



}
