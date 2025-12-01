package com.streamflix.playback.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.cloudfront.CloudFrontUtilities;
import software.amazon.awssdk.services.cloudfront.model.CannedSignerRequest;
import software.amazon.awssdk.services.cloudfront.url.SignedUrl;

import java.nio.file.Path;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
@Slf4j
public class CloudFrontSigningService {

    @Value("${aws.cloudfront.domain}")
    private String cloudFrontDomain;

    @Value("${aws.cloudfront.key-pair-id}")
    private String keyPairId;

    @Value("${aws.cloudfront.private-key}")
    private String privateKeyPath;

    @Value("${aws.cloudfront.signed-url-expiration:14400}")
    private int expirationSeconds;

    private final CloudFrontUtilities cloudFrontUtilities = CloudFrontUtilities.create();

    public String generateSignedUrl(String resourcePath) {
        try {
            String resourceUrl = String.format("https://%s%s", cloudFrontDomain, resourcePath);
            Instant expirationTime = Instant.now().plus(expirationSeconds, ChronoUnit.SECONDS);

            CannedSignerRequest signerRequest = CannedSignerRequest.builder()
                    .resourceUrl(resourceUrl)
                    .privateKey(Path.of(privateKeyPath))
                    .keyPairId(keyPairId)
                    .expirationDate(expirationTime)
                    .build();

            SignedUrl signedUrl = cloudFrontUtilities.getSignedUrlWithCannedPolicy(signerRequest);
            
            log.debug("Generated signed URL for resource: {}", resourcePath);
            return signedUrl.url();
        } catch (Exception e) {
            log.error("Failed to generate signed URL for resource: {}", resourcePath, e);
            throw new RuntimeException("Failed to generate streaming URL", e);
        }
    }

    public String generateSignedCookie(String resourcePath) {
        // For cookie-based authentication (useful for HLS manifests)
        try {
            String resourceUrl = String.format("https://%s%s/*", cloudFrontDomain, resourcePath);
            Instant expirationTime = Instant.now().plus(expirationSeconds, ChronoUnit.SECONDS);

            CannedSignerRequest signerRequest = CannedSignerRequest.builder()
                    .resourceUrl(resourceUrl)
                    .privateKey(Path.of(privateKeyPath))
                    .keyPairId(keyPairId)
                    .expirationDate(expirationTime)
                    .build();

            // Return cookie value
            return cloudFrontUtilities.getCookiesForCannedPolicy(signerRequest).toString();
        } catch (Exception e) {
            log.error("Failed to generate signed cookie for resource: {}", resourcePath, e);
            throw new RuntimeException("Failed to generate streaming cookie", e);
        }
    }
}
