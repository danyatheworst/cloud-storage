package danyatheworst.auth;

import danyatheworst.auth.dto.RequestSignInDto;
import danyatheworst.auth.dto.RequestSignUpDto;
import danyatheworst.auth.dto.UserDto;
import danyatheworst.common.ErrorResponseDto;
import danyatheworst.exceptions.InvalidCredentialsException;
import danyatheworst.exceptions.UnauthorizedException;
import danyatheworst.user.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/auth")
public class AuthController {
    private final RegistrationService registrationService;
    private final AuthenticationService authenticationService;

    @GetMapping("/me")
    public ResponseEntity<UserDto> me(@AuthenticationPrincipal User user) {
        if (user == null) {
            throw new UnauthorizedException("User is not logged in");
        }
        UserDto userDto = new UserDto(user.getId(), user.getUsername(), user.getRoles());
        return ResponseEntity.status(HttpStatus.OK).body(userDto);
    }

    @PostMapping("/sign-up")
    public ResponseEntity<Void> signUp(@RequestBody @Valid RequestSignUpDto signUpDto) {
        this.registrationService.handleNewUser(signUpDto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/sign-in")
    public ResponseEntity<Void> signIn(
            @RequestBody @Valid RequestSignInDto signInDto,
            BindingResult bindingResult,
            HttpServletRequest request) {

        if (bindingResult.hasErrors()) {
            throw new InvalidCredentialsException("Invalid username or password");
        }
        SecurityContextHolder.getContext().setAuthentication(this.authenticationService.authenticate(signInDto));
        HttpSession session = request.getSession(true);
        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                SecurityContextHolder.getContext());

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponseDto> handleUnauthorizedException(UnauthorizedException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                new ErrorResponseDto(e.getMessage()));
    }
}