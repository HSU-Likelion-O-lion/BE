package com.likelion.olion.global.config;

import com.likelion.olion.global.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public/pages")
@Tag(name = "공개 페이지", description = "고객지원과 약관 페이지의 공개 URL API")
public class PublicPageController {
    private final PublicPageProperties publicPageProperties;

    public PublicPageController(PublicPageProperties publicPageProperties) {
        this.publicPageProperties = publicPageProperties;
    }

    @GetMapping
    @Operation(summary = "공개 페이지 URL 조회", description = "고객지원, 이용약관, 개인정보처리방침 페이지 URL을 조회합니다.")
    public ResponseEntity<ApiResponse<PublicPageResponse>> getPublicPages() {
        return ResponseEntity.ok(ApiResponse.success(
                "공개 페이지 URL을 조회했습니다.", PublicPageResponse.from(publicPageProperties)));
    }
}
