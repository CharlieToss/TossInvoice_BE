package hankyung.tossinvoice.controller;

import hankyung.tossinvoice.controller.support.annotation.UserId;
import hankyung.tossinvoice.dto.dashboard.res.DashboardHomeResponse;
import hankyung.tossinvoice.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    // 홈(02-B) 통합 대시보드 — KPI 4종 + 6개월 거래 추이 차트를 한 번에 반환합니다.
    // 알림 미리보기·거래 미리보기는 클라이언트가 기존 endpoint(`/api/v1/notifications`, `/api/v1/trades`)를 별도로 호출합니다.
    @GetMapping("/api/v1/dashboard/home")
    public ResponseEntity<DashboardHomeResponse> getHome(@UserId Long userId) {
        return ResponseEntity.ok(dashboardService.getHome(userId));
    }
}
