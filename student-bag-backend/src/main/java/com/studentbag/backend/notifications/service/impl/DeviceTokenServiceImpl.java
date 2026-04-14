package com.studentbag.backend.notifications.service.impl;

import com.studentbag.backend.notifications.dto.request.SaveDeviceTokenRequest;
import com.studentbag.backend.notifications.entity.UserDeviceToken;
import com.studentbag.backend.notifications.repository.UserDeviceTokenRepository;
import com.studentbag.backend.notifications.service.DeviceTokenService;
import com.studentbag.backend.users.entity.User;
import com.studentbag.backend.users.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class DeviceTokenServiceImpl implements DeviceTokenService {

    private final UserDeviceTokenRepository userDeviceTokenRepository;
    private final UserRepository userRepository;

    @Override
    public void saveToken(UUID userId, SaveDeviceTokenRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        UserDeviceToken token = userDeviceTokenRepository.findByFcmToken(request.getFcmToken())
                .orElse(UserDeviceToken.builder().fcmToken(request.getFcmToken()).build());

        token.setUser(user);
        token.setDeviceType(request.getDeviceType());
        token.setActive(true);

        userDeviceTokenRepository.save(token);
    }
}