package com.nationwide.nationwide_server.member_terms;

import com.nationwide.nationwide_server.member.Member;
import com.nationwide.nationwide_server.terms.Terms;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.sql.Timestamp;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@Table(name = "member_terms_tb")
public class MemberTerms {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY )
    @JoinColumn(name = "member_id")
    private Member memberId;

    @ManyToOne(fetch = FetchType.LAZY )
    @JoinColumn(name = "terms_id")
    private Terms termsId;

    @CreationTimestamp
    private Timestamp agreedAt;
}
