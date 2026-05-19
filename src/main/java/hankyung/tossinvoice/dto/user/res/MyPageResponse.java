package hankyung.tossinvoice.dto.user.res;

import lombok.Builder;

// 마이페이지 조회 응답 — 로그인 정보 + 회원정보(OCR 추출 정보 포함) 전체를 반환합니다.
// businessRegistrationUrl/bankbookUrl은 GCS public URL로, 클라이언트가 바로 뷰어에 노출합니다.
@Builder
public record MyPageResponse(
        // 로그인 정보
        String email,
        String phone,

        // 회원정보(OCR 추출, 수정 불가)
        String companyName,
        String businessNumber,
        String ceoName,
        String companyType,
        String businessType,
        String address,
        String bank,
        String account,

        // 등록 서류 URL
        String businessRegistrationUrl,
        String bankbookUrl
) {
}
