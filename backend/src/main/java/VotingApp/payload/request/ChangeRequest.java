package VotingApp.payload.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class ChangeRequest {
    @NotBlank
    private String code;

    @NotBlank
    private String password;
}
