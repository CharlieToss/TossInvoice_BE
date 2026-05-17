package hankyung.tossinvoice.controller;

import hankyung.tossinvoice.dto.user.res.CompanySearchResponse;
import hankyung.tossinvoice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    // 사업자번호로 거래처(회원) 정보와 신고 카운트 기반 위험도를 조회합니다.
    @GetMapping("/api/v1/company-search/{businessNumber}")
    public ResponseEntity<CompanySearchResponse> findByBusinessNumber(
            @PathVariable String businessNumber
    ) {
        CompanySearchResponse companySearchResponse = userService.findByBusinessNumber(businessNumber);

        return ResponseEntity.ok().body(companySearchResponse);
    }
}
