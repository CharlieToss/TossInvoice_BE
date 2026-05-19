package hankyung.tossinvoice.global.storage.adapter;

import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import hankyung.tossinvoice.global.exception.BaseException;
import hankyung.tossinvoice.global.storage.StorageErrorCode;
import hankyung.tossinvoice.global.storage.StoragePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

// GCP Cloud Storage 기반 StoragePort 구현체입니다.
// content-type 검증은 어댑터 책임으로, 호출자가 allowedContentTypes를 넘기면 화이트리스트 검증을 수행합니다.
@Slf4j
@Component
@RequiredArgsConstructor
public class GcsStorageAdapter implements StoragePort {

    private final Storage storage;

    @Value("${cloud.storage.bucket}")
    private String bucket;

    @Override
    public String upload(MultipartFile file, String pathHint) {
        return upload(file, pathHint, null);
    }

    @Override
    public String upload(MultipartFile file, String pathHint, Set<String> allowedContentTypes) {
        if (file == null || file.isEmpty()) {
            throw BaseException.type(StorageErrorCode.EMPTY_FILE);
        }
        if (allowedContentTypes != null && !allowedContentTypes.isEmpty()
                && !allowedContentTypes.contains(file.getContentType())) {
            throw BaseException.type(StorageErrorCode.UNSUPPORTED_CONTENT_TYPE);
        }

        String objectKey = buildObjectKey(pathHint, file.getOriginalFilename());
        BlobInfo blobInfo = BlobInfo.newBuilder(bucket, objectKey)
                .setContentType(file.getContentType())
                .build();

        try {
            storage.create(blobInfo, file.getBytes());
        } catch (IOException e) {
            log.error("[GcsStorage] upload failed key={}", objectKey, e);
            throw BaseException.type(StorageErrorCode.UPLOAD_FAILED);
        }

        String publicUrl = String.format("https://storage.googleapis.com/%s/%s", bucket, objectKey);
        log.info("[GcsStorage] uploaded key={} contentType={} size={} -> {}",
                objectKey, file.getContentType(), file.getSize(), publicUrl);
        return publicUrl;
    }

    // 같은 이름이 들어와도 충돌하지 않도록 UUID 접미사를 붙입니다.
    private String buildObjectKey(String pathHint, String originalFilename) {
        String prefix = (pathHint == null || pathHint.isBlank()) ? "misc" : pathHint;
        String safeName = (originalFilename == null || originalFilename.isBlank()) ? "file" : originalFilename;
        return prefix + "/" + UUID.randomUUID() + "_" + safeName;
    }
}
