package com.nationwide.nationwide_server.member;

import com.nationwide.nationwide_server._core._entity.ImageOwner;
import com.nationwide.nationwide_server._core.util.TimeFormatUtil;
import com.nationwide.nationwide_server.image_file.ImageFile;
import com.nationwide.nationwide_server.member.dto.MemberRequestDTO;
import com.nationwide.nationwide_server.member.m_enum.Gender;
import com.nationwide.nationwide_server.member.m_enum.LoginType;
import com.nationwide.nationwide_server.member.m_enum.UserRole;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "member_tb")
public class Member implements ImageOwner {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String nickName;

    private String loginId;
    private String password;

    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    private String birth; // 생년
    private String date; // 월 일

    private String addressNumber; // 지번
    private String address; // 주소
    private String addressDetail; // 상세 주소

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ImageFile> imageFiles = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    private LoginType loginType = LoginType.LOCAL; // 로그인 타입 설정

    @CreationTimestamp
    private Timestamp createdAt; // 생성 일

    @UpdateTimestamp
    private Timestamp updatedAt; // 생성 일


    @Enumerated(EnumType.STRING)
    @Builder.Default
    private UserRole userRole = UserRole.USER;

    public String getTime(){
        return TimeFormatUtil.timestampFormat(createdAt);
    }

    private Timestamp delDate; // 삭제 일

    private boolean isEmailVerified = true; // 이메일 인증 유무
    private boolean isPhoneVerified = false; // 휴대폰 인증 유무

    @Override
    public List<ImageFile> getImageFiles() {
        return imageFiles;
    }

    @Override
    public void addImageFile(ImageFile imageFile){
        this.imageFiles.add(imageFile);
        imageFile.setMember(this);
    }

    public void updateMember(MemberRequestDTO.UpdateDTO dto){
        if(dto.getNickName() != null) {
            this.nickName = dto.getNickName();
        }
        if(dto.getPhoneNumber() != null){
            this.phoneNumber = dto.getPhoneNumber();
        }
        if(dto.getGender() != null){
            this.gender = dto.getGender();
        }
        if(dto.getBirth() != null){
            this.birth = dto.getBirth();
        }
        if(dto.getDate() != null ){
            this.date = dto.getDate();
        }
        if(dto.getAddressNumber() != null){
            this.addressNumber = dto.getAddressNumber();
        }
        if(dto.getAddress() != null){
            this.address = dto.getAddress();
        }
        if(dto.getAddressDetail() != null){
            this.addressDetail = dto.getAddressDetail();
        }
        this.updatedAt = new Timestamp(System.currentTimeMillis()); // 명시적 갱신
    }

    public boolean getIsMine(Long memberIdx){
        return this.id.equals(memberIdx);
    }

}
