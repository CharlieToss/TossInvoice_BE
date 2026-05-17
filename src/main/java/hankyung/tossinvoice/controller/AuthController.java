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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    // 신규 회원가입 요청을 처리하고 생성된 사용자 ID를 반환합니다.
    @PostMapping("/api/v1/auth/signup")
    public ResponseEntity<SignupResponse> signup(@Valid @RequestBody SignupRequest request) {
        SignupResponse response = authService.signup(request);

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
