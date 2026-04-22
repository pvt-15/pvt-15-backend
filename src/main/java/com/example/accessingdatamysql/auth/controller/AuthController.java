package com.example.accessingdatamysql.auth.controller;

import com.example.accessingdatamysql.auth.dto.AuthResponse;
import com.example.accessingdatamysql.auth.dto.GoogleLoginRequest;
import com.example.accessingdatamysql.auth.dto.LoginRequest;
import com.example.accessingdatamysql.auth.dto.RegisterRequest;
import com.example.accessingdatamysql.user.dto.UserResponse;
import com.example.accessingdatamysql.user.mapper.UserMapper;
import com.example.accessingdatamysql.model.User;
import com.example.accessingdatamysql.user.repository.UserRepository;
import com.example.accessingdatamysql.auth.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

/**
 * REST-controller for authentication and identification of the logged-in user
 *
 * <p>The controller uses endpoints under <code>/auth</code> for handling off:</p>
 * <ul>
 *     <li>register new user</li>
 *     <li>login with email/password</li>
 *     <li>login through Google</li>
 *     <li>fetching information of the authenticated user</li>
 * </ul>
 *
 *  The controller is using {@link AuthService} for the authentication logic,
 *  whilst {@link UserRepository} and {@link UserMapper} is used to fetch and
 *  map userdata in the endpoint <code>/auth/me</code>
 *
 * <p>{@code ResponseEntity<?>} used for returns since sometimes return DTO,
 * sometimes error message</p>
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    /**
     * Service which provides logic regarding registration, login and
     * authorization through extern providers such as Google
     */
    private final AuthService authService;

    /**
     * Repository to fetch users from database
     * Is used mainly for endpoint which return current user
     */
    private final UserRepository userRepository;

    /**
     * Mapper which transforms {@link User}-entities to DTOs
     * which are safe to return
     */
    private final UserMapper userMapper;

    /**
     * Creates a new {@link AuthController}
     *
     * @param authService service for authentication logic
     * @param userRepository repository for access to userdata
     * @param userMapper mapper for converting to {@link UserResponse}
     */
    public AuthController(AuthService authService,
                          UserRepository userRepository,
                          UserMapper userMapper) {
        this.authService = authService;
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    /**
     * Register a new user
     *
     * <p>Receives a {@link RegisterRequest}-object from the client and forwards
     * it to {@link AuthService#register(RegisterRequest)}.</p>
     *
     * {@code 201 Created} if successful
     * {@code 400 Bad Request} together with an errormessage, if unsuccessful
     *
     * @param request request-body with registration details (e.g. name,
     *                email, password)
     * @return {@link ResponseEntity} with authentication reply if successful,
     *         otherwise an errormessage
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        try {
            AuthResponse response = authService.register(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Logs in a user
     *
     * <p>Receives a {@link LoginRequest>-object and lets {@link AuthService}
     * validate the credentials. If succesful a {@link AuthResponse}-object
     * is returned with token and userinfo</p>
     *
     * <p>If credentials are invalid, {@code 401 Unauthorized}, together with
     * error message is returned.</p>
     *
     * @param request request-body with login credentials (e.g. name and password)
     * @return {@link ResponseEntity} with authentication reply if successful,
     *         otherwise an errormessage
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            AuthResponse response = authService.login(request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }

    /**
     * Logs in a user, or registers a user through Google-authentication
     *
     * <p>Receives a {@link GoogleLoginRequest}-object, containing a Google-token
     * and delegates verification to {@link AuthService#loginWithGoogle(GoogleLoginRequest)}.</p>
     *
     * <p>If successful, {@link AuthResponse}-object is returned.
     * If Google login can not be verified, {@code 401 Unauthorized} is returned.</p>
     *
     * @param request request-body with Google-token
     * @return {@link ResponseEntity} with authentication reply if successful,
     *         otherwise an errormessage
     */
    @PostMapping("/google")
    public ResponseEntity<?> googleLogin(@RequestBody GoogleLoginRequest request) {
        try {
            AuthResponse response = authService.loginWithGoogle(request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }

    /**
     * Fetches information for the current authenticated user
     *
     * <p>Method uses {@link AuthenticationPrincipal} to receive the users JWT.
     * The JWTs' subject is the users intern ID as a String. The ID is converted to
     * {@link Integer} and is then used to find user in database</p>
     *
     * <p>If user is found, it is mapped to {@link UserResponse}-object and is returned
     * with {@code 200 OK}. If no user is found, {@code 404 Not Found} is returned.
     * If JWT-subject can not be interpreted as Integer, {@code 400 Bad Request} is returned.</p>
     *
     * @param jwt JWT for the authenticated user. Injected automatically by Spring Security
     * @return {@link ResponseEntity} with current user, alternatively
     *         {@code 404 Not Found} or {@code 400 Bad Request}
     */
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@AuthenticationPrincipal Jwt jwt) {
        try {
            Integer userId = Integer.valueOf(jwt.getSubject());

            Optional<User> user = userRepository.findById(userId);

            return user.map(userMapper::toUserResponse).map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.notFound().build());
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
