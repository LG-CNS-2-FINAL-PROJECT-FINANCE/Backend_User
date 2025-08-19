package com.ddiring.backend_user.kakao;

import com.ddiring.backend_user.common.exception.BadParameter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class KakaoOAuthService {

    private final String KAKAO_TOKEN_URL = "https://kauth.kakao.com/oauth/token";
    private final String KAKAO_USER_INFO_URL = "https://kapi.kakao.com/v2/user/me";

    @Value("${spring.kakao.client-id}")
    private String clientId;

    @Value("${spring.kakao.redirect-uri}")
    private String redirectUri;

    @Value("${spring.kakao.client-secret}")
    private String clientSecret;

    public String getAccessToken(String code) {
        RestTemplate restTemplate = new RestTemplate();

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", clientId);
    // use configured redirectUri only
    params.add("redirect_uri", redirectUri);
        params.add("code", code);
        params.add("client_secret", clientSecret);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        try {
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    KAKAO_TOKEN_URL,
                    HttpMethod.POST,
                    request,
                    new ParameterizedTypeReference<>() {});
            Map<String, Object> body = response.getBody();
            if (body == null || body.get("access_token") == null) {
                throw new BadParameter("카카오 토큰 응답에 access_token이 없습니다.");
            }
            return (String) body.get("access_token");
        } catch (RestClientResponseException e) {
            log.error("카카오 토큰 발급 실패: status={} body={}", e.getStatusCode().value(), e.getResponseBodyAsString());
            String reason = e.getStatusText();
            try {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode node = mapper.readTree(e.getResponseBodyAsString());
                String err = node.path("error").asText(null);
                String desc = node.path("error_description").asText(null);
                if (err != null || desc != null) {
                    reason = String.format("%s%s%s",
                            err != null ? err : "",
                            (err != null && desc != null) ? " - " : "",
                            desc != null ? desc : "");
                }
            } catch (Exception ignore) {
                // ignore JSON parse errors
            }
            throw new BadParameter("카카오 토큰 발급 실패: " + reason);
        } catch (RestClientException e) {
            log.error("카카오 토큰 발급 중 통신 오류: {}", e.getMessage(), e);
            throw new BadParameter("카카오 토큰 발급 중 통신 오류가 발생했습니다.");
        }
    }

    public KakaoUserInfo getUserInfo(String accessToken) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Void> request = new HttpEntity<>(headers);

        try {
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    KAKAO_USER_INFO_URL,
                    HttpMethod.GET,
                    request,
                    new ParameterizedTypeReference<>() {}
            );

            Map<String, Object> body = response.getBody();
            if (body == null) {
                throw new BadParameter("카카오 사용자 정보 응답이 비어있습니다.");
            }

            Object idObj = body.get("id");
            if (idObj == null) {
                throw new BadParameter("카카오 사용자 정보에 id가 없습니다.");
            }
            Long id = Long.valueOf(idObj.toString());

            @SuppressWarnings("unchecked")
            Map<String, Object> kakaoAccount = (Map<String, Object>) body.get("kakao_account");
            String email = kakaoAccount != null ? (String) kakaoAccount.get("email") : null;

            return new KakaoUserInfo(email);
        } catch (RestClientResponseException e) {
            log.error("카카오 사용자 정보 조회 실패: status={} body={}", e.getStatusCode().value(), e.getResponseBodyAsString());
            String reason = e.getStatusText();
            try {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode node = mapper.readTree(e.getResponseBodyAsString());
                String err = node.path("error").asText(null);
                String desc = node.path("error_description").asText(null);
                if (err != null || desc != null) {
                    reason = String.format("%s%s%s",
                            err != null ? err : "",
                            (err != null && desc != null) ? " - " : "",
                            desc != null ? desc : "");
                }
            } catch (Exception ignore) {
                // ignore JSON parse errors
            }
            throw new BadParameter("카카오 사용자 정보 조회 실패: " + reason);
        } catch (RestClientException e) {
            log.error("카카오 사용자 정보 조회 중 통신 오류: {}", e.getMessage(), e);
            throw new BadParameter("카카오 사용자 정보 조회 중 통신 오류가 발생했습니다.");
        }
    }

    @Getter
    @AllArgsConstructor
    public static class KakaoUserInfo {
    private String email;
    }
}
