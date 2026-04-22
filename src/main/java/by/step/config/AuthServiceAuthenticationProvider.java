package by.step.config;

import by.step.client.AuthServiceClient;
import by.step.dto.AuthResponseDto;
import by.step.dto.LoginRequestDto;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Collections;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthServiceAuthenticationProvider implements AuthenticationProvider {

    private final AuthServiceClient authServiceClient;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName().trim();
        String password = authentication.getCredentials().toString().trim();

        log.info("=== AUTH PROVIDER CALLED ===");
        log.info("Username: {}", username);

        try {
            LoginRequestDto loginRequest = new LoginRequestDto(username, password);
            AuthResponseDto response = authServiceClient.login(loginRequest);

            log.info("Response token: {}", response != null ? response.getToken() : "null");

            if (response != null && response.getToken() != null && !response.getToken().isEmpty()) {
                log.info("Authentication successful for: {}", username);

                ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
                HttpSession session = attr.getRequest().getSession(true);
                session.setAttribute("jwt_token", response.getToken());
                log.info("JWT token saved to session");

                return new UsernamePasswordAuthenticationToken(
                        username,
                        null,
                        Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
                );
            }
        } catch (Exception e) {
            log.error("Authentication failed: {}", e.getMessage());
            e.printStackTrace();
        }

        throw new BadCredentialsException("Invalid username or password");
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}