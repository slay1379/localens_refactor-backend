package com.example.localens.oauth.dto;

import com.example.localens.member.domain.Member;
import com.example.localens.oauth.domain.OAuthProvider;
import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Getter
public class OAuthAttributes {
    private final Map<String, Object> attributes;
    private final String nameAttributeKey;
    private final String name;
    private final String email;
    private final OAuthProvider provider;

    @Builder
    public OAuthAttributes(Map<String, Object> attributes, String nameAttributeKey, String name, String email, OAuthProvider provider) {
        this.attributes = attributes;
        this.nameAttributeKey = nameAttributeKey;
        this.name = name;
        this.email = email;
        this.provider = provider;
    }

    public static OAuthAttributes of(OAuthProvider provider, String userNameAttributeName, Map<String, Object> attributes) {
        switch (provider) {
            case GOOGLE -> {
                return ofGoogle(userNameAttributeName, attributes);
            }
            case KAKAO -> {
                return ofKakao(userNameAttributeName, attributes);
            }
            case NAVER -> {
                return ofNaver(userNameAttributeName, attributes);
            }
            default -> throw new IllegalArgumentException("Unsupported provider: " + provider);
        }
    }

    private static OAuthAttributes ofGoogle(String userNameAttributeName, Map<String,Object> attributes) {
        return OAuthAttributes.builder()
                .name((String) attributes.get("name"))
                .email((String) attributes.get("email"))
                .attributes(attributes)
                .nameAttributeKey(userNameAttributeName)
                .provider(OAuthProvider.GOOGLE)
                .build();
    }

    private static OAuthAttributes ofKakao(String userNameAttributeName, Map<String, Object> attributes) {
        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        return OAuthAttributes.builder()
                .name((String) kakaoAccount.get("profile_nickname"))
                .email((String) kakaoAccount.get("email"))
                .attributes(attributes)
                .nameAttributeKey(userNameAttributeName)
                .provider(OAuthProvider.KAKAO)
                .build();
    }

    private static OAuthAttributes ofNaver(String userNameAttributeName, Map<String, Object> attributes) {
        Map<String, Object> response = (Map<String, Object>) attributes.get("response");
        return OAuthAttributes.builder()
                .name((String) response.get("name"))
                .email((String) response.get("email"))
                .attributes(attributes)
                .nameAttributeKey(userNameAttributeName)
                .provider(OAuthProvider.NAVER)
                .build();
    }

    public Member toEntity() {
        return Member.builder()
                .name(name)
                .email(email)
                .provider(provider)
                .build();
    }
}
