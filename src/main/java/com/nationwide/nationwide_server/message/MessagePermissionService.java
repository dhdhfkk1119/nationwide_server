package com.nationwide.nationwide_server.message;

import com.nationwide.nationwide_server.follow.Follow;
import com.nationwide.nationwide_server.follow.FollowRepository;
import com.nationwide.nationwide_server.member.Member;
import com.nationwide.nationwide_server.member.m_enum.MessagePermission;
import com.nationwide.nationwide_server.member_block.MemberBlockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

// canSendMessage()는 MessageService와 MemberService(동네 친구 목록의 canMessage 계산) 양쪽에서 쓰인다.
// MemberBlockService/MemberService를 거치면 순환 의존(MemberService -> MessagePermissionService ->
// MemberBlockService -> MemberService)이 생기므로, 리포지토리를 직접 참조해 순환을 피한다.
@Service
@RequiredArgsConstructor
public class MessagePermissionService {
    private final MemberBlockRepository memberBlockRepository;
    private final FollowRepository followRepository;

    public boolean canSendMessage(Member sender, Member recipient) {
        if (sender.getId().equals(recipient.getId())) {
            return false;
        }
        if (memberBlockRepository.existsEitherDirection(sender.getId(), recipient.getId())) {
            return false;
        }

        // 필드 도입 이전에 생성된 회원은 DB 값이 null일 수 있으므로 기본값(FOLLOWERS_ONLY)으로 취급한다.
        MessagePermission permission = recipient.getMessagePermission() != null
                ? recipient.getMessagePermission()
                : MessagePermission.FOLLOWERS_ONLY;

        return switch (permission) {
            case ALL -> true;
            case BLOCK_ALL -> false;
            case FOLLOWERS_ONLY -> {
                Follow relation = followRepository.findByRequesterIdAndTargetId(sender.getId(), recipient.getId());
                yield relation != null && relation.isActiveFollowing();
            }
            case FOLLOWING_ONLY -> {
                Follow relation = followRepository.findByRequesterIdAndTargetId(recipient.getId(), sender.getId());
                yield relation != null && relation.isActiveFollowing();
            }
        };
    }
}
