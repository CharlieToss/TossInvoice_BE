package hankyung.tossinvoice.controller;

import hankyung.tossinvoice.controller.support.annotation.UserId;
import hankyung.tossinvoice.dto.notification.res.NotificationPageResponse;
import hankyung.tossinvoice.service.NotificationService;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/api/v1/notifications")
    public ResponseEntity<NotificationPageResponse> getNotifications(
            @UserId Long userId,
            @RequestParam(required = false) Long cursorId,
            @Min(1) @RequestParam(defaultValue = "6") int size
    ) {
        return ResponseEntity.ok(notificationService.getNotifications(userId, cursorId, size));
    }
}
