package com.thuto.backend_thutoLink.api;

import com.thuto.backend_thutoLink.auth.AuthTokenService;
import com.thuto.backend_thutoLink.model.UserAccount;
import com.thuto.backend_thutoLink.service.PlatformService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final PlatformService platformService;
    private final AuthTokenService authTokenService;

    public AuthController(PlatformService platformService, AuthTokenService authTokenService) {
        this.platformService = platformService;
        this.authTokenService = authTokenService;
    }

    @PostMapping("/login")
    public PlatformService.LoginResponse login(@RequestBody PlatformService.LoginRequest request) {
        UserAccount account = platformService.authenticate(request.email(), request.password());
        String token = authTokenService.issueToken(account);
        return new PlatformService.LoginResponse(
                token,
                new PlatformService.UserSummary(account.id(), account.fullName(), account.email(), account.role().name()),
                platformService.dashboardFor(account).demoCredentials()
        );
    }
}
