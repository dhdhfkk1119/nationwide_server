package com.nationwide.nationwide_server.member_block;

import com.nationwide.nationwide_server._core.errors.exception.Exception400;
import com.nationwide.nationwide_server._core.errors.exception.Exception401;
import com.nationwide.nationwide_server._core.util.SessionUser;
import com.nationwide.nationwide_server.member.Member;
import com.nationwide.nationwide_server.member.MemberService;
import com.nationwide.nationwide_server.member_block.dto.MemberBlockResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberBlockService {
    private final MemberBlockRepository memberBlockRepository;
    private final MemberService memberService;

    @Transactional
    public boolean toggleBlock(SessionUser sessionUser, Long targetMemberId) {
        if (sessionUser == null) {
            throw new Exception401("로그인이 필요합니다.");
        }
        if (sessionUser.getId().equals(targetMemberId)) {
            throw new Exception400("자기 자신은 차단할 수 없습니다.");
        }
        memberService.validateActiveMember(sessionUser.getId());

        Member blocker = memberService.findById(sessionUser.getId());
        Member blocked = memberService.findById(targetMemberId);

        MemberBlock existing = memberBlockRepository.findByBlockerIdAndBlockedId(blocker.getId(), blocked.getId());
        if (existing != null) {
            memberBlockRepository.delete(existing);
            return false;
        }

        memberBlockRepository.save(
                MemberBlock.builder()
                        .blocker(blocker)
                        .blocked(blocked)
                        .build()
        );
        return true;
    }

    public boolean isBlocking(Long blockerId, Long blockedId) {
        if (blockerId == null || blockedId == null) {
            return false;
        }
        return memberBlockRepository.existsByBlockerIdAndBlockedId(blockerId, blockedId);
    }

    public boolean isBlockedEitherWay(Long memberIdA, Long memberIdB) {
        if (memberIdA == null || memberIdB == null) {
            return false;
        }
        return memberBlockRepository.existsEitherDirection(memberIdA, memberIdB);
    }

    public Slice<MemberBlockResponseDTO.ItemDTO> blockedSlice(SessionUser sessionUser, Pageable pageable) {
        if (sessionUser == null) {
            throw new Exception401("로그인이 필요합니다.");
        }

        return memberBlockRepository.findSliceByBlockerId(sessionUser.getId(), pageable)
                .map(MemberBlockResponseDTO.ItemDTO::of);
    }
}
