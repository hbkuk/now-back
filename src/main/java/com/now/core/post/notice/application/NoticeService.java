package com.now.core.post.notice.application;

import com.now.common.exception.ErrorType;
import com.now.core.post.notice.domain.Notice;
import com.now.core.post.notice.domain.repository.NoticeRepository;
import com.now.core.post.common.exception.InvalidPostException;
import com.now.core.post.common.presentation.dto.Condition;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 공지 게시글 관련 비즈니스 로직을 처리하는 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NoticeService {

    private final NoticeRepository noticeRepository;

    /**
     * 상단에 고정된 공지 게시물과 조건에 맞는 게시물 정보를 조회 후 반환
     *
     * @param condition 게시물 제한 정보를 담은 객체
     * @return 공지사항 게시글 정보 리스트
     */
    public List<Notice> getAllNoticesWithPin(Condition condition) {
        return noticeRepository.findAllNoticesWithPin(condition);
    }

    /**
     * 공지 게시글 응답
     *
     * @param postIdx 게시글 번호
     * @return 공지 게시글 정보
     */
    public Notice getNotice(Long postIdx) {
        Notice notice = noticeRepository.findNotice(postIdx);
        if (notice == null) {
            throw new InvalidPostException(ErrorType.NOT_FOUND_POST);
        }

        return notice;
    }
}

