package hankyung.tossinvoice.global.storage;

import org.springframework.web.multipart.MultipartFile;

import java.util.Set;

// 외부 스토리지(GCS 등) 어댑터가 구현하는 포트 인터페이스입니다.
// 도메인 로직은 이 포트만 알고 있어 구현체에 의존하지 않습니다.
public interface StoragePort {

    // 파일을 업로드하고 접근 가능한 URL을 반환합니다.
    // pathHint는 키 prefix(예: "signatures", "users/business-registration")이며 구현체가 무시할 수 있습니다.
    String upload(MultipartFile file, String pathHint);

    // content-type 허용 목록을 강제해 업로드합니다.
    // 허용되지 않은 content-type이면 BaseException(StorageErrorCode.UNSUPPORTED_CONTENT_TYPE)이 발생합니다.
    String upload(MultipartFile file, String pathHint, Set<String> allowedContentTypes);
}
