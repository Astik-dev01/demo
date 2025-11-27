package kg.taskflow.config;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.SetBucketPolicyArgs;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
@Getter
public class MinioConfig {

    @Value("${minio.endpoint}")
    private String endpoint;

    @Value("${minio.access-key}")
    private String accessKey;

    @Value("${minio.secret-key}")
    private String secretKey;

    @Value("${minio.bucket}")
    private String bucket;

    @Value("${minio.public-url:#{null}}")
    private String publicUrl;

    @Bean
    public MinioClient minioClient() {
        log.info("Configuring MinIO client with endpoint: {}", endpoint);

        MinioClient client = MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();

        initBucket(client);
        return client;
    }

    private void initBucket(MinioClient client) {
        try {
            boolean found = client.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
            if (!found) {
                client.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
                log.info("Bucket '{}' created successfully", bucket);

                // Set public read policy
                String policy = """
                    {
                        "Version": "2012-10-17",
                        "Statement": [
                            {
                                "Effect": "Allow",
                                "Principal": "*",
                                "Action": ["s3:GetObject"],
                                "Resource": ["arn:aws:s3:::%s/*"]
                            }
                        ]
                    }
                    """.formatted(bucket);

                client.setBucketPolicy(SetBucketPolicyArgs.builder()
                        .bucket(bucket)
                        .config(policy)
                        .build());
                log.info("Public read policy set for bucket '{}'", bucket);
            } else {
                log.info("Bucket '{}' already exists", bucket);
            }
        } catch (Exception e) {
            log.error("Error initializing MinIO bucket: {}", e.getMessage());
        }
    }

    public String getFileUrl(String filename) {
        String baseUrl = publicUrl != null ? publicUrl : endpoint;
        return baseUrl + "/" + bucket + "/" + filename;
    }
}
