package com.github.prajjwal.florio.util;

import com.github.prajjwal.florio.dto.AuthResponse;
import com.github.prajjwal.florio.service.AuthService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private static final String FRONTEND_REDIRECT_URL = "http://localhost:5173/oauth2/callback";
    private final AuthService authService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");

        log.info("OAuth2 login success for email = {}", email);

        AuthResponse authResponse = authService.oauth2Login(email, name);
        String redirectUrl = FRONTEND_REDIRECT_URL +
                "?token=" + authResponse.getAccessToken() +
                "&refreshToken=" + authResponse.getRefreshToken() +
                "&role=" + authResponse.getUser().getRole();

        response.sendRedirect(redirectUrl);
    }
}