package com.likelion.olion.domain.community.controller;

import com.likelion.olion.domain.community.dto.CommunityRoomResponse;
import com.likelion.olion.domain.community.service.CommunityRoomService;
import com.likelion.olion.global.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api/community/rooms")
@Tag(name = "쉼터 목록", description = "책장 도서 기준 쉼터 목록 조회 API")
public class CommunityRoomController {
    private final CommunityRoomService communityRoomService;

    public CommunityRoomController(CommunityRoomService communityRoomService) {
        this.communityRoomService = communityRoomService;
    }

    @GetMapping
    @Operation(summary = "책별 쉼터 목록 조회", description = "오늘 독서 목표를 달성한 사용자의 책장 도서 기준으로 쉼터 목록을 조회합니다.")
    public ResponseEntity<ApiResponse<CommunityRoomResponse>> getRooms(Principal principal) {
        CommunityRoomResponse response = communityRoomService.getRooms(Long.valueOf(principal.getName()));
        String message = response.rooms().isEmpty()
                ? "아직 참여할 수 있는 쉼터가 없습니다."
                : "소통방 목록을 조회했습니다.";
        String code = response.rooms().isEmpty() ? "SUCCESS_EMPTY" : "SUCCESS";
        return ResponseEntity.ok(ApiResponse.success(
                code, org.springframework.http.HttpStatus.OK, message, response));
    }
}
