package danyatheworst.auth;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/auth")
public class AuthController {
    private final RegistrationService registrationService;

    @PostMapping("/sign-up")
    public ResponseEntity<Void> signUp(@RequestBody @Valid RequestSignUpDto signUpDto) {
        this.registrationService.register(signUpDto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
