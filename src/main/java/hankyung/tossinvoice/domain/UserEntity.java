package hankyung.tossinvoice.domain;

import hankyung.tossinvoice.domain.base.BaseTimeEntity;
import hankyung.tossinvoice.domain.constant.CompanyType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserEntity extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "company_name", length = 100, nullable = false)
    private String companyName;

    @Column(name = "business_type", length = 20, nullable = false)
    private String businessType;

    @Column(name = "business_number", length = 20, nullable = false, unique = true)
    private String businessNumber;

    @Column(name = "ceo_name", length = 20, nullable = false)
    private String ceoName;

    @Column(name = "account", length = 20, nullable = false)
    private String account;

    @Column(name = "bank", length = 20, nullable = false)
    private String bank;

    @Column(name = "email", length = 100, nullable = false, unique = true)
    private String email;

    // PI/PO/Invoice 문서 헤더에 표시되는 회사 주소(예: "서울특별시 종로구 종로12").
    @Column(name = "address", length = 200, nullable = false)
    private String address;

    // 문서 헤더에 표시되는 일반 연락처 전화(02-1234-5678 등).
    @Column(name = "phone", length = 20, nullable = false)
    private String phone;

    @Column(name = "password_hash", length = 255, nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "company_type", length = 20, nullable = false)
    private CompanyType companyType;

    // 사업자등록증 PDF 원본 GCS URL — 회원가입 시 업로드되며 수정 불가.
    @Column(name = "business_registration_url", length = 512, nullable = false)
    private String businessRegistrationUrl;

    // 통장사본 이미지 GCS URL — 회원가입 + 계좌번호 변경 시점에 갱신됩니다.
    @Column(name = "bankbook_url", length = 512, nullable = false)
    private String bankbookUrl;

    @Column(name = "deleted_at")
    private LocalDate deletedAt;

    @Builder
    public UserEntity(String companyName, String businessType, String businessNumber,
                      String ceoName, String account, String bank,
                      String email, String address, String phone,
                      String passwordHash, CompanyType companyType,
                      String businessRegistrationUrl, String bankbookUrl) {
        this.companyName = companyName;
        this.businessType = businessType;
        this.businessNumber = businessNumber;
        this.ceoName = ceoName;
        this.account = account;
        this.bank = bank;
        this.email = email;
        this.address = address;
        this.phone = phone;
        this.passwordHash = passwordHash;
        this.companyType = companyType;
        this.businessRegistrationUrl = businessRegistrationUrl;
        this.bankbookUrl = bankbookUrl;
    }

    // 계좌번호 변경 — 통장사본 OCR 1차 검증을 통과한 새 은행/계좌번호와 통장사본 URL을 함께 갱신합니다.
    public void updateAccount(String bank, String account, String bankbookUrl) {
        this.bank = bank;
        this.account = account;
        this.bankbookUrl = bankbookUrl;
    }

    // 비밀번호 변경 — BCrypt 해시값을 받아 저장합니다(평문은 서비스 레이어에서 해싱).
    public void updatePassword(String passwordHash) {
        this.passwordHash = passwordHash;
    }
}