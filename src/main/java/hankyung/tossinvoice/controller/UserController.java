package hankyung.tossinvoice.controller;

import hankyung.tossinvoice.controller.support.annotation.UserId;
import hankyung.tossinvoice.dto.user.req.UpdateAccountRequest;
import hankyung.tossinvoice.dto.user.req.UpdatePasswordRequest;
import hankyung.tossinvoice.dto.user.res.CompanySearchResponse;
import hankyung.tossinvoice.dto.user.res.MyPageResponse;
import hankyung.tossinvoice.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    // 사업자번호로 거래처(회원) 정보와 신고 카운트 기반 위험도를 조회합니다.
    @GetMapping("/api/v1/company/{businessNumber}")
    public ResponseEntity<CompanySearchResponse> findByBusinessNumber(
            @PathVariable String businessNumber
    ) {
        CompanySearchResponse companySearchResponse = userService.findByBusinessNumber(businessNumber);

        return ResponseEntity.ok().body(companySearchResponse);
    }

    // 마이페이지 조회 — 본인 회원정보 전체를 반환합니다.
    @GetMapping("/api/v1/users/me")
    public ResponseEntity<MyPageResponse> getMyPage(@UserId Long userId) {
        return ResponseEntity.ok(userService.getMyPage(userId));
    }

    // 계좌번호 변경 — multipart/form-data로 통장사본 이미지 + 새 계좌번호를 함께 받습니다.
    //   - data     : UpdateAccountRequest JSON (account)
    //   - bankbook : 새 통장사본 이미지
    @PatchMapping(value = "/api/v1/users/me/account", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> updateAccount(
            @UserId Long userId,
            @Valid @RequestPart("data") UpdateAccountRequest request,
            @RequestPart("bankbook") MultipartFile bankbook
    ) {
        userService.updateAccount(userId, request, bankbook);
        return ResponseEntity.noContent().build();
    }

    // 비밀번호 변경 — 새 비밀번호 평문을 받아 BCrypt 해싱 후 저장.
    @PatchMapping("/api/v1/users/me/password")
    public ResponseEntity<Void> updatePassword(
            @UserId Long userId,
            @Valid @RequestBody UpdatePasswordRequest request
    ) {
        userService.updatePassword(userId, request);
        return ResponseEntity.noContent().build();
    }
}
