package com.creatorhire.service;

import java.util.Set;
import java.util.stream.Collectors;
import com.creatorhire.dto.AuthResponse;
import com.creatorhire.dto.CurrentUserResponse;
import com.creatorhire.dto.LoginRequest;
import com.creatorhire.dto.OtpResponse;
import com.creatorhire.dto.RegisterRequest;
import com.creatorhire.entity.ClientProfile;
import com.creatorhire.entity.CreatorProfile;
import com.creatorhire.entity.Role;
import com.creatorhire.entity.User;
import com.creatorhire.exception.ConflictException;
import com.creatorhire.exception.ForbiddenException;
import com.creatorhire.repository.ClientProfileRepository;
import com.creatorhire.repository.CreatorProfileRepository;
import com.creatorhire.repository.RoleRepository;
import com.creatorhire.repository.UserRepository;
import com.creatorhire.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    /** Roles allowed for public self-registration. ADMIN accounts are created out-of-band. */
    private static final Set<String> SELF_REGISTER_ROLES = Set.of("CLIENT", "CREATOR");

    private final UserRepository users;
    private final RoleRepository roles;
    private final ClientProfileRepository clientProfiles;
    private final CreatorProfileRepository creatorProfiles;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final OtpService otpService;

    public AuthService(
            UserRepository users,
            RoleRepository roles,
            ClientProfileRepository clientProfiles,
            CreatorProfileRepository creatorProfiles,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtService jwtService,
            OtpService otpService) {
        this.users = users;
        this.roles = roles;
        this.clientProfiles = clientProfiles;
        this.creatorProfiles = creatorProfiles;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.otpService = otpService;
    }

    @Transactional
    public OtpResponse register(RegisterRequest request) {
        String roleName = request.role().toUpperCase();
        if (!SELF_REGISTER_ROLES.contains(roleName)) {
            throw new ConflictException("Role must be CLIENT or CREATOR");
        }
        if (users.existsByEmail(request.email())) {
            throw new ConflictException("Email is already registered");
        }
        Role role = roles.findByName(roleName)
                .orElseThrow(() -> new IllegalStateException("Role not seeded: " + roleName));

        User user = new User();
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setEmailVerified(false);
        user.getRoles().add(role);
        users.save(user);

        if ("CLIENT".equals(roleName)) {
            ClientProfile profile = new ClientProfile();
            profile.setUser(user);
            clientProfiles.save(profile);
        } else {
            CreatorProfile profile = new CreatorProfile();
            profile.setUser(user);
            creatorProfiles.save(profile);
        }

        otpService.issueOtp(user);
        return new OtpResponse("Verification code sent to your email", user.getEmail());
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password()));
        User full = users.findByEmail(request.email())
                .orElseThrow(() -> new ForbiddenException("Invalid email or password"));
        if (!full.isEmailVerified()) {
            throw new ForbiddenException("Email not verified. Please verify the code sent to your email");
        }
        UserDetails user = (UserDetails) auth.getPrincipal();
        return new AuthResponse(jwtService.generateToken(user), user.getUsername(), rolesOf(user), true);
    }

    @Transactional(noRollbackFor = {ConflictException.class,
            com.creatorhire.exception.RateLimitException.class})
    public AuthResponse verifyOtp(String email, String code) {
        User user = otpService.verifyOtp(email, code);
        return new AuthResponse(jwtService.generateToken(user), user.getEmail(), rolesOf(user), true);
    }

    @Transactional
    public OtpResponse resendOtp(String email) {
        otpService.resendOtp(email);
        return new OtpResponse("A new verification code was sent to your email", email);
    }

    public CurrentUserResponse me(UserDetails user) {
        User full = users.findByEmail(user.getUsername()).orElseThrow();
        return new CurrentUserResponse(full.getEmail(), full.getFirstName(), full.getLastName(), rolesOf(full));
    }

    private Set<String> rolesOf(UserDetails user) {
        return user.getAuthorities().stream()
                .map(a -> a.getAuthority().replace("ROLE_", ""))
                .collect(Collectors.toSet());
    }
}
