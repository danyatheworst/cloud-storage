package danyatheworst.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class RequestSignInDto {

    @NotBlank(message = "Invalid username or password")
    @Size(min = 2, max = 50, message = "Invalid username or password")
    private final String username;

    @Size(min = 6, max = 50, message = "Invalid username or password")
    private final String password;

    public RequestSignInDto(String username, String password) {
        this.username = username.trim();
        this.password = password;
    }
}
