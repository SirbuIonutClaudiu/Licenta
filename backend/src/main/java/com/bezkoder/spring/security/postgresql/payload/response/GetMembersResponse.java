package com.bezkoder.spring.security.postgresql.payload.response;

import com.bezkoder.spring.security.postgresql.models.ImageModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;

@Data
@AllArgsConstructor
public class GetMembersResponse {
    private List<UserResponse> users;
    private List<ImageModel> images;
    private int length;
}
