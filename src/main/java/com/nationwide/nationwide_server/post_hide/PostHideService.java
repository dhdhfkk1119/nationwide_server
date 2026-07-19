package com.nationwide.nationwide_server.post_hide;

import com.nationwide.nationwide_server._core.errors.exception.Exception400;
import com.nationwide.nationwide_server._core.errors.exception.Exception401;
import com.nationwide.nationwide_server._core.util.SessionUser;
import com.nationwide.nationwide_server.member.Member;
import com.nationwide.nationwide_server.member.MemberService;
import com.nationwide.nationwide_server.post_hide.dto.PostHideResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostHideService {
    private final PostHideRepository postHideRepository;
    private final MemberService memberService;

    @Transactional
    public boolean toggleHide(SessionUser sessionUser, Long targetMemberId) {
        if (sessionUser == null) {
            throw new Exception401("로그인이 필요합니다.");
        }
        if (sessionUser.getId().equals(targetMemberId)) {
            throw new Exception400("자기 자신에게는 게시물을 숨길 수 없습니다.");
        }
        memberService.validateActiveMember(sessionUser.getId());

        Member owner = memberService.findById(sessionUser.getId());
        Member viewer = memberService.findById(targetMemberId);

        PostHide existing = postHideRepository.findByOwnerIdAndViewerId(owner.getId(), viewer.getId());
        if (existing != null) {
            postHideRepository.delete(existing);
            return false;
        }

        postHideRepository.save(
                PostHide.builder()
                        .owner(owner)
                        .viewer(viewer)
                        .build()
        );
        return true;
    }

    public boolean isHiddenFromViewer(Long ownerId, Long viewerId) {
        if (ownerId == null || viewerId == null) {
            return false;
        }
        return postHideRepository.existsByOwnerIdAndViewerId(ownerId, viewerId);
    }

    public Slice<PostHideResponseDTO.ItemDTO> hiddenSlice(SessionUser sessionUser, Pageable pageable) {
        if (sessionUser == null) {
            throw new Exception401("로그인이 필요합니다.");
        }

        return postHideRepository.findSliceByOwnerId(sessionUser.getId(), pageable)
                .map(PostHideResponseDTO.ItemDTO::of);
    }
}
