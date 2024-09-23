package danyatheworst.auth.dto;

import danyatheworst.user.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
public class UserDto {
    private final Long id;
    private final String username;
    private final Set<Role> roles;
}
