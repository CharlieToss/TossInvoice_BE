package hankyung.tossinvoice.dto.user.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

// 비밀번호 변경 요청. 새 비밀번호 평문을 받아 서버에서 BCrypt 해싱 후 저장합니다.
@Builder
public record UpdatePasswordRequest(
        @NotBlank(message = "새 비밀번호를 입력해주세요.")
        @Size(min = 8, max = 100, message = "비밀번호는 8자 이상 100자 이하로 입력해주세요.")
        String password
) {
}
