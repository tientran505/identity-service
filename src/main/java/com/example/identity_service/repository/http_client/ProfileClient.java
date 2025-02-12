package com.example.identity_service.repository.http_client;

import com.example.identity_service.dto.request.ProfileCreationRequest;
import com.example.identity_service.dto.response.UserProfileCreationResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "profile-service",  url = "${app.services.profile-service}")
public interface ProfileClient {
    @PostMapping(value = "/profiles", produces = MediaType.APPLICATION_JSON_VALUE)
    UserProfileCreationResponse createProfile(@RequestBody ProfileCreationRequest profile);
}
