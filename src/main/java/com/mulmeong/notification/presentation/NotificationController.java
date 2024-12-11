package com.mulmeong.notification.presentation;

import com.fasterxml.jackson.databind.ser.Serializers;
import com.mulmeong.notification.application.NotificationService;
import com.mulmeong.notification.common.response.BaseResponse;
import com.mulmeong.notification.common.utils.CursorPage;
import com.mulmeong.notification.dto.NotificationHistoryResponseDto;
import com.mulmeong.notification.dto.NotificationStatusResponseDto;
import com.mulmeong.notification.vo.NotificationHistoryResponseVo;
import com.mulmeong.notification.vo.NotificationStatusResponseVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("auth/v1")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/members/{memberUuid}/notifications")
    @Operation(summary = "회원의 전체 알림 목록 조회", tags = {"Notification Service"},
            description = """
                    - type : `all` / `unread` <br>
                    - kind : `feed` / `shorts` / `comment` / 
                    `recomment` / `follow` / `chat` / 
                    `grade` / `contest` / `report`<br>
                    """

    )
    BaseResponse<CursorPage<NotificationHistoryResponseVo>> getAllNotificationHistories(
            @PathVariable String memberUuid,
            @Parameter(
                    description = "(전체 알림/읽지 않은 알림) 조회, 기본 값은 전체 알림 조회",
                    schema = @Schema(allowableValues = {"all", "unread"})
            )
            @RequestParam(defaultValue = "all", value = "type", required = false) String type,
            @Parameter(
                    description = "각 알림 별 목록 조회, 기본값은 전체 목록에 대한 조회",
                    schema = @Schema(allowableValues = {"feed", "shorts", "comment", "recomment", "follow",
                                                        "chat", "grade", "contest", "report"})
            )
            @RequestParam(value = "kind", required = false) String kind,
            @RequestParam(value = "nextCursor", required = false) String lastId,
            @RequestParam(value = "pageSize", required = false) Integer pageSize,
            @RequestParam(value = "pageNo", required = false) Integer pageNo
    ) {
        CursorPage<NotificationHistoryResponseDto> cursorPage = notificationService.getNotificationHistoryByPage(
                memberUuid, type, kind, lastId, pageSize, pageNo);
        return new BaseResponse<>(
                CursorPage.toCursorPage(cursorPage, cursorPage.getContent().stream()
                        .map(NotificationHistoryResponseDto::toVo).toList()
                ));
    }

    @GetMapping("/members/{memberUuid}/notifications-status")
    @Operation(summary = "회원의 알림 종류 및 상태 조회", tags = {"Notification Service"},
            description = """
                    kind: `feed` / `shorts` / `comment` / 
                    `recomment` / `follow` / `chat` / 
                    `grade` / `contest` / `report`""")
    BaseResponse<List<NotificationStatusResponseVo>> getAllNotificationHistories(@PathVariable String memberUuid) {
        return new BaseResponse<>(notificationService.getAllNotificationStatus(memberUuid)
                .stream().map(NotificationStatusResponseDto::toVo).toList());
    }

    @PostMapping("/members/notifications/{historyUuid}")
    @Operation(summary = "회원의 알림 읽었을 때 상태 변경", tags = {"Notification Service"})
    BaseResponse<Void> updateNotificationHistory(
            @RequestHeader("Member-Uuid") String memberUuid, @PathVariable String historyUuid) {
        notificationService.updateNotificationHistoryRead(memberUuid, historyUuid);
        return new BaseResponse<>();
    }

    @PostMapping("/members/notifications-status/{type}")
    @Operation(summary = "회원의 알림 토글 상태 변경", tags = {"Notification Service"})
    BaseResponse<Void> updateNotificationStatus(
            @RequestHeader("Member-Uuid") String memberUuid, @PathVariable String type) {
        notificationService.updateNotificationStatus(memberUuid, type);
        return new BaseResponse<>();
    }

}
