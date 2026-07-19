package com.nationwide.nationwide_server.member;

import com.nationwide.nationwide_server._core._entity.ImageOwner;
import com.nationwide.nationwide_server._core.util.TimeFormatUtil;
import com.nationwide.nationwide_server.image_file.ImageFile;
import com.nationwide.nationwide_server.member.dto.MemberRequestDTO;
import com.nationwide.nationwide_server.member.m_enum.Gender;
import com.nationwide.nationwide_server.member.m_enum.LoginType;
import com.nationwide.nationwide_server.member.m_enum.MessagePermission;
import com.nationwide.nationwide_server.member.m_enum.UserRole;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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
    private String email;
    private String lastLoginIp;

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

    // 동네 사람 보기용 "현재 위치" (회원가입 주소와 별개, GPS 역지오코딩 또는 직접 설정 결과)
    private String currentLocationAddress;
    private String currentLocationAddress1;
    private String currentLocationAddress2;
    // "AUTO"(GPS 자동 설정) 또는 "MANUAL"(주소 검색으로 직접 설정)
    private String locationSource;

    @Builder.Default
    private int addressChangeCount = 0;

    @Builder.Default
    private String bio = "";

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ImageFile> imageFiles = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    private LoginType loginType = LoginType.LOCAL;

    // VARCHAR로 명시해 네이티브 MySQL ENUM 컬럼 생성을 방지한다 (향후 값 추가 시 ALTER 없이 동작하도록).
    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "VARCHAR(20)")
    @Builder.Default
    private MessagePermission messagePermission = MessagePermission.FOLLOWERS_ONLY;

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

    @Builder.Default
    private boolean isDeactivate = false;

    private Timestamp deactivateDate;
    private Timestamp deactivateUntil;
    private Timestamp deactivateCancelDate;

    @Builder.Default
    private int deactivateCount = 0;

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

    public void updatePrivacySettings(boolean isPrivateProfile, boolean isLocationVisible, MessagePermission messagePermission) {
        this.isPrivateProfile = isPrivateProfile;
        this.isLocationVisible = isLocationVisible;
        if (messagePermission != null) {
            this.messagePermission = messagePermission;
        }
        this.updatedAt = new Timestamp(System.currentTimeMillis());
    }

    public boolean isDeactivatedNow() {
        if (!isDeactivate) {
            return false;
        }

        Timestamp now = new Timestamp(System.currentTimeMillis());
        return deactivateUntil == null || deactivateUntil.after(now);
    }

    public boolean canDeactivate() {
        return deactivateCount < 3;
    }

    public int getRemainingDeactivateCount() {
        return Math.max(0, 3 - deactivateCount);
    }

    public static final int MAX_ADDRESS_CHANGE_COUNT = 5;

    public boolean canChangeAddress() {
        return addressChangeCount < MAX_ADDRESS_CHANGE_COUNT;
    }

    public int getRemainingAddressChangeCount() {
        return Math.max(0, MAX_ADDRESS_CHANGE_COUNT - addressChangeCount);
    }

    public void startDeactivation(int durationMonths) {
        Timestamp now = new Timestamp(System.currentTimeMillis());
        this.isDeactivate = true;
        this.deactivateDate = now;
        this.deactivateUntil = Timestamp.valueOf(LocalDateTime.now().plusMonths(durationMonths));
        this.deactivateCancelDate = null;
        this.deactivateCount += 1;
        this.updatedAt = now;
    }

    public void cancelDeactivation() {
        Timestamp now = new Timestamp(System.currentTimeMillis());
        this.isDeactivate = false;
        this.deactivateCancelDate = now;
        this.deactivateUntil = null;
        this.updatedAt = now;
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

    public void updateCurrentLocation(
            Double latitude,
            Double longitude,
            String currentLocationAddress,
            String currentLocationAddress1,
            String currentLocationAddress2,
            String locationSource
    ) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.currentLocationAddress = currentLocationAddress;
        this.currentLocationAddress1 = currentLocationAddress1;
        this.currentLocationAddress2 = currentLocationAddress2;
        this.locationSource = locationSource;
        this.updatedAt = new Timestamp(System.currentTimeMillis());
    }

    /**
     * 로그인 IP를 기록하고, 이전과 다른 IP인지 여부를 반환한다.
     * 첫 로그인(이전 기록 없음)은 신규 IP로 취급하지 않는다 - 알림 스팸 방지.
     */
    public boolean recordLoginIp(String ipAddress) {
        boolean isNewIp = lastLoginIp != null && !lastLoginIp.equals(ipAddress);
        this.lastLoginIp = ipAddress;
        return isNewIp;
    }

    public void clearCurrentLocation() {
        this.latitude = null;
        this.longitude = null;
        this.currentLocationAddress = null;
        this.currentLocationAddress1 = null;
        this.currentLocationAddress2 = null;
        this.locationSource = null;
        this.updatedAt = new Timestamp(System.currentTimeMillis());
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

    public static final int MAX_NICKNAME_LENGTH = 18;
    private static final String[] OAUTH_LOGIN_ID_PREFIXES = {"naver_", "kakao_", "google_"};

    // 닉네임 미설정 회원을 다른 유저에게 노출할 때, 실명 대신 email/로그인 ID 기반의 가명을 보여준다.
    // 회원 id를 시드로 써서 매 요청마다 같은 가명이 나오도록 한다(랜덤이지만 안정적).
    // 네이버/구글 로그인 ID는 "naver_"/"google_" + 발급사 고유 식별자(매우 긴 문자열)라서 그대로 쓰면
    // 너무 길어지므로, 접두사는 버리고 email의 @ 앞부분만 최대 길이만큼 사용한다.
    public String getDisplayNickName() {
        if (nickName != null && !nickName.isBlank()) {
            return nickName;
        }

        String localPart;

        if (email != null && !email.isBlank() && email.contains("@")) {
            localPart = email.substring(0, email.indexOf('@'));
        } else if (loginId != null && !loginId.isBlank()) {
            String providerPrefix = extractProviderPrefix();
            localPart = loginId.startsWith(providerPrefix) ? loginId.substring(providerPrefix.length()) : loginId;
        } else {
            localPart = "user";
        }

        if (localPart.isBlank()) {
            localPart = "user";
        }
        if (localPart.length() > MAX_NICKNAME_LENGTH) {
            localPart = localPart.substring(0, MAX_NICKNAME_LENGTH);
        }

        return localPart + "@" + randomSuffix();
    }

    private String extractProviderPrefix() {
        if (loginId == null) {
            return "";
        }
        for (String prefix : OAUTH_LOGIN_ID_PREFIXES) {
            if (loginId.startsWith(prefix)) {
                return prefix;
            }
        }
        return "";
    }

    private String randomSuffix() {
        Random random = new Random(id != null ? id : 0L);
        String chars = "abcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            builder.append(chars.charAt(random.nextInt(chars.length())));
        }
        return builder.toString();
    }
}
