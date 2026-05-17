package hankyung.tossinvoice.dto.user.res;

import lombok.Builder;

@Builder
public record CompanySearchResponse(
        String companyName,
        String status,
        String businessNumber,
        String ceoName,
        String businessType,
        String bank,
        String account,
        String companyType,
        String address,
        String phone,
        String email
) {
}
