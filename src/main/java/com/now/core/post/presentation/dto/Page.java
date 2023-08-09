package com.now.core.post.presentation.dto;

import lombok.Data;

/**
 * 페이지 정보를 담는 데이터 전송 객체
 */
@Data
public class Page {

    /**
     * 블록당 페이지 수
     */
    private final Integer blockPerPage = 5;

    /**
     * 페이지당 레코드 수
     */
    private Integer recordsPerPage;

    /**
     * 페이지 번호
     */
    private Integer pageNo;

    /**
     * 레코드 시작 인덱스
     */
    private Integer recordStartIndex;

    /**
     * 최대 페이지 수
     */
    private Integer maxPage;

    /**
     * 시작 페이지
     */
    private Integer startPage;

    /**
     * 종료 페이지
     */
    private Integer endPage;

    /**
     * 정적 팩토리 메서드 사용으로 인한 기본 생성자를 private 설정
     */
    private Page() {
    }

    /**
     * 전달된 페이지 번호, 페이지당 레코드 수를 기반으로 Page 객체 생성 후 반환
     *
     * @param recordsPerPage 페이지당 레코드 수
     * @param pageNo         페이지 번호
     * @return Page 객체 생성 후 반환
     */
    public static Page of(Integer recordsPerPage, Integer pageNo) {
        Page page = new Page();

        page.setPageNo(pageNo);
        page.setRecordsPerPage(recordsPerPage);
        page.setRecordStartIndex((pageNo - 1) * recordsPerPage);
        return page;
    }

    /**
     * 전체 게시글 수를 기반으로 페이지 정보 업데이트 한 객체 반환
     *
     * @param totalPostCount 전체 게시글 수
     * @return 전체 게시글 수를 기반으로 페이지 정보 업데이트 한 객체 반환
     */
    public Page calculatePaginationInfo(Long totalPostCount) {
        this.maxPage = updateMaxPage(totalPostCount);
        this.pageNo = Math.min(pageNo, maxPage);
        this.startPage = updateStartPage();
        this.endPage = updateEndPage();

        return this;
    }

    /**
     * 종료 페이지 수 반환
     *
     * @return 종료 페이지 수 반환
     */
    private int updateEndPage() {
        int endPage = startPage + this.blockPerPage - 1;
        if (endPage > this.maxPage) {
            endPage = maxPage;
        }
        return endPage;
    }

    /**
     * 시작 페이지 수 반환
     *
     * @return 시작 페이지 수 반환
     */
    private int updateStartPage() {
        return (((int) (Math.ceil((double) pageNo / this.blockPerPage))) - 1) * this.blockPerPage + 1;
    }

    /**
     * 최대 페이지 수 반환
     *
     * @param totalBoardCount 전체 게시글 수
     * @return 최대 페이지 수 반환
     */
    private int updateMaxPage(double totalBoardCount) {
        return (int) (Math.ceil(totalBoardCount / this.recordsPerPage));
    }
}