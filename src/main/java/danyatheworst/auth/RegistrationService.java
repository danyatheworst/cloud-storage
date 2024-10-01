package danyatheworst.auth;

import danyatheworst.auth.dto.RequestSignUpDto;
import danyatheworst.exceptions.EntityAlreadyExistsException;
import danyatheworst.storage.service.FileStorageService;
import danyatheworst.user.Role;
import danyatheworst.user.User;
import danyatheworst.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;

@RequiredArgsConstructor
@Service
public class RegistrationService {
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;
    private final PasswordEncoder passwordEncoder;

    public Long createUser(RequestSignUpDto signUpDto) {
        try {
            String encodedPassword = this.passwordEncoder.encode(signUpDto.getPassword());
            User user = new User(signUpDto.getUsername(), encodedPassword);
            user.setRoles(Set.of(Role.USER));
            this.userRepository.save(user);
            return user.getId();
        } catch (DataIntegrityViolationException e) {
            throw new EntityAlreadyExistsException("That username is taken. Try another");
        }
    }

    public void handleNewUser(RequestSignUpDto signUpDto) {
        Long userId = this.createUser(signUpDto);
        this.fileStorageService.createDirectory("", userId);
    }
}
