package danyatheworst.storage;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RequestFSODto {
    @NotBlank(message = "name must be provided")
    private String name;

    public void setName(String name) {
        this.name = name != null ? name.trim() : null;
    }
}