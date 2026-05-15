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
    private Integer id;

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

    @Column(name = "password_hash", length = 255, nullable = false)
    private String passwordHash;

    @Column(name = "deleted_at")
    private LocalDate deletedAt;

    @Column(name = "report_count", nullable = false)
    private Integer reportCount = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "company_type", length = 20, nullable = false)
    private CompanyType companyType;

    @Builder
    public UserEntity(String companyName, String businessType, String businessNumber,
                      String ceoName, String account, String bank,
                      String email, String passwordHash) {
        this.companyName = companyName;
        this.businessType = businessType;
        this.businessNumber = businessNumber;
        this.ceoName = ceoName;
        this.account = account;
        this.bank = bank;
        this.email = email;
        this.passwordHash = passwordHash;
    }
}