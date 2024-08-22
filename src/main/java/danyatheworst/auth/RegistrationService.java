package danyatheworst.auth;

import danyatheworst.exceptions.EntityAlreadyExistsException;
import danyatheworst.user.User;
import danyatheworst.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class RegistrationService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public void register(RequestSignUpDto signUpDto) {
        try {
            String encodedPass = this.passwordEncoder.encode(signUpDto.getPassword());
            User user = new User(signUpDto.getLogin(), encodedPass);
            this.userRepository.save(user);

        } catch (DataIntegrityViolationException e) {
            if (e.getCause() instanceof ConstraintViolationException) {
                throw new EntityAlreadyExistsException("That username is taken. Try another");
            }
        }
    }
}
