package by.step.client;

import by.step.dto.AuthResponseDto;
import by.step.dto.LoginRequestDto;
import by.step.dto.RegistrationRequestDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "auth-service", url = "${auth-service.url:http://localhost:9510}")
public interface AuthServiceClient {

    @PostMapping(value = "/api/jwt/auth/login",
            consumes = "application/json",
            produces = "application/json")
    AuthResponseDto login(@RequestBody LoginRequestDto request);

    @PostMapping("/api/auth/register")
    AuthResponseDto register(@RequestBody RegistrationRequestDto request);
}