package hankyung.tossinvoice.controller;

import hankyung.tossinvoice.controller.support.annotation.UserId;
import hankyung.tossinvoice.dto.auth.req.LoginRequest;
import hankyung.tossinvoice.dto.auth.req.ReissueRequest;
import hankyung.tossinvoice.dto.auth.req.SignupRequest;
import hankyung.tossinvoice.dto.auth.res.JwtTokenResponse;
import hankyung.tossinvoice.dto.auth.res.SignupResponse;
import hankyung.tossinvoice.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    // 신규 회원가입 요청을 multipart/form-data로 받습니다.
    //   - data                 : SignupRequest JSON
    //   - businessRegistration : 사업자등록증 PDF
    //   - bankbook             : 통장사본 이미지
    @PostMapping(value = "/api/v1/auth/signup", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<SignupResponse> signup(
            @Valid @RequestPart("data") SignupRequest request,
            @RequestPart("businessRegistration") MultipartFile businessRegistration,
            @RequestPart("bankbook") MultipartFile bankbook
    ) {
        SignupResponse response = authService.signup(request, businessRegistration, bankbook);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    // 이메일/비밀번호로 로그인하고 access/refresh 토큰을 발급합니다.
    @PostMapping("/api/v1/auth/login")
    public ResponseEntity<JwtTokenResponse> login(@Valid @RequestBody LoginRequest request) {
        JwtTokenResponse response = authService.login(request);

        return ResponseEntity.ok(response);
    }

    // refresh 토큰을 검증해 새 access/refresh 토큰 한 쌍을 발급합니다(RTR).
    @PostMapping("/api/v1/auth/reissue")
    public ResponseEntity<JwtTokenResponse> reissue(@Valid @RequestBody ReissueRequest request) {
        JwtTokenResponse response = authService.reissue(request);

        return ResponseEntity.ok(response);
    }

    // 현재 사용자의 refresh 토큰을 폐기해 로그아웃 상태로 만듭니다.
    @PostMapping("/api/v1/auth/logout")
    public ResponseEntity<Void> logout(@UserId Long userId) {
        authService.logout(userId);

        return ResponseEntity.noContent().build();
    }
}
