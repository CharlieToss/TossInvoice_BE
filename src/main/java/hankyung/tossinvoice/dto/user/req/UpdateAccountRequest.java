package hankyung.tossinvoice.dto.user.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

// 계좌번호 변경 요청.
// 클라이언트가 통장사본 OCR로 1차 검증(통장주명 == 사업자등록증 대표자명)을 통과한 뒤 호출합니다.
// 서버는 OCR 결과를 신뢰하고 받은 계좌번호만 갱신합니다.
@Builder
public record UpdateAccountRequest(
        @NotBlank(message = "계좌번호를 입력해주세요.")
        @Size(max = 20, message = "계좌번호는 최대 20자까지 입력 가능합니다.")
        String account
) {
}
