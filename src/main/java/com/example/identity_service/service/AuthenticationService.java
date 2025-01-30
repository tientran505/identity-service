package com.example.identity_service.service;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.StringJoiner;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.example.identity_service.dto.request.AuthenticationRequest;
import com.example.identity_service.dto.request.IntrospectRequest;
import com.example.identity_service.dto.request.LogoutRequest;
import com.example.identity_service.dto.request.RefreshTokenRequest;
import com.example.identity_service.dto.response.AuthenticationResponse;
import com.example.identity_service.dto.response.IntrospectResponse;
import com.example.identity_service.entity.ExpiredToken;
import com.example.identity_service.entity.User;
import com.example.identity_service.exception.AppException;
import com.example.identity_service.exception.ErrorCode;
import com.example.identity_service.repository.ExpiredTokenRepository;
import com.example.identity_service.repository.UserRepository;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationService {
    UserRepository userRepository;
    ExpiredTokenRepository expiredTokenRepository;

    @NonFinal
    @Value("${jwt.signerKey}")
    protected String SIGNER_KEY;

    @NonFinal
    @Value("${jwt.valid-duration}")
    protected long VALID_DURATION;

    @NonFinal
    @Value("${jwt.refreshable-duration}")
    protected long REFRESHABLE_DURATION;

    public AuthenticationResponse authenticate(AuthenticationRequest authenticationRequest) {
        log.info("Signer key: {}", SIGNER_KEY);

        User user = userRepository
                .findByUsername(authenticationRequest.getUsername())
                .orElseThrow(() -> new AppException(ErrorCode.USER_INVALID));

        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        boolean isAuthenticated = passwordEncoder.matches(authenticationRequest.getPassword(), user.getPassword());

        if (!isAuthenticated) {
            throw new AppException(ErrorCode.USER_UNAUTHENTICATED);
        }

        String token = generateAccessToken(user);
        return AuthenticationResponse.builder()
                .access_token(token)
                .isAuthenticated(true)
                .build();

        //        return passwordEncoder.matches(authenticationRequest.getPassword(), user.getPassword());
    }

    private String generateAccessToken(User user) {
        JWSHeader headers = new JWSHeader(JWSAlgorithm.HS512);
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .subject(user.getUsername())
                .issuer("tientran")
                .issueTime(new Date())
                .expirationTime(new Date(
                        Instant.now().plus(VALID_DURATION, ChronoUnit.SECONDS).toEpochMilli()))
                .jwtID(UUID.randomUUID().toString())
                .claim("scope", scopeBuilder(user))
                .build();
        Payload payload = new Payload(claims.toJSONObject());
        JWSObject jwsObject = new JWSObject(headers, payload);

        try {
            jwsObject.sign(new MACSigner(SIGNER_KEY));
        } catch (JOSEException e) {
            log.error("Cannot create token", e);
            throw new RuntimeException(e);
        }

        return jwsObject.serialize();
    }

    public IntrospectResponse introspect(IntrospectRequest req) throws JOSEException, ParseException {
        String token = req.getAccessToken();
        log.info("token: {}", token);
        boolean isValid = true;
        try {
            verifyToken(token, false);
        } catch (AppException e) {
            isValid = false;
        }

        return IntrospectResponse.builder().valid(isValid).build();
    }

    private String scopeBuilder(User user) {
        StringJoiner stringJoiner = new StringJoiner(" ");

        if (!CollectionUtils.isEmpty(user.getRoles())) {
            user.getRoles().forEach(role -> {
                stringJoiner.add("ROLE_" + role.getName());

                if (!CollectionUtils.isEmpty(role.getPermissions())) {
                    role.getPermissions().forEach(permission -> {
                        stringJoiner.add(permission.getName());
                    });
                }
            });
        }

        return stringJoiner.toString();
    }

    private SignedJWT verifyToken(String token, boolean isRefresh) throws JOSEException, ParseException {
        JWSVerifier verifier = new MACVerifier(SIGNER_KEY.getBytes());
        SignedJWT signedJWT = SignedJWT.parse(token);

        Date expirationTime = isRefresh
                ? new Date(signedJWT
                        .getJWTClaimsSet()
                        .getIssueTime()
                        .toInstant()
                        .plus(REFRESHABLE_DURATION, ChronoUnit.SECONDS)
                        .toEpochMilli())
                : signedJWT.getJWTClaimsSet().getExpirationTime();

        boolean isVerified = signedJWT.verify(verifier);

        if (!(isVerified && expirationTime.after(new Date()))) {
            throw new AppException(ErrorCode.USER_UNAUTHENTICATED);
        }

        if (expiredTokenRepository.existsById(signedJWT.getJWTClaimsSet().getJWTID())) {
            throw new AppException(ErrorCode.USER_UNAUTHENTICATED);
        }

        return signedJWT;
    }

    public void logout(LogoutRequest req) throws ParseException, JOSEException {
        try {
            SignedJWT signedToken = verifyToken(req.getAccessToken(), true);
            String jit = signedToken.getJWTClaimsSet().getJWTID();
            Date expiryTime = signedToken.getJWTClaimsSet().getExpirationTime();

            ExpiredToken expiredToken =
                    ExpiredToken.builder().id(jit).expiryTime(expiryTime).build();

            expiredTokenRepository.save(expiredToken);
        } catch (AppException e) {
            log.info("Token already expired");
        }
    }

    public AuthenticationResponse refreshToken(RefreshTokenRequest req) throws ParseException, JOSEException {
        var signedJWT = verifyToken(req.getRefreshToken(), true);

        var jit = signedJWT.getJWTClaimsSet().getJWTID();
        var expiryTime = signedJWT.getJWTClaimsSet().getExpirationTime();

        ExpiredToken expiredToken =
                ExpiredToken.builder().id(jit).expiryTime(expiryTime).build();

        expiredTokenRepository.save(expiredToken);

        var username = signedJWT.getJWTClaimsSet().getSubject();

        User user = userRepository
                .findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_UNAUTHENTICATED));

        String token = generateAccessToken(user);
        return AuthenticationResponse.builder()
                .access_token(token)
                .isAuthenticated(true)
                .build();
    }
}
