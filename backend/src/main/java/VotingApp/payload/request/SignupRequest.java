package VotingApp.payload.request;

import lombok.Data;

import java.util.Set;

import javax.validation.constraints.*;

@Data
public class SignupRequest {
    @NotBlank
    @Size(min = 3, max = 20)
    private String name;

    @NotBlank
    private String institutionalCode;
 
    @NotBlank
    @Size(max = 50)
    @Email
    private String email;

    @NotBlank
    private String address;

    @NotBlank
    private String applicationDate;
    
    private Set<String> role;
    
    @NotBlank
    @Size(min = 6, max = 40)
    private String password;

    private boolean verifiedApplication;

    private boolean verifiedEmail;

    private boolean activated2FA;
}
