package com.now.domain.post;

import com.now.core.post.domain.Notice;

import java.time.LocalDateTime;

public class NoticeTest {
    public static Notice createNotice(String managerId) {
        return Notice.builder()
                .noticeIdx(1L)
                .title("제목")
                .managerId(managerId)
                .regDate(LocalDateTime.now())
                .modDate(LocalDateTime.now())
                .content("내용")
                .viewCount(0)
                .likeCount(0)
                .dislikeCount(0)
                .isPinned(true)
                .build();
    }
}
