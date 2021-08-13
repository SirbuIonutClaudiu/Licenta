package com.bezkoder.spring.security.postgresql.payload.request;

import com.bezkoder.spring.security.postgresql.models.ERole;
import lombok.Data;

import java.util.List;

@Data
public class UsersOrganizationRequest {
    private int page;
    private int perPage;
    private String sortParameter;
    private String sortDirection;
    private boolean filterByActivatedEmail;
    private boolean activatedEmail;
    private boolean filterByActivatedAccount;
    private boolean activatedAccount;
    private boolean filterByDisabledAccount;
    private boolean disabledAccount;
    private List<ERole> eRoles;

    public UsersOrganizationRequest(int page, int perPage, String sortParameter, String sortDirection, boolean filterByActivatedEmail,
                                    boolean activatedEmail, boolean filterByActivatedAccount, boolean activatedAccount,
                                    boolean filterByDisabledAccount, boolean disabledAccount, List<ERole> eRoles) {
        this.page = page;
        this.perPage = perPage;
        this.sortParameter = sortParameter;
        this.sortDirection = sortDirection;
        this.filterByActivatedEmail = filterByActivatedEmail;
        this.activatedEmail = activatedEmail;
        this.filterByActivatedAccount = filterByActivatedAccount;
        this.activatedAccount = activatedAccount;
        this.filterByDisabledAccount = filterByDisabledAccount;
        this.disabledAccount = disabledAccount;
        this.eRoles = eRoles;
    }
}