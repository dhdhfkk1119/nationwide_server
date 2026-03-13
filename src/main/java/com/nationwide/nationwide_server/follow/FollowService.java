package com.nationwide.nationwide_server.follow;

import com.nationwide.nationwide_server._core.errors.exception.Exception400;
import com.nationwide.nationwide_server._core.util.SessionUser;
import com.nationwide.nationwide_server.alarm.AlarmService;
import com.nationwide.nationwide_server.follow.dto.FollowResponseDTO;
import com.nationwide.nationwide_server.member.Member;
import com.nationwide.nationwide_server.member.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FollowService {
    private final FollowRepository followRepository;
    private final MemberService memberService;
    private final AlarmService alarmService;

    @Transactional
    public FollowResponseDTO.StatusDTO toggleFollow(SessionUser sessionUser, Long targetMemberId) {
        if (sessionUser == null) {
            throw new Exception400("로그인이 필요합니다.");
        }
        if (sessionUser.getId().equals(targetMemberId)) {
            throw new Exception400("자기 자신은 팔로우할 수 없습니다.");
        }

        Member follower = memberService.findById(sessionUser.getId());
        Member following = memberService.findById(targetMemberId);
        Follow follow = followRepository.findByFollowerIdAndFollowingId(follower.getId(), following.getId());

        if (follow == null) {
            followRepository.save(
                    Follow.builder()
                            .follower(follower)
                            .following(following)
                            .build()
            );
            alarmService.createFollowAlarm(follower, following);
        } else {
            followRepository.delete(follow);
        }

        return getStatus(sessionUser.getId(), targetMemberId);
    }

    public FollowResponseDTO.StatusDTO getStatus(Long viewerId, Long targetMemberId) {
        boolean isFollowing = viewerId != null
                && !viewerId.equals(targetMemberId)
                && followRepository.existsByFollowerIdAndFollowingId(viewerId, targetMemberId);
        boolean isFollowedBy = viewerId != null
                && !viewerId.equals(targetMemberId)
                && followRepository.existsByFollowerIdAndFollowingId(targetMemberId, viewerId);

        Long followerCnt = followRepository.countFollowersByMemberId(targetMemberId);
        Long followingCnt = followRepository.countFollowingByMemberId(targetMemberId);

        return FollowResponseDTO.StatusDTO.of(isFollowing, isFollowedBy, followerCnt, followingCnt);
    }

    public Slice<FollowResponseDTO.MemberListDTO> followerSlice(
            SessionUser sessionUser,
            Long memberId,
            Pageable pageable
    ) {
        Long viewerId = sessionUser != null ? sessionUser.getId() : null;
        return followRepository.findFollowerSlice(memberId, pageable)
                .map(follow -> FollowResponseDTO.MemberListDTO.of(follow.getFollower(), viewerId, this));
    }

    public Slice<FollowResponseDTO.MemberListDTO> followingSlice(
            SessionUser sessionUser,
            Long memberId,
            Pageable pageable
    ) {
        Long viewerId = sessionUser != null ? sessionUser.getId() : null;
        return followRepository.findFollowingSlice(memberId, pageable)
                .map(follow -> FollowResponseDTO.MemberListDTO.of(follow.getFollowing(), viewerId, this));
    }

    public Long countFollowers(Long memberId) {
        return followRepository.countFollowersByMemberId(memberId);
    }

    public Long countFollowing(Long memberId) {
        return followRepository.countFollowingByMemberId(memberId);
    }
}
