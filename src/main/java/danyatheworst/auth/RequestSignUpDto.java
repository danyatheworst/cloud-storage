package danyatheworst.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class RequestSignUpDto {

    @NotBlank(message = "Login should be between 3 and 50 characters")
    @Size(min = 2, max = 50, message = "Login should be between 3 and 50 characters")
    private final String login;

    @Size(min = 6, max = 50, message = "Password should be between 6 and 50 characters")
    private final String password;

    public RequestSignUpDto(String login, String password) {
        this.login = login.trim();
        this.password = password;
    }
}
