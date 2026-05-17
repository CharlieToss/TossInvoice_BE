package hankyung.tossinvoice.global.storage.adapter;

import hankyung.tossinvoice.global.storage.StoragePort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

// 실제 스토리지 연동이 붙기 전까지 사용하는 mock 구현체입니다.
// 추후 S3StorageAdapter 등을 만들 때 @Primary를 옮기거나 @Profile로 활성화 환경을 분리하세요.
@Slf4j
@Primary
@Component
public class MockStorageAdapter implements StoragePort {

    @Override
    public String upload(MultipartFile file, String pathHint) {
        if (file == null || file.isEmpty()) {
            return null;
        }

        String fakeUrl = "mock://" + pathHint + "/" + UUID.randomUUID();
        log.info("[MockStorage] upload path={} size={} originalFilename={} -> {}",
                pathHint, file.getSize(), file.getOriginalFilename(), fakeUrl);
        return fakeUrl;
    }
}
