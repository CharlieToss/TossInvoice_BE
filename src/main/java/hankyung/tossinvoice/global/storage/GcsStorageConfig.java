package hankyung.tossinvoice.global.storage;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;

// GCP Cloud Storage 클라이언트를 빈으로 등록합니다.
// credentials JSON은 classpath 리소스에서 읽고, 프로젝트 ID는 application.yml에서 주입합니다.
@Configuration
public class GcsStorageConfig {

    @Value("${cloud.storage.project-id}")
    private String projectId;

    @Value("${cloud.storage.credentials-location}")
    private String credentialsLocation;

    @Bean
    public Storage storage() throws IOException {
        ClassPathResource resource = new ClassPathResource(credentialsLocation);
        GoogleCredentials credentials = GoogleCredentials.fromStream(resource.getInputStream());

        return StorageOptions.newBuilder()
                .setProjectId(projectId)
                .setCredentials(credentials)
                .build()
                .getService();
    }
}
