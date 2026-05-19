package hankyung.tossinvoice.service;

import hankyung.tossinvoice.domain.RefreshTokenEntity;
import hankyung.tossinvoice.domain.UserEntity;
import hankyung.tossinvoice.domain.constant.AuthConstant;
import hankyung.tossinvoice.domain.exception.AuthErrorCode;
import hankyung.tossinvoice.dto.auth.req.LoginRequest;
import hankyung.tossinvoice.dto.auth.req.ReissueRequest;
import hankyung.tossinvoice.dto.auth.req.SignupRequest;
import hankyung.tossinvoice.dto.auth.res.JwtTokenResponse;
import hankyung.tossinvoice.dto.auth.res.SignupResponse;
import hankyung.tossinvoice.global.exception.BaseException;
import hankyung.tossinvoice.global.exception.GlobalErrorCode;
import hankyung.tossinvoice.global.storage.StoragePort;
import hankyung.tossinvoice.repository.RefreshTokenRepository;
import hankyung.tossinvoice.repository.UserRepository;
import hankyung.tossinvoice.security.jwt.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthService {
    // 회원가입 시 사업자등록증은 PDF만, 통장사본은 이미지(jpeg/png)만 허용합니다.
    private static final Set<String> BUSINESS_REGISTRATION_ALLOWED_TYPES = Set.of("application/pdf");
    private static final Set<String> BANKBOOK_ALLOWED_TYPES = Set.of("image/jpeg", "image/png");

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final StoragePort storagePort;

    @Transactional
    public SignupResponse signup(SignupRequest request, MultipartFile businessRegistration, MultipartFile bankbook) {
        // 이메일/사업자번호는 unique 컬럼이라 가입 전에 중복 여부를 먼저 검사합니다.
        if (userRepository.existsByEmail(request.email())) {
            throw BaseException.type(AuthErrorCode.EMAIL_DUPLICATED);
        }
        if (userRepository.existsByBusinessNumber(request.businessNumber())) {
            throw BaseException.type(AuthErrorCode.BUSINESS_NUMBER_DUPLICATED);
        }

        // 중복 검사 이후에 파일 업로드를 수행해 불필요한 GCS 비용을 줄입니다.
        String businessRegistrationUrl = storagePort.upload(
                businessRegistration, "users/business-registration", BUSINESS_REGISTRATION_ALLOWED_TYPES);
        String bankbookUrl = storagePort.upload(
                bankbook, "users/bankbook", BANKBOOK_ALLOWED_TYPES);

        // 비밀번호 평문은 BCrypt로 해싱해 DB에는 해시값만 보관합니다.
        String passwordHash = passwordEncoder.encode(request.password());

        UserEntity user = UserEntity.builder()
                .companyName(request.companyName())
                .businessType(request.businessType())
                .businessNumber(request.businessNumber())
                .ceoName(request.ceoName())
                .account(request.account())
                .bank(request.bank())
                .email(request.email())
                .address(request.address())
                .phone(request.phone())
                .passwordHash(passwordHash)
                .companyType(request.companyType())
                .businessRegistrationUrl(businessRegistrationUrl)
                .bankbookUrl(bankbookUrl)
                .build();

        UserEntity saved = userRepository.save(user);

        return SignupResponse.builder()
                .userId(saved.getId())
                .build();
    }

    @Transactional
    public JwtTokenResponse login(LoginRequest request) {
        // 이메일 미존재와 비밀번호 불일치 모두 동일한 에러로 응답해 계정 존재 여부 노출을 막습니다.
        UserEntity user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> BaseException.type(AuthErrorCode.INVALID_CREDENTIALS));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw BaseException.type(AuthErrorCode.INVALID_CREDENTIALS);
        }

        return issueTokensAndPersist(user.getId());
    }

    // 도난 의심 분기에서 deleteById를 커밋해야 하므로 BaseException은 롤백 대상에서 제외합니다.
    @Transactional(noRollbackFor = BaseException.class)
    public JwtTokenResponse reissue(ReissueRequest request) {
        // 서명/만료/타입 검증을 먼저 통과한 토큰만 DB와 비교합니다.
        Claims claims = jwtUtil.getTokenBody(request.refreshToken());

        String tokenType = claims.get(AuthConstant.TOKEN_TYPE_CLAIM_NAME, String.class);
        if (!AuthConstant.REFRESH_TOKEN_TYPE.equals(tokenType)) {
            throw BaseException.type(GlobalErrorCode.INVALID_TOKEN_TYPE);
        }

        Long userId = claims.get(AuthConstant.USER_ID_CLAIM_NAME, Long.class);
        if (userId == null) {
            throw BaseException.type(GlobalErrorCode.INVALID_JWT);
        }

        RefreshTokenEntity stored = refreshTokenRepository.findById(userId)
                .orElseThrow(() -> BaseException.type(GlobalErrorCode.REFRESH_TOKEN_NOT_FOUND));

        // DB의 토큰과 다르다면 누군가 이미 재발급을 받은 상황이라 도난 의심으로 보고 강제 로그아웃 처리합니다.
        if (!stored.getToken().equals(request.refreshToken())) {
            refreshTokenRepository.deleteById(userId);
            throw BaseException.type(GlobalErrorCode.REFRESH_TOKEN_MISMATCH);
        }

        return issueTokensAndPersist(userId);
    }

    @Transactional
    public void logout(Long userId) {
        // 저장된 refresh 토큰이 없어도 멱등하게 동작하도록 존재 여부에 관계없이 삭제를 시도합니다.
        refreshTokenRepository.deleteById(userId);
    }

    // 로그인/재발급 양쪽에서 동일한 방식으로 토큰을 발급하고 RTR 정책에 따라 DB를 upsert합니다.
    private JwtTokenResponse issueTokensAndPersist(Long userId) {
        JwtTokenResponse tokens = jwtUtil.generateTokens(userId);

        LocalDateTime expiresAt = LocalDateTime.now()
                .plus(jwtUtil.getRefreshTokenExpirePeriod(), ChronoUnit.MILLIS);

        RefreshTokenEntity refreshToken = refreshTokenRepository.findById(userId)
                .map(existing -> {
                    existing.update(tokens.refreshToken(), expiresAt);
                    return existing;
                })
                .orElseGet(() -> RefreshTokenEntity.builder()
                        .userId(userId)
                        .token(tokens.refreshToken())
                        .expiresAt(expiresAt)
                        .build());
        refreshTokenRepository.save(refreshToken);

        return tokens;
    }
}
