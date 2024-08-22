package danyatheworst.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class RequestSignUpDto {

    @NotBlank(message = "Login should be between 3 and 50 characters")
    @Size(min = 2, max = 50, message = "Login should be between 3 and 50 characters")
    private String login;

    @NotBlank(message = "Password should be between 6 and 50 characters")
    @Size(min = 6, max = 50, message = "Password should be between 3 and 50 characters")
    private String password;

    public void setLogin(String login) {
        this.login = login.trim();
    }

    public void setPassword(String password) {
        this.password = password.trim();
    }
}
