package com.nationwide.nationwide_server.alarm;

import com.nationwide.nationwide_server._core._custom_annotation.LoginUser;
import com.nationwide.nationwide_server._core.util.ApiUtil;
import com.nationwide.nationwide_server._core.util.SessionUser;
import com.nationwide.nationwide_server.alarm.dto.AlarmResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/alarms")
@RequiredArgsConstructor
public class AlarmController {

    private final AlarmService alarmService;

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(@LoginUser SessionUser sessionUser) {
        return alarmService.subscribe(sessionUser);
    }

    @GetMapping
    public ResponseEntity<?> alarmSlice(
            @LoginUser SessionUser sessionUser,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        Slice<AlarmResponseDTO.ListDTO> response = alarmService.alarmSlice(sessionUser, pageable);
        return ResponseEntity.ok(ApiUtil.success(response));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<?> unreadCount(@LoginUser SessionUser sessionUser) {
        AlarmResponseDTO.UnreadCountDTO response = alarmService.unreadCount(sessionUser);
        return ResponseEntity.ok(ApiUtil.success(response));
    }

    @PutMapping("/{alarmId}/read")
    public ResponseEntity<?> readAlarm(
            @LoginUser SessionUser sessionUser,
            @PathVariable("alarmId") Long alarmId
    ) {
        alarmService.readAlarm(sessionUser, alarmId);
        return ResponseEntity.ok(ApiUtil.success(true));
    }
}
