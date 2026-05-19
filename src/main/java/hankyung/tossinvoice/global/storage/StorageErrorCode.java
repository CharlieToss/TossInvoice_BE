package hankyung.tossinvoice.global.storage;

import hankyung.tossinvoice.global.exception.ErrorCode;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum StorageErrorCode implements ErrorCode {
    EMPTY_FILE(HttpStatus.BAD_REQUEST, "STORAGE_001", "업로드 파일이 비어있습니다."),
    UNSUPPORTED_CONTENT_TYPE(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "STORAGE_002", "지원하지 않는 파일 형식입니다."),
    UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "STORAGE_003", "파일 업로드에 실패했습니다.");

    private final HttpStatus status;
    private final String errorCode;
    private final String message;
}
