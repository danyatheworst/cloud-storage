package danyatheworst.auth;

import danyatheworst.auth.dto.RequestSignInDto;
import danyatheworst.exceptions.InvalidCredentialsException;
import danyatheworst.exceptions.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class AuthenticationService {

    private final AuthenticationManager authenticationManager;

    public Authentication authenticate(RequestSignInDto signInDto) {
        try {
            Authentication auth = new UsernamePasswordAuthenticationToken(signInDto.getUsername(), signInDto.getPassword());
            return this.authenticationManager.authenticate(auth);
        } catch (BadCredentialsException e) {
            throw new InvalidCredentialsException("Invalid username or password");
        }
    }
}
