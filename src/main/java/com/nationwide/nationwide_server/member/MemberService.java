package com.nationwide.nationwide_server.member;

import com.nationwide.nationwide_server._core.errors.exception.Exception400;
import com.nationwide.nationwide_server._core.errors.exception.Exception401;
import com.nationwide.nationwide_server._core.errors.exception.Exception404;
import com.nationwide.nationwide_server._core.jwt.JwtTokenProvider;
import com.nationwide.nationwide_server._core.util.SessionUser;
import com.nationwide.nationwide_server.board.BoardRepository;
import com.nationwide.nationwide_server.email.EmailRepository;
import com.nationwide.nationwide_server.email.EmailService;
import com.nationwide.nationwide_server.follow.FollowRepository;
import com.nationwide.nationwide_server.image_file.ImageFile;
import com.nationwide.nationwide_server.image_file.ImageFileService;
import com.nationwide.nationwide_server.image_file.dto.ImageResponseDTO;
import com.nationwide.nationwide_server.location.LocationCoordinate;
import com.nationwide.nationwide_server.location.LocationService;
import com.nationwide.nationwide_server.member.dto.MemberRequestDTO;
import com.nationwide.nationwide_server.member.dto.MemberResponseDTO;
import com.nationwide.nationwide_server.member_terms.MemberTerms;
import com.nationwide.nationwide_server.member_terms.MemberTermsRepository;
import com.nationwide.nationwide_server.terms.Terms;
import com.nationwide.nationwide_server.terms.TermsRepository;
import com.nationwide.nationwide_server.terms.TermsService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
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

    @Transactional
    public void save(MemberRequestDTO.SaveDTO saveDTO) {
        Member member = saveDTO.toEntity();

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

<<<<<<< HEAD
    // 회원 로그인
    @Transactional
    public MemberResponseDTO.LoginDTO loginMember(MemberRequestDTO.LoginDTO dto){
=======
    public MemberResponseDTO.LoginDTO loginMember(MemberRequestDTO.LoginDTO dto) {
>>>>>>> e58ce993e4f8603c87817a4a0dca0713fc5defa5
        Member member = findByLoginId(dto.getLoginId());

        if (!bCryptPasswordEncoder.matches(dto.getPassword(), member.getPassword())) {
            throw new Exception401(MEMBER_PASS_NOT_MISMATCH.getMessage());
        }

        syncMemberLocation(member, false);

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

<<<<<<< HEAD
        boolean isAddressChanging = isAddressChanging(member, updateDTO);
        if (isAddressChanging && member.getAddressChangeCount() >= 3) {
            throw new Exception400("도로명 주소는 3회까지만 변경할 수 있습니다.");
        }

        // 기존 이미지 중에 다른 값 있으면 삭제
        if(updateDTO.getImageFileId() != null){
            imageFileService.syncDeleteImages(member,updateDTO.getImageFileId());
=======
        if (updateDTO.getImageFileId() != null) {
            imageFileService.syncDeleteImages(member, updateDTO.getImageFileId());
>>>>>>> e58ce993e4f8603c87817a4a0dca0713fc5defa5
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
        member.updatePrivacySettings(dto.isPrivateProfile(), dto.isLocationVisible());
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

<<<<<<< HEAD
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

=======
    public List<MemberResponseDTO.SearchDTO> searchMembers(String query) {
        String normalized = query == null ? "" : query.trim();
        if (normalized.isBlank()) {
            return List.of();
        }
>>>>>>> e58ce993e4f8603c87817a4a0dca0713fc5defa5

        return memberRepository.searchByNameOrNickName(normalized).stream()
                .map(MemberResponseDTO.SearchDTO::of)
                .limit(20)
                .toList();
    }
}
