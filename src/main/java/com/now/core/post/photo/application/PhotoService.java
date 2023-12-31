package com.now.core.post.photo.application;

import com.now.common.exception.ErrorType;
import com.now.core.category.domain.constants.PostGroup;
import com.now.core.category.exception.InvalidCategoryException;
import com.now.core.comment.domain.CommentRepository;
import com.now.core.member.domain.Member;
import com.now.core.member.domain.MemberRepository;
import com.now.core.member.exception.InvalidMemberException;
import com.now.core.post.photo.domain.Photo;
import com.now.core.post.photo.domain.repository.PhotoRepository;
import com.now.core.post.common.exception.InvalidPostException;
import com.now.core.post.common.presentation.dto.Condition;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 사진 게시글 관련 비즈니스 로직을 처리하는 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PhotoService {

    private final PhotoRepository photoRepository;
    private final MemberRepository memberRepository;
    private final CommentRepository commentRepository;

    /**
     * 모든 사진 게시글 정보를 조회 후 반환
     *
     * @return 사진 게시글 정보 리스트
     */
    public List<Photo> getAllPhotos(Condition condition) {
        return photoRepository.findAllPhotos(condition);
    }

    /**
     * 사진 게시글 등록
     *
     * @param photo 등록할 사진 게시글 정보
     */
    public void registerPhoto(Photo photo) {
        Member member = getMember(photo.getMemberId());

        if (!PostGroup.isCategoryInGroup(PostGroup.PHOTO, photo.getCategory())) {
            throw new InvalidCategoryException(ErrorType.INVALID_CATEGORY);
        }

        photoRepository.savePhoto(photo.updateMemberIdx(member.getMemberIdx()));
    }

    /**
     * 사진 게시글 수정
     *
     * @param photo 수정할 사진 게시글 정보
     */
    @CacheEvict(value = {"postCache", "photoCache"}, allEntries = true)
    public void updatePhoto(Photo photo) {
        Member member = getMember(photo.getMemberId());

        if (!PostGroup.isCategoryInGroup(PostGroup.PHOTO, photo.getCategory())) {
            throw new InvalidCategoryException(ErrorType.INVALID_CATEGORY);
        }

        photoRepository.updatePhoto(photo.updateMemberIdx(member.getMemberIdx()));
    }
    /**
     * 사진 게시글 삭제
     *
     * @param postIdx 게시글 번호
     */
    @CacheEvict(value = {"postCache", "photoCache"}, allEntries = true)
    public void deletePhoto(Long postIdx) {
        photoRepository.deletePhoto(postIdx);
    }

    /**
     * 게시글 수정 권한 확인
     *
     * @param postIdx  게시글 번호
     * @param memberId 회원 아이디
     */
    public void hasUpdateAccess(Long postIdx, String memberId) {
        Photo photo = photoRepository.findPhoto(postIdx);
        photo.canUpdate(getMember(memberId));
    }

    /**
     * 게시글 삭제 권한 확인
     *
     * @param postIdx  게시글 번호
     * @param memberId 회원 아이디
     */
    public void hasDeleteAccess(Long postIdx, String memberId) {
        Photo photo = photoRepository.findPhoto(postIdx);
        photo.canDelete(getMember(memberId), commentRepository.findAllByPostIdx(postIdx));
    }

    /**
     * 회원 정보 응답
     *
     * @param memberId 회원 아이디
     * @return 회원 도메인 객체
     */
    private Member getMember(String memberId) {
        Member member = memberRepository.findById(memberId);
        if(member == null) {
            throw new InvalidMemberException(ErrorType.NOT_FOUND_MEMBER);
        }
        return member;
    }

    /**
     * 사진 게시글 응답
     *
     * @param postIdx 게시글 번호
     * @return 사진 게시글 정보
     */
    @Cacheable(value = "postCache", key="#postIdx")
    public Photo getPhoto(Long postIdx) {
        Photo photo = photoRepository.findPhoto(postIdx);
        if (photo == null) {
            throw new InvalidPostException(ErrorType.NOT_FOUND_POST);
        }

        return photo;
    }

    /**
     * 사진 수정 게시글 응답
     *
     * @param postIdx 게시글 번호
     * @param memberId 회원 ID
     * @return 사진 수정 게시글 정보
     */
    public Photo getEditPhoto(Long postIdx, String memberId) {
        Photo photo = getPhoto(postIdx);
        Member member = getMember(memberId);

        photo.canUpdate(member);
        return photo;
    }
}

