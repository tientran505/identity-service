package com.example.identity_service.repository.http_client;

import com.example.identity_service.configuration.AuthenticationRequestInterceptor;
import com.example.identity_service.dto.request.ProfileCreationRequest;
import com.example.identity_service.dto.response.UserProfileCreationResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(
        name = "profile-service",
        url = "${app.services.profile-service}",
        configuration = {AuthenticationRequestInterceptor.class}
)
public interface ProfileClient {
    @PostMapping(value = "/internal/profiles", produces = MediaType.APPLICATION_JSON_VALUE)
    UserProfileCreationResponse createProfile(@RequestBody ProfileCreationRequest profile);
}
