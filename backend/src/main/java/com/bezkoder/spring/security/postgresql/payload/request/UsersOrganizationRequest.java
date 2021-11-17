/*
    This file is part of UnitbVoting application.

    UnitbVoting is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    UnitbVoting is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with UnitbVoting. If not, see <https://www.gnu.org/licenses/>.
*/
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