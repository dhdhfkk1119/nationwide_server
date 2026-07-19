package com.nationwide.nationwide_server.follow;

import com.nationwide.nationwide_server._core.errors.exception.Exception400;
import com.nationwide.nationwide_server._core.errors.exception.Exception404;
import com.nationwide.nationwide_server._core.util.SessionUser;
import com.nationwide.nationwide_server.alarm.AlarmService;
import com.nationwide.nationwide_server.follow.dto.FollowResponseDTO;
import com.nationwide.nationwide_server.member.Member;
import com.nationwide.nationwide_server.member.MemberService;
import com.nationwide.nationwide_server.member_block.MemberBlockService;
import com.nationwide.nationwide_server.post_hide.PostHideService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FollowService {
    private final FollowRepository followRepository;
    private final MemberService memberService;
    private final AlarmService alarmService;
    private final MemberBlockService memberBlockService;
    private final PostHideService postHideService;

    @Transactional
    public FollowResponseDTO.StatusDTO toggleFollow(SessionUser sessionUser, Long targetMemberId) {
        if (sessionUser == null) {
            throw new Exception400("濡쒓렇?몄씠 ?꾩슂?⑸땲??");
        }
        memberService.validateActiveMember(sessionUser.getId());
        if (sessionUser.getId().equals(targetMemberId)) {
            throw new Exception400("?먭린 ?먯떊? ?붾줈?고븷 ???놁뒿?덈떎.");
        }

        Member requester = memberService.findById(sessionUser.getId());
        Member target = memberService.findById(targetMemberId);
        Follow relation = followRepository.findByRequesterIdAndTargetId(requester.getId(), target.getId());

        if (!target.isPrivateProfile()) {
            handlePublicFollowToggle(requester, target, relation);
            return getStatus(sessionUser.getId(), targetMemberId);
        }

        handlePrivateFollowToggle(requester, target, relation);
        return getStatus(sessionUser.getId(), targetMemberId);
    }

    @Transactional
    public FollowResponseDTO.StatusDTO respondToRequest(
            SessionUser sessionUser,
            Long requesterMemberId,
            String action
    ) {
        if (sessionUser == null) {
            throw new Exception400("濡쒓렇?몄씠 ?꾩슂?⑸땲??");
        }
        memberService.validateActiveMember(sessionUser.getId());

        Follow request = followRepository.findByRequesterIdAndTargetId(requesterMemberId, sessionUser.getId());
        if (request == null || request.getRelationStatus() != FollowRelationStatus.REQUESTED) {
            throw new Exception404("泥섎━???붾줈???붿껌??李얠쓣 ???놁뒿?덈떎.");
        }

        Timestamp now = new Timestamp(System.currentTimeMillis());

        switch (action) {
            case "VISIBLE_ONLY" -> {
                request.setRelationStatus(FollowRelationStatus.VISIBLE_ONLY);
                request.setProfileVisibleAt(now);
                request.setApprovedAt(null);
                request.setRejectedAt(null);
                request.setCanceledAt(null);
            }
            case "FOLLOWING" -> {
                request.setRelationStatus(FollowRelationStatus.FOLLOWING);
                request.setApprovedAt(now);
                request.setProfileVisibleAt(now);
                request.setRejectedAt(null);
                request.setCanceledAt(null);
            }
            case "REJECTED" -> {
                request.setRelationStatus(FollowRelationStatus.REJECTED);
                request.setRejectedAt(now);
                request.setApprovedAt(null);
                request.setProfileVisibleAt(null);
                alarmService.createFollowRequestRejectedAlarm(request.getTarget(), request.getRequester());
            }
            default -> throw new Exception400("吏?먰븯吏 ?딅뒗 ?붿껌 泥섎━ 諛⑹떇?낅땲??");
        }

        return getStatus(sessionUser.getId(), requesterMemberId);
    }

    public FollowResponseDTO.StatusDTO getStatus(Long viewerId, Long targetMemberId) {
        Member target = memberService.findById(targetMemberId);
        Follow viewerRelation = viewerId != null && !viewerId.equals(targetMemberId)
                ? followRepository.findByRequesterIdAndTargetId(viewerId, targetMemberId)
                : null;

        boolean isFollowing = viewerRelation != null && viewerRelation.isActiveFollowing();
        boolean hasPendingRequest = viewerRelation != null && viewerRelation.isPendingRequest();
        FollowRelationStatus relationStatus = viewerRelation != null
                ? viewerRelation.getRelationStatus()
                : null;

        Follow reverseRelation = viewerId != null && !viewerId.equals(targetMemberId)
                ? followRepository.findByRequesterIdAndTargetId(targetMemberId, viewerId)
                : null;
        boolean isFollowedBy = reverseRelation != null && reverseRelation.isActiveFollowing();

        boolean canViewProfile = canViewProfile(viewerId, target, viewerRelation);

        Long followerCnt = followRepository.countFollowersByMemberId(targetMemberId);
        Long followingCnt = followRepository.countFollowingByMemberId(targetMemberId);

        boolean isBlocking = memberBlockService.isBlocking(viewerId, targetMemberId);
        boolean isBlockedByOther = memberBlockService.isBlocking(targetMemberId, viewerId);
        boolean isHidingFromOther = postHideService.isHiddenFromViewer(viewerId, targetMemberId);

        return FollowResponseDTO.StatusDTO.of(
                isFollowing,
                isFollowedBy,
                hasPendingRequest,
                canViewProfile,
                relationStatus,
                followerCnt,
                followingCnt,
                isBlocking,
                isBlockedByOther,
                isHidingFromOther
        );
    }

    public boolean canViewProfile(Long viewerId, Member target, Follow relation) {
        if (memberService.isDeactivated(target)) {
            return viewerId != null && viewerId.equals(target.getId());
        }
        if (viewerId != null && viewerId.equals(target.getId())) {
            return true;
        }
        if (!target.isPrivateProfile()) {
            return true;
        }
        return relation != null && relation.canViewPrivateProfile();
    }

    public boolean canViewProfile(Long viewerId, Member target) {
        Follow relation = viewerId != null && !viewerId.equals(target.getId())
                ? followRepository.findByRequesterIdAndTargetId(viewerId, target.getId())
                : null;
        return canViewProfile(viewerId, target, relation);
    }

    public Slice<FollowResponseDTO.MemberListDTO> followerSlice(
            SessionUser sessionUser,
            Long memberId,
            Pageable pageable
    ) {
        Long viewerId = sessionUser != null ? sessionUser.getId() : null;
        return followRepository.findFollowerSlice(memberId, pageable)
                .map(follow -> FollowResponseDTO.MemberListDTO.of(
                        follow.getRequester(),
                        viewerId,
                        this,
                        follow.getRelationStatus() == FollowRelationStatus.REQUESTED
                ));
    }

    public Slice<FollowResponseDTO.MemberListDTO> followingSlice(
            SessionUser sessionUser,
            Long memberId,
            Pageable pageable
    ) {
        Long viewerId = sessionUser != null ? sessionUser.getId() : null;
        return followRepository.findFollowingSlice(memberId, pageable)
                .map(follow -> FollowResponseDTO.MemberListDTO.of(
                        follow.getTarget(),
                        viewerId,
                        this,
                        follow.getRelationStatus() == FollowRelationStatus.REQUESTED
                ));
    }

    public Slice<FollowResponseDTO.MemberListDTO> incomingRequestSlice(
            SessionUser sessionUser,
            Pageable pageable
    ) {
        if (sessionUser == null) {
            throw new Exception400("濡쒓렇?몄씠 ?꾩슂?⑸땲??");
        }
        return followRepository.findIncomingRequestSlice(sessionUser.getId(), pageable)
                .map(follow -> FollowResponseDTO.MemberListDTO.of(
                        follow.getRequester(),
                        sessionUser.getId(),
                        this,
                        true
                ));
    }

    public Long countFollowers(Long memberId) {
        return followRepository.countFollowersByMemberId(memberId);
    }

    public Long countFollowing(Long memberId) {
        return followRepository.countFollowingByMemberId(memberId);
    }

    private void handlePublicFollowToggle(Member requester, Member target, Follow relation) {
        Timestamp now = new Timestamp(System.currentTimeMillis());
        if (relation == null) {
            followRepository.save(
                    Follow.builder()
                            .requester(requester)
                            .target(target)
                            .relationStatus(FollowRelationStatus.FOLLOWING)
                            .approvedAt(now)
                            .profileVisibleAt(now)
                            .build()
            );
            promoteReversePendingRequestIfNeeded(requester, target, now);
            alarmService.createFollowAlarm(requester, target);
            return;
        }

        if (relation.isActiveFollowing()) {
            followRepository.delete(relation);
            return;
        }

        relation.setRelationStatus(FollowRelationStatus.FOLLOWING);
        relation.setApprovedAt(now);
        relation.setProfileVisibleAt(now);
        relation.setRejectedAt(null);
        relation.setCanceledAt(null);
        promoteReversePendingRequestIfNeeded(requester, target, now);
        alarmService.createFollowAlarm(requester, target);
    }

    private void handlePrivateFollowToggle(Member requester, Member target, Follow relation) {
        Timestamp now = new Timestamp(System.currentTimeMillis());

        if (relation == null) {
            followRepository.save(
                    Follow.builder()
                            .requester(requester)
                            .target(target)
                            .relationStatus(FollowRelationStatus.REQUESTED)
                            .build()
            );
            alarmService.createFollowAlarm(requester, target);
            return;
        }

        if (relation.isPendingRequest() || relation.canViewPrivateProfile()) {
            followRepository.delete(relation);
            return;
        }

        relation.setRelationStatus(FollowRelationStatus.REQUESTED);
        relation.setCanceledAt(null);
        relation.setRejectedAt(null);
        relation.setApprovedAt(null);
        relation.setProfileVisibleAt(null);
        alarmService.createFollowAlarm(requester, target);
    }

    private void promoteReversePendingRequestIfNeeded(
            Member requester,
            Member target,
            Timestamp now
    ) {
        Follow reverseRelation =
                followRepository.findByRequesterIdAndTargetId(target.getId(), requester.getId());

        if (reverseRelation == null || reverseRelation.getRelationStatus() != FollowRelationStatus.REQUESTED) {
            return;
        }

        reverseRelation.setRelationStatus(FollowRelationStatus.FOLLOWING);
        reverseRelation.setApprovedAt(now);
        reverseRelation.setProfileVisibleAt(now);
        reverseRelation.setRejectedAt(null);
        reverseRelation.setCanceledAt(null);
    }
}
