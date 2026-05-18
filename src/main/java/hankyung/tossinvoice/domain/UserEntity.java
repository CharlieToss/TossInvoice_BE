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

    @Column(name = "deleted_at")
    private LocalDate deletedAt;

    @Builder
    public UserEntity(String companyName, String businessType, String businessNumber,
                      String ceoName, String account, String bank,
                      String email, String address, String phone,
                      String passwordHash, CompanyType companyType) {
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
    }
}