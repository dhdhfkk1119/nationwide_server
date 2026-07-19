package com.nationwide.nationwide_server.member;

import com.nationwide.nationwide_server._core.errors.exception.Exception400;
import com.nationwide.nationwide_server._core.errors.exception.Exception401;
import com.nationwide.nationwide_server._core.errors.exception.Exception404;
import com.nationwide.nationwide_server._core.jwt.JwtTokenProvider;
import com.nationwide.nationwide_server._core.util.SessionUser;
import com.nationwide.nationwide_server.board.BoardRepository;
import com.nationwide.nationwide_server.email.EmailRepository;
import com.nationwide.nationwide_server.email.EmailService;
import com.nationwide.nationwide_server.follow.Follow;
import com.nationwide.nationwide_server.follow.FollowRepository;
import com.nationwide.nationwide_server.image_file.ImageFile;
import com.nationwide.nationwide_server.image_file.ImageFileService;
import com.nationwide.nationwide_server.image_file.dto.ImageResponseDTO;
import com.nationwide.nationwide_server.location.LocationCoordinate;
import com.nationwide.nationwide_server.location.LocationService;
import com.nationwide.nationwide_server.member.dto.MemberRequestDTO;
import com.nationwide.nationwide_server.member.dto.MemberResponseDTO;
import com.nationwide.nationwide_server.message.MessagePermissionService;
import com.nationwide.nationwide_server.member_terms.MemberTerms;
import com.nationwide.nationwide_server.member_terms.MemberTermsRepository;
import com.nationwide.nationwide_server.terms.Terms;
import com.nationwide.nationwide_server.terms.TermsRepository;
import com.nationwide.nationwide_server.terms.TermsService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.nationwide.nationwide_server._core._enum.ErrorCode.EMAIL_NOT_VERIFIED;
import static com.nationwide.nationwide_server._core._enum.ErrorCode.MEMBER_ALREADY_EXISTS;
import static com.nationwide.nationwide_server._core._enum.ErrorCode.MEMBER_ID_NOT_FOUND;
import static com.nationwide.nationwide_server._core._enum.ErrorCode.MEMBER_NOT_FOUND;
import static com.nationwide.nationwide_server._core._enum.ErrorCode.MEMBER_NOT_MINE;
import static com.nationwide.nationwide_server._core._enum.ErrorCode.MEMBER_PASS_NOT_MISMATCH;
import static com.nationwide.nationwide_server._core._enum.ErrorCode.TERMS_IS_AGREED;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {
    private final MemberRepository memberRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final MemberTermsRepository memberTermsRepository;
    private final TermsService termsService;
    private final EmailRepository emailRepository;
    private final EmailService emailService;
    private final TermsRepository termsRepository;
    private final ImageFileService imageFileService;
    private final JwtTokenProvider jwtTokenProvider;
    private final BoardRepository boardRepository;
    private final FollowRepository followRepository;
    private final LocationService locationService;
    private final MessagePermissionService messagePermissionService;

    @Transactional
    public void save(MemberRequestDTO.SaveDTO saveDTO) {
        Member member = saveDTO.toEntity();

        if (member.getNickName() != null && member.getNickName().length() > Member.MAX_NICKNAME_LENGTH) {
            throw new Exception400("닉네임은 최대 " + Member.MAX_NICKNAME_LENGTH + "자까지 입력할 수 있습니다.");
        }

        emailService.findByLoginId(saveDTO.getLoginId());

        if (!emailRepository.existsByLoginId(saveDTO.getLoginId())) {
            throw new Exception400(EMAIL_NOT_VERIFIED.getMessage());
        }

        if (existsByLoginId(saveDTO.getLoginId())) {
            throw new Exception401(MEMBER_ALREADY_EXISTS.getMessage());
        }

        List<Terms> requiredTerms = termsRepository.findByIsRequired();
        List<Long> requiredIds = requiredTerms.stream()
                .map(Terms::getId)
                .collect(Collectors.toList());

        if (!saveDTO.getAgreedTermsIds().containsAll(requiredIds)) {
            throw new Exception400(TERMS_IS_AGREED.getMessage());
        }

        member.setPassword(bCryptPasswordEncoder.encode(member.getPassword()));
        member.setEmail(saveDTO.getLoginId());
        memberRepository.save(member);
        syncMemberLocation(member, true);

        List<MemberTerms> memberTermsList = new ArrayList<>();
        for (Long terms : saveDTO.getAgreedTermsIds()) {
            Terms termsTo = termsService.findByTermsId(terms);
            memberTermsList.add(MemberTerms.builder()
                    .memberId(member)
                    .termsId(termsTo)
                    .build());
        }

        member.setEmailVerified(true);
        memberTermsRepository.saveAll(memberTermsList);
        emailRepository.deleteByLoginId(saveDTO.getLoginId());
    }

    // 회원 로그인
    @Transactional
    public MemberResponseDTO.LoginDTO loginMember(MemberRequestDTO.LoginDTO dto, String ipAddress){
        Member member = findByLoginId(dto.getLoginId());

        if (!bCryptPasswordEncoder.matches(dto.getPassword(), member.getPassword())) {
            throw new Exception401(MEMBER_PASS_NOT_MISMATCH.getMessage());
        }

        syncMemberLocation(member, false);
        emailService.notifyIfNewLoginLocation(member, ipAddress);

        String accessToken = jwtTokenProvider.createAccessToken(member);
        String refreshToken = dto.isAutoLogin() ? jwtTokenProvider.createRefreshToken(member) : null;
        Long expiresIn = jwtTokenProvider.getAccessExpirationSeconds();

        String thumbnailImagePath = member.getImageFiles().stream()
                .findFirst()
                .map(ImageFile::getImageFilePath)
                .orElse(null);

        return new MemberResponseDTO.LoginDTO(
                member,
                accessToken,
                refreshToken,
                expiresIn,
                thumbnailImagePath
        );
    }

    @Transactional
    public void updateMember(
            Long memberIdx,
            SessionUser sessionUser,
            MemberRequestDTO.UpdateDTO updateDTO,
            List<MultipartFile> file
    ) {
        Member member = findById(memberIdx);
        if (!member.getIsMine(sessionUser.getId())) {
            throw new Exception401(MEMBER_NOT_MINE.getMessage());
        }

        boolean isAddressChanging = isAddressChanging(member, updateDTO);
        if (isAddressChanging && !member.canChangeAddress()) {
            throw new Exception400("도로명 주소는 최대 " + Member.MAX_ADDRESS_CHANGE_COUNT + "회까지만 변경할 수 있습니다.");
        }

        if (updateDTO.getNickName() != null && updateDTO.getNickName().length() > Member.MAX_NICKNAME_LENGTH) {
            throw new Exception400("닉네임은 최대 " + Member.MAX_NICKNAME_LENGTH + "자까지 입력할 수 있습니다.");
        }

        // 기존 이미지 중에 다른 값 있으면 삭제
        if(updateDTO.getImageFileId() != null){
            imageFileService.syncDeleteImages(member,updateDTO.getImageFileId());
        }

        if (file != null && !file.isEmpty()) {
            imageFileService.addNewImages(member, file);
        }

        member.updateMember(updateDTO);
        syncMemberLocation(member, isAddressChanging);
    }

    public MemberResponseDTO.DetailDTO detail(Long memberId) {
        Member member = findById(memberId);
        List<ImageResponseDTO> imageFileDTOs = member.getImageFiles().stream()
                .map(ImageResponseDTO::new)
                .toList();
        Long boardCnt = boardRepository.countByMemberId(memberId);
        Long followerCnt = followRepository.countFollowersByMemberId(memberId);
        Long followingCnt = followRepository.countFollowingByMemberId(memberId);

        return new MemberResponseDTO.DetailDTO(
                member,
                imageFileDTOs,
                boardCnt,
                followerCnt,
                followingCnt
        );
    }

    @Transactional
    public MemberResponseDTO.PrivacySettingsDTO updatePrivacySettings(
            SessionUser sessionUser,
            MemberRequestDTO.PrivacySettingsDTO dto
    ) {
        if (sessionUser == null) {
            throw new Exception401("濡쒓렇?몄씠 ?꾩슂?⑸땲??");
        }

        Member member = findById(sessionUser.getId());
        member.updatePrivacySettings(dto.isPrivateProfile(), dto.isLocationVisible(), dto.getMessagePermission());
        return MemberResponseDTO.PrivacySettingsDTO.of(member);
    }

    @Transactional
    public MemberResponseDTO.DeactivationStatusDTO deactivate(
            SessionUser sessionUser,
            MemberRequestDTO.DeactivateRequestDTO dto
    ) {
        if (sessionUser == null) {
            throw new Exception401("濡쒓렇?몄씠 ?꾩슂?⑸땲??");
        }
        if (dto.getDurationMonths() < 1 || dto.getDurationMonths() > 12) {
            throw new Exception400("鍮꾪솢?깊솕 湲곌컙??1媛쒖썡遺??곗씠?먯꽌 12媛쒖썡源뚯? ?좏깮?섑빐二쇱꽭??");
        }

        Member member = findById(sessionUser.getId());
        if (member.isDeactivatedNow()) {
            throw new Exception400("?대? 鍮꾪솢?깊솕 以묒씤 怨꾩젙?낅땲??");
        }
        if (!member.canDeactivate()) {
            throw new Exception400("鍮꾪솢?깊솕??理쒕? 3踰덇퉴吏留?媛��ν빀?덈떎.");
        }

        member.startDeactivation(dto.getDurationMonths());
        return MemberResponseDTO.DeactivationStatusDTO.of(member);
    }

    @Transactional
    public MemberResponseDTO.DeactivationStatusDTO cancelDeactivation(SessionUser sessionUser) {
        if (sessionUser == null) {
            throw new Exception401("濡쒓렇?몄씠 ?꾩슂?⑸땲??");
        }

        Member member = findById(sessionUser.getId());
        if (!member.isDeactivatedNow()) {
            return MemberResponseDTO.DeactivationStatusDTO.of(member);
        }

        member.cancelDeactivation();
        return MemberResponseDTO.DeactivationStatusDTO.of(member);
    }

    public boolean isDeactivated(Member member) {
        return member != null && member.isDeactivatedNow();
    }

    public boolean canExposeMember(Member member, Long viewerId) {
        return !isDeactivated(member) || (viewerId != null && viewerId.equals(member.getId()));
    }

    public void validateActiveMember(Long memberId) {
        Member member = findById(memberId);
        if (member.isDeactivatedNow()) {
            throw new Exception400("鍮꾪솢?깊솕 以묒씤 怨꾩젙??湲곕뒫???댁슜?????놁뒿?덈떎.");
        }
    }

    public Member findById(Long memberId) {
        return memberRepository.findByMemberId(memberId)
                .orElseThrow(() -> new Exception404(MEMBER_NOT_FOUND.getMessage()));
    }

    public boolean existsByLoginId(String loginId) {
        return memberRepository.existsByLoginId(loginId);
    }

    public Member findByLoginId(String loginId) {
        return memberRepository.findByLoginId(loginId)
                .orElseThrow(() -> new Exception404(MEMBER_ID_NOT_FOUND.getMessage()));
    }

    private boolean isAddressChanging(Member member, MemberRequestDTO.UpdateDTO updateDTO) {
        String nextAddressNumber = updateDTO.getAddressNumber() != null
                ? updateDTO.getAddressNumber()
                : member.getAddressNumber();
        String nextAddress = updateDTO.getAddress() != null ? updateDTO.getAddress() : member.getAddress();
        String nextAddressDetail = updateDTO.getAddressDetail() != null
                ? updateDTO.getAddressDetail()
                : member.getAddressDetail();

        return !Objects.equals(nextAddressNumber, member.getAddressNumber())
                || !Objects.equals(nextAddress, member.getAddress())
                || !Objects.equals(nextAddressDetail, member.getAddressDetail());
    }

    private void syncMemberLocation(Member member, boolean forceRefresh) {
        String fullAddress = member.getFullAddress();

        if (fullAddress.isBlank()) {
            member.clearCoordinates();
            return;
        }

        if (!forceRefresh
                && member.hasCoordinates()
                && fullAddress.equals(member.getGeocodedAddress())) {
            return;
        }

        Optional<LocationCoordinate> coordinate = locationService.geocodeRoadAddress(fullAddress);
        if (coordinate.isPresent()) {
            LocationCoordinate locationCoordinate = coordinate.get();
            member.updateCoordinates(
                    locationCoordinate.latitude(),
                    locationCoordinate.longitude(),
                    fullAddress
            );
            return;
        }

        if (forceRefresh) {
            member.clearCoordinates();
        }
    }

    @Transactional
    public MemberResponseDTO.CurrentLocationDTO updateCurrentLocation(
            SessionUser sessionUser,
            MemberRequestDTO.CurrentLocationDTO dto
    ) {
        if (sessionUser == null) {
            throw new Exception401("로그인이 필요합니다.");
        }
        if (dto.getLatitude() == null || dto.getLongitude() == null) {
            throw new Exception400("위치 좌표가 올바르지 않습니다.");
        }

        Member member = findById(sessionUser.getId());

        LocationService.ReverseGeocodeResult result = locationService
                .reverseGeocode(dto.getLatitude(), dto.getLongitude())
                .orElseThrow(() -> new Exception400("위치를 확인할 수 없습니다."));

        member.updateCurrentLocation(
                dto.getLatitude(),
                dto.getLongitude(),
                result.address(),
                result.address1(),
                result.address2(),
                "AUTO"
        );

        return MemberResponseDTO.CurrentLocationDTO.of(member);
    }

    @Transactional
    public MemberResponseDTO.CurrentLocationDTO updateCurrentLocationManually(
            SessionUser sessionUser,
            MemberRequestDTO.ManualLocationDTO dto
    ) {
        if (sessionUser == null) {
            throw new Exception401("로그인이 필요합니다.");
        }
        if (dto.getFullAddress() == null || dto.getFullAddress().isBlank()) {
            throw new Exception400("주소를 입력해주세요.");
        }

        Member member = findById(sessionUser.getId());

        LocationCoordinate coordinate = locationService
                .geocodeAddressViaNaver(dto.getFullAddress())
                .orElseThrow(() -> new Exception400("주소로 위치를 찾을 수 없습니다."));

        member.updateCurrentLocation(
                coordinate.latitude(),
                coordinate.longitude(),
                dto.getAddress(),
                dto.getAddress1(),
                dto.getAddress2(),
                "MANUAL"
        );

        return MemberResponseDTO.CurrentLocationDTO.of(member);
    }

    @Transactional
    public MemberResponseDTO.CurrentLocationDTO clearCurrentLocation(SessionUser sessionUser) {
        if (sessionUser == null) {
            throw new Exception401("로그인이 필요합니다.");
        }

        Member member = findById(sessionUser.getId());
        member.clearCurrentLocation();
        return MemberResponseDTO.CurrentLocationDTO.of(member);
    }

    public MemberResponseDTO.NearbyMemberListDTO findNearbyMembers(
            SessionUser sessionUser,
            double radiusKm,
            Pageable pageable
    ) {
        if (sessionUser == null) {
            throw new Exception401("로그인이 필요합니다.");
        }
        if (radiusKm < 1 || radiusKm > 1000) {
            throw new Exception400("반경은 1km에서 1000km 사이로 설정해주세요.");
        }

        Member viewer = findById(sessionUser.getId());
        if (!viewer.hasCoordinates()) {
            throw new Exception400("먼저 현재 위치를 설정해주세요.");
        }

        double latDelta = radiusKm / 111.0;
        double lngDelta = radiusKm / (111.0 * Math.cos(Math.toRadians(viewer.getLatitude())));

        int limit = pageable.getPageSize() + 1;
        int offset = (int) pageable.getOffset();

        List<MemberRepository.NearbyMemberProjection> projections = memberRepository.findNearbyMemberIds(
                viewer.getId(),
                viewer.getLatitude(),
                viewer.getLongitude(),
                radiusKm,
                viewer.getLatitude() - latDelta,
                viewer.getLatitude() + latDelta,
                viewer.getLongitude() - lngDelta,
                viewer.getLongitude() + lngDelta,
                limit,
                offset
        );

        boolean hasNext = projections.size() > pageable.getPageSize();
        List<MemberRepository.NearbyMemberProjection> pageProjections = hasNext
                ? projections.subList(0, pageable.getPageSize())
                : projections;

        Map<Long, Member> memberById = memberRepository.findAllById(
                pageProjections.stream().map(MemberRepository.NearbyMemberProjection::getId).toList()
        ).stream().collect(Collectors.toMap(Member::getId, member -> member));

        List<MemberResponseDTO.NearbyMemberDTO> content = pageProjections.stream()
                .filter(projection -> memberById.containsKey(projection.getId()))
                .map(projection -> {
                    Member nearbyMember = memberById.get(projection.getId());
                    boolean canMessage = messagePermissionService.canSendMessage(viewer, nearbyMember);
                    Follow relation = followRepository.findByRequesterIdAndTargetId(viewer.getId(), nearbyMember.getId());
                    boolean isFollowing = relation != null && relation.isActiveFollowing();
                    return MemberResponseDTO.NearbyMemberDTO.of(nearbyMember, projection.getDistanceKm(), canMessage, isFollowing);
                })
                .toList();

        return MemberResponseDTO.NearbyMemberListDTO.of(content, hasNext);
    }

    public List<MemberResponseDTO.SearchDTO> searchMembers(String query) {
        if (query == null || query.isBlank()) {
            return List.of();
        }

        String normalized = query.trim();
        return memberRepository.searchByNameOrNickName(normalized).stream()
                .map(MemberResponseDTO.SearchDTO::of)
                .limit(20)
                .toList();
    }
}
