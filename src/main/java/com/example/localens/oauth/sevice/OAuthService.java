package com.example.localens.oauth.sevice;

import com.example.localens.member.domain.Member;
import com.example.localens.member.repository.MemberRepository;
import com.example.localens.oauth.domain.OAuthProvider;
import com.example.localens.oauth.dto.OAuthAttributes;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OAuthService extends DefaultOAuth2UserService {
    private final MemberRepository memberRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId().toUpperCase();
        OAuthProvider provider = OAuthProvider.valueOf(registrationId);
        String userNameAttributeName = userRequest.getClientRegistration().getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName();

        OAuthAttributes attributes = OAuthAttributes.of(provider, userNameAttributeName, oAuth2User.getAttributes());
        saveOrUpdate(attributes);

        return oAuth2User;
    }

    private Member saveOrUpdate(OAuthAttributes attributes) {
        Optional<Member> memberOptional = memberRepository.findByEmail(attributes.getEmail());
        if (memberOptional.isPresent()) {
            Member existingMember = memberOptional.get();
            existingMember.update(attributes.getName(), attributes.getProvider());
            return memberRepository.save(existingMember);
        } else {
            return memberRepository.save(attributes.toEntity());
        }
    }
}
