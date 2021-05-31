package VotingApp.payload.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class ResetRequest {
    @NotBlank
    private String email;
}
