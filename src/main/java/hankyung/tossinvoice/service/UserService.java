package hankyung.tossinvoice.service;

import hankyung.tossinvoice.domain.UserEntity;
import hankyung.tossinvoice.domain.exception.UserErrorCode;
import hankyung.tossinvoice.dto.user.res.CompanySearchResponse;
import hankyung.tossinvoice.global.exception.BaseException;
import hankyung.tossinvoice.repository.ReportRepository;
import hankyung.tossinvoice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final ReportRepository reportRepository;

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
}
