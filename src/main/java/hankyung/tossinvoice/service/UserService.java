package hankyung.tossinvoice.service;

import hankyung.tossinvoice.domain.TradeEntity;
import hankyung.tossinvoice.domain.UserEntity;
import hankyung.tossinvoice.domain.constant.NotificationType;
import hankyung.tossinvoice.domain.constant.TradeStatus;
import hankyung.tossinvoice.domain.exception.UserErrorCode;
import hankyung.tossinvoice.dto.user.req.UpdateAccountRequest;
import hankyung.tossinvoice.dto.user.req.UpdatePasswordRequest;
import hankyung.tossinvoice.dto.user.res.CompanySearchResponse;
import hankyung.tossinvoice.dto.user.res.MyPageResponse;
import hankyung.tossinvoice.global.exception.BaseException;
import hankyung.tossinvoice.global.storage.StoragePort;
import hankyung.tossinvoice.repository.ReportRepository;
import hankyung.tossinvoice.repository.TradeRepository;
import hankyung.tossinvoice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    // 계좌번호 변경 시 통장사본 이미지는 jpeg/png만 허용합니다.
    private static final Set<String> BANKBOOK_ALLOWED_TYPES = Set.of("image/jpeg", "image/png");

    private final UserRepository userRepository;
    private final ReportRepository reportRepository;
    private final TradeRepository tradeRepository;
    private final NotificationService notificationService;
    private final PasswordEncoder passwordEncoder;
    private final StoragePort storagePort;

    @Transactional(readOnly = true)
    public CompanySearchResponse findByBusinessNumber(String businessNumber) {
        UserEntity user = userRepository.findByBusinessNumber(businessNumber)
                .orElseThrow(() -> BaseException.type(UserErrorCode.COMPANY_NOT_FOUND));

        CompanySearchResponse companySearchResponse = CompanySearchResponse
                .builder()
                .companyName(user.getCompanyName())
                .status(convertReportStatus(user.getId()))
                .businessNumber(user.getBusinessNumber())
                .ceoName(user.getCeoName())
                .businessType(user.getBusinessType())
                .bank(user.getBank())
                .account(user.getAccount())
                .companyType(user.getCompanyType().getDescription())
                .address(user.getAddress())
                .phone(user.getPhone())
                .email(user.getEmail())
                .build();

        return companySearchResponse;
    }

    private String convertReportStatus(Long reportedId) {
        Long reportCount = reportRepository.countByReportedId(reportedId);

        if(reportCount > 5) {
            return "위험";
        } else if(reportCount > 1) {
            return "주의";
        } else {
            return "정상";
        }
    }

    // 마이페이지 조회 — 본인 회원정보 전체를 반환합니다.
    @Transactional(readOnly = true)
    public MyPageResponse getMyPage(Long userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> BaseException.type(UserErrorCode.COMPANY_NOT_FOUND));

        return MyPageResponse.builder()
                .email(user.getEmail())
                .phone(user.getPhone())
                .companyName(user.getCompanyName())
                .businessNumber(user.getBusinessNumber())
                .ceoName(user.getCeoName())
                .companyType(user.getCompanyType().getDescription())
                .businessType(user.getBusinessType())
                .address(user.getAddress())
                .bank(user.getBank())
                .account(user.getAccount())
                .businessRegistrationUrl(user.getBusinessRegistrationUrl())
                .bankbookUrl(user.getBankbookUrl())
                .build();
    }

    // 계좌번호 변경 — 새 통장사본 이미지를 GCS에 업로드하고 계좌번호 + 통장사본 URL을 함께 갱신합니다.
    // OCR 1차 검증은 클라이언트가 통과한 뒤 호출되므로 서버는 받은 값을 신뢰합니다.
    @Transactional
    public void updateAccount(Long userId, UpdateAccountRequest request, MultipartFile bankbook) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> BaseException.type(UserErrorCode.COMPANY_NOT_FOUND));

        String bankbookUrl = storagePort.upload(bankbook, "users/bankbook", BANKBOOK_ALLOWED_TYPES);
        user.updateAccount(request.account(), bankbookUrl);
        notifyPartners(user, NotificationType.PARTNER_ACCOUNT_CHANGED);
    }

    private void notifyPartners(UserEntity sender, NotificationType type) {
        String maskedAccount = maskAccount(sender.getAccount());
        Set<Long> partnerIds = tradeRepository.findActivePartnerTrades(sender.getId(), TradeStatus.CANCELLED)
                .stream()
                .map(t -> t.getSellerId().equals(sender.getId()) ? t.getBuyerId() : t.getSellerId())
                .collect(Collectors.toSet());
        partnerIds.forEach(partnerId ->
                notificationService.sendWithName(sender.getId(), partnerId, null, type, sender.getCompanyName(), maskedAccount));
    }

    private static String maskAccount(String account) {
        if (account == null) return "****";
        String digits = account.replaceAll("[^0-9]", "");
        if (digits.length() <= 8) return "****";
        return digits.substring(0, 4) + "****" + digits.substring(digits.length() - 4);
    }

    // 비밀번호 변경 — 평문 비밀번호를 BCrypt로 해싱해 저장합니다.
    @Transactional
    public void updatePassword(Long userId, UpdatePasswordRequest request) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> BaseException.type(UserErrorCode.COMPANY_NOT_FOUND));

        String passwordHash = passwordEncoder.encode(request.password());
        user.updatePassword(passwordHash);
    }
}
