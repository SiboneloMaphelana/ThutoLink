package com.thuto.backend_thutoLink.auth;

import com.thuto.backend_thutoLink.model.UserAccount;
import com.thuto.backend_thutoLink.service.PlatformService;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AuthTokenService {
    private final PlatformService platformService;
    private final Map<String, String> sessions = new ConcurrentHashMap<>();

    public AuthTokenService(PlatformService platformService) {
        this.platformService = platformService;
    }

    public String issueToken(UserAccount account) {
        String token = UUID.randomUUID().toString();
        sessions.put(token, account.id());
        return token;
    }

    public UserAccount resolve(String token) {
        String userId = sessions.get(token);
        if (userId == null) {
            return null;
        }
        return platformService.findUserById(userId);
    }
}
