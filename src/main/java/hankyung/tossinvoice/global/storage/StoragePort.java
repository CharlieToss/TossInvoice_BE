package hankyung.tossinvoice.global.storage;

import org.springframework.web.multipart.MultipartFile;

// 외부 스토리지(S3 등) 어댑터가 구현하는 포트 인터페이스입니다.
// 도메인 로직은 이 포트만 알고 있어 구현체(MockStorage / S3Storage 등)에 의존하지 않습니다.
public interface StoragePort {

    // 파일을 업로드하고 접근 가능한 URL을 반환합니다.
    // pathHint는 키 생성 시 참고용 경로(예: "signatures/sales-order")이며 구현체가 무시할 수 있습니다.
    String upload(MultipartFile file, String pathHint);
}
