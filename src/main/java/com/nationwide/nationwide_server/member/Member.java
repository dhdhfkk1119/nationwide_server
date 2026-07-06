package com.nationwide.nationwide_server.member;

import com.nationwide.nationwide_server._core._entity.ImageOwner;
import com.nationwide.nationwide_server._core.util.TimeFormatUtil;
import com.nationwide.nationwide_server.image_file.ImageFile;
import com.nationwide.nationwide_server.member.dto.MemberRequestDTO;
import com.nationwide.nationwide_server.member.m_enum.Gender;
import com.nationwide.nationwide_server.member.m_enum.LoginType;
import com.nationwide.nationwide_server.member.m_enum.UserRole;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
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
@ToString(exclude = {"imageFiles"})
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

    private String birth;
    private String date;

    private String addressNumber;
    private String address;
    private String addressDetail;
    private Double latitude;
    private Double longitude;
    private String geocodedAddress;

    @Builder.Default
    private int addressChangeCount = 0;

    @Builder.Default
    private String bio = "";

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ImageFile> imageFiles = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    private LoginType loginType = LoginType.LOCAL;

    @CreationTimestamp
    private Timestamp createdAt;

    @UpdateTimestamp
    private Timestamp updatedAt;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private UserRole userRole = UserRole.USER;

    private Timestamp delDate;

    private boolean isEmailVerified = true;
    private boolean isPhoneVerified = false;

    @Builder.Default
    private boolean isPrivateProfile = false;

    @Builder.Default
    private boolean isLocationVisible = true;

    public String getTime() {
        return TimeFormatUtil.timestampFormat(createdAt);
    }

    @Override
    public List<ImageFile> getImageFiles() {
        return imageFiles;
    }

    @Override
    public void addImageFile(ImageFile imageFile) {
        this.imageFiles.add(imageFile);
        imageFile.setMember(this);
    }

    public void updateMember(MemberRequestDTO.UpdateDTO dto) {
        boolean addressChanged = false;

        if (dto.getName() != null) {
            this.name = dto.getName();
        }
        if (dto.getNickName() != null) {
            this.nickName = dto.getNickName();
        }
        if (dto.getPhoneNumber() != null) {
            this.phoneNumber = dto.getPhoneNumber();
        }
        if (dto.getGender() != null) {
            this.gender = dto.getGender();
        }
        if (dto.getBirth() != null) {
            this.birth = dto.getBirth();
        }
        if (dto.getDate() != null) {
            this.date = dto.getDate();
        }
        if (dto.getAddressNumber() != null) {
            addressChanged = addressChanged || !dto.getAddressNumber().equals(this.addressNumber);
            this.addressNumber = dto.getAddressNumber();
        }
        if (dto.getAddress() != null) {
            addressChanged = addressChanged || !dto.getAddress().equals(this.address);
            this.address = dto.getAddress();
        }
        if (dto.getAddressDetail() != null) {
            addressChanged = addressChanged || !dto.getAddressDetail().equals(this.addressDetail);
            this.addressDetail = dto.getAddressDetail();
        }
        if (dto.getBio() != null) {
            this.bio = dto.getBio();
        }
        if (dto.getIsPrivateProfile() != null) {
            this.isPrivateProfile = dto.getIsPrivateProfile();
        }
        if (dto.getIsLocationVisible() != null) {
            this.isLocationVisible = dto.getIsLocationVisible();
        }
        if (addressChanged) {
            this.addressChangeCount += 1;
        }
        this.updatedAt = new Timestamp(System.currentTimeMillis());
    }

    public void updatePrivacySettings(boolean isPrivateProfile, boolean isLocationVisible) {
        this.isPrivateProfile = isPrivateProfile;
        this.isLocationVisible = isLocationVisible;
        this.updatedAt = new Timestamp(System.currentTimeMillis());
    }

    public boolean getIsMine(Long memberIdx) {
        return this.id.equals(memberIdx);
    }

    public void updateCoordinates(Double latitude, Double longitude, String geocodedAddress) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.geocodedAddress = geocodedAddress;
        this.updatedAt = new Timestamp(System.currentTimeMillis());
    }

    public void clearCoordinates() {
        updateCoordinates(null, null, null);
    }

    public String getFullAddress() {
        StringBuilder builder = new StringBuilder();

        if (address != null && !address.isBlank()) {
            builder.append(address.trim());
        }
        if (addressDetail != null && !addressDetail.isBlank()) {
            if (!builder.isEmpty()) {
                builder.append(' ');
            }
            builder.append(addressDetail.trim());
        }

        return builder.toString().trim();
    }

    public boolean hasCoordinates() {
        return latitude != null && longitude != null;
    }
}
