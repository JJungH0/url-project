package com.url.jjung.domain.url.controller;

import com.url.jjung.domain.auth.repository.UserRepository;
import com.url.jjung.domain.url.dto.CreateUrlReq;
import com.url.jjung.domain.url.dto.CreateUrlResp;
import com.url.jjung.domain.url.repository.ShortUrlRepository;
import com.url.jjung.domain.url.service.UrlService;
import com.url.jjung.global.exception.CustomException;
import com.url.jjung.global.exception.ErrorCode;
import com.url.jjung.global.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class UrlControllerTest {
    @InjectMocks
    private UrlController urlController;

    @Mock
    private UrlService urlService;

    private MockMvc mockMvc;
    private ObjectMapper obj;


    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(urlController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        obj = new ObjectMapper();

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("test@gmail.com", null, Collections.emptyList()));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private CreateUrlResp createUrlResp() {
        return new CreateUrlResp(
                "https://www.example.com/very/long/url",
                "http://localhost:8080/r/aB3kZ1",
                "aB3kZ1",
                LocalDateTime.now().plusDays(30)
        );
    }

    @Test
    @DisplayName("단축 URL 생성 성공")
    void createShortUrl_success() throws Exception {
        // given
        CreateUrlReq req = new CreateUrlReq("https://www.example.com/very/long/url", 30);
        CreateUrlResp resp = createUrlResp();

        given(urlService.createShortUrl(any(), any())).willReturn(resp);

        // when & then
        mockMvc.perform(post("/api/urls")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(obj.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.originalUrl").value("https://www.example.com/very/long/url"))
                .andExpect(jsonPath("$.data.shortCode").value("aB3kZ1"))
                .andExpect(jsonPath("$.data.shortUrl").value("http://localhost:8080/r/aB3kZ1"));
    }

    @Test
    @DisplayName("단축 URL 생성 실패 - URL 형식 오류")
    void createShortUrl_fail_invalidUrl() throws Exception {
        // given
        CreateUrlReq req = new CreateUrlReq("test-url", 30);

        // when & then
        mockMvc.perform(post("/api/urls")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(obj.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("URL은 http:// 또는 https://로 시작해야 합니다."));
    }

    @Test
    @DisplayName("단축 URL 생성 실패 - URL 중복")
    void createShortUrl_fail_duplicate() throws Exception {
        // given
        CreateUrlReq req = new CreateUrlReq("https://www.example.com/very/long/url", 30);

        willThrow(new CustomException(ErrorCode.URL_DUPLICATE))
                .given(urlService).createShortUrl(any(), any());

        // when & then
        mockMvc.perform(post("/api/urls")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(obj.writeValueAsString(req)))
                .andDo(print())
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.URL_DUPLICATE.getMessage()));
    }

    @Test
    @DisplayName("내 단축 URL 목록 조회 성공")
    void getMyUrls_success() throws Exception {
        // given
        List<CreateUrlResp> resp = List.of(createUrlResp());
        given(urlService.getMyUrls(any())).willReturn(resp);

        // when & then
        mockMvc.perform(get("/api/urls"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].shortCode").value("aB3kZ1"));
    }

    @Test
    @DisplayName("내 단축 URL 목록 조회 성공 - 목록이 비어있음")
    void getMyUrls_success_empty() throws Exception {
        // given
        given(urlService.getMyUrls(any())).willReturn(Collections.emptyList());

        // when & then
        mockMvc.perform(get("/api/urls"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(0));

    }

    @Test
    @DisplayName("단축 URL 삭제 성공")
    void deleteShortUrl_success() throws Exception {
        // given
        willDoNothing().given(urlService).deleteShortUrl(any(),anyString());

        // when & then
        mockMvc.perform(delete("/api/urls/aB3kZ1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("단축 URL 삭제 실패 - 존재하지 않는 URL")
    void deleteShortUrl_fail_notFound() throws Exception {
        // given
        willThrow(new CustomException(ErrorCode.URL_NOT_FOUND))
                .given(urlService).deleteShortUrl(any(), anyString());

        // when & then
        mockMvc.perform(delete("/api/urls/aB3kZ1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.URL_NOT_FOUND.getMessage()));
    }

    @Test
    @DisplayName("단축 URL 삭제 실패 - 본인 URL이 아님")
    void deleteShortUrl_fail_forbidden() throws Exception {

        willThrow(new CustomException(ErrorCode.URL_FORBIDDEN))
                .given(urlService).deleteShortUrl(any(), anyString());

        mockMvc.perform(delete("/api/urls/aB3kZ1"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.URL_FORBIDDEN.getMessage()));
    }
}