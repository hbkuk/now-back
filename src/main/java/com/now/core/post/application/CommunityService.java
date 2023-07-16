package com.now.core.post.application;

import com.now.core.authentication.constants.Authority;
import com.now.core.category.domain.constants.PostGroup;
import com.now.core.comment.application.CommentService;
import com.now.core.member.application.MemberService;
import com.now.core.member.domain.Member;
import com.now.core.post.domain.Community;
import com.now.core.post.domain.PostRepository;
import com.now.core.post.exception.CannotWritePostException;
import com.now.core.post.exception.PermissionDeniedException;
import com.now.core.post.presentation.dto.Condition;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

/**
 * 커뮤니티 게시글 관련 비즈니스 로직을 처리하는 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CommunityService {

    private final PostRepository postRepository;
    private final MemberService memberService;
    private final CommentService commentService;
    private final MessageSourceAccessor messageSource;


    /**
     * 모든 커뮤니티 게시글 정보를 조회 후 반환
     *
     * @return 커뮤니티 게시글 정보 리스트
     */
    public List<Community> retrieveAllCommunities(Condition condition) {
        return postRepository.findAllCommunity(condition);
    }

    /**
     * 커뮤니티 게시글 응답
     *
     * @param postIdx 게시글 번호
     * @return 공지 게시글 정보
     */
    public Community findByPostIdx(Long postIdx) {
        Community community = postRepository.findCommunity(postIdx);
        if (community == null) {
            throw new NoSuchElementException(messageSource.getMessage("error.noSuch.post"));
        }

        return community;
    }

    /**
     * 커뮤니티 게시글 등록
     *
     * @param community 등록할 커뮤니티 게시글 정보
     * @throws PermissionDeniedException 권한이 없을 경우 발생하는 예외
     * @throws CannotWritePostException  게시글 작성이 불가능한 경우 발생하는 예외
     */
    public void registerCommunity(Community community) {
        Member member = memberService.findMemberById(community.getMemberId());

        if (!PostGroup.isCategoryInGroup(PostGroup.COMMUNITY, community.getCategory())) {
            throw new CannotWritePostException(messageSource.getMessage("error.write.failed"));
        }

        postRepository.saveCommunity(community.updateMemberIdx(member.getMemberIdx()));
    }

    /**
     * 커뮤니티 게시글 수정
     *
     * @param community 수정할 커뮤니티 게시글 정보
     */
    public void updateCommunity(Community community) {
        Member member = memberService.findMemberById(community.getMemberId());

        if (!PostGroup.isCategoryInGroup(PostGroup.COMMUNITY, community.getCategory())) {
            throw new CannotWritePostException(messageSource.getMessage("error.write.failed"));
        }

        postRepository.updateCommunity(community.updateMemberIdx(member.getMemberIdx()));
    }

    public void hasUpdateAccess(Long postIdx, String memberId, Authority authority) {
        if (Authority.isManager(authority)) {
            return;
        }

        Community community = postRepository.findCommunity(postIdx);
        community.canUpdate(memberService.findMemberById(memberId));
    }

    /**
     * 게시글 삭제 권한 확인
     *
     * @param postIdx   게시글 번호
     * @param memberId  회원 아이디
     * @param authority 권한
     */
    public void hasDeleteAccess(Long postIdx, String memberId, Authority authority) {
        if (Authority.isManager(authority)) {
            return;
        }

        Community community = postRepository.findCommunity(postIdx);
        community.canDelete(memberService.findMemberById(memberId), commentService.findAllByPostIdx(postIdx));
    }

    /**
     * 게시글 번호에 해당하는 게시글 삭제
     * 
     * @param postIdx 게시글 번호
     */
    public void deleteCommunity(Long postIdx) {
        postRepository.deleteCommunity(postIdx);
    }
}

