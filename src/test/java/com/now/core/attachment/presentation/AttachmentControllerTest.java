package com.now.core.attachment.presentation;

import com.now.config.document.utils.RestDocsTestSupport;
import com.now.core.attachment.presentation.dto.AttachmentResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.io.InputStream;

import static com.now.config.fixtures.attachment.AttachmentFixture.createAttachmentResponseForBinaryDownload;
import static com.now.config.fixtures.attachment.AttachmentFixture.createMockMultipartFile;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.responseHeaders;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;

class AttachmentControllerTest extends RestDocsTestSupport {

    @Test
    @DisplayName("바이너리 다운로드 응답")
    void serveDownloadFile() throws Exception {
        Long attachmentIdx = 1L;
        String attachmentName = "NOW_ERD.PNG";
        AttachmentResponse attachment = createAttachmentResponseForBinaryDownload(attachmentName);
        given(attachmentService.getAttachment(attachmentIdx)).willReturn(attachment);

        given(storageService.createStream(attachment.getSavedAttachmentName()))
                .willReturn(createMockMultipartFile(attachmentName).getInputStream());

        ResultActions resultActions =
                mockMvc.perform(RestDocumentationRequestBuilders.get("/api/attachments/{attachmentIdx}", attachmentIdx))
                        .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_OCTET_STREAM))
                        .andExpect(MockMvcResultMatchers.header().string(HttpHeaders.CONTENT_DISPOSITION,
                                "form-data; name=\"attachment\"; filename=\"NOW_ERD.PNG\"")) // 수정된 부분
                        .andExpect(MockMvcResultMatchers.status().isOk());

        resultActions
                .andDo(restDocs.document(
                        pathParameters(
                                parameterWithName("attachmentIdx").description("첨부파일 ID")
                        ),
                        responseHeaders(
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("다운로드 응답의 콘텐츠 타입"),
                                headerWithName(HttpHeaders.CONTENT_DISPOSITION).description("다운로드 응답의 Content-Disposition 헤더")
                        )
                ));
    }
}