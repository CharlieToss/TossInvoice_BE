package hankyung.tossinvoice.service;

import hankyung.tossinvoice.domain.UserEntity;
import hankyung.tossinvoice.domain.exception.UserErrorCode;
import hankyung.tossinvoice.dto.user.res.CompanySearchResponse;
import hankyung.tossinvoice.global.exception.BaseException;
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

    @Transactional
    public CompanySearchResponse findByBusinessNumber(String businessNumber) {
        UserEntity user = userRepository.findByBusinessNumber(businessNumber)
                .orElseThrow(() -> BaseException.type(UserErrorCode.COMPANY_NOT_FOUND));

        CompanySearchResponse companySearchResponse = CompanySearchResponse
                .builder()
                .companyName(user.getCompanyName())
                .status(convertReportStatus(user.getReportCount()))
                .businessNumber(user.getBusinessNumber())
                .ceoName(user.getCeoName())
                .businessType(user.getBusinessType())
                .bank(user.getBank())
                .account(user.getAccount())
                .companyType(user.getCompanyType().getDescription())
                .build();

        return companySearchResponse;
    }

    private String convertReportStatus(Integer reportCount) {
        if(reportCount > 10) {
            return "위험";
        } else if(reportCount > 5) {
            return "주의";
        } else {
            return "정상";
        }
    }
}
