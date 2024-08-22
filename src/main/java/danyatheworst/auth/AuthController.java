package danyatheworst.auth;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/auth")
public class AuthController {
    private final RegistrationService registrationService;

    @GetMapping("/sign-up")
    public ResponseEntity<Void> signUp(@RequestBody @Valid RequestSignUpDto signUpDto) {
        this.registrationService.register(signUpDto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
