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

Copyright 2020-2021 Sirbu Ionut Claudiu
*/
package com.bezkoder.spring.security.postgresql.security.services;

import com.bezkoder.spring.security.postgresql.models.ERole;
import com.bezkoder.spring.security.postgresql.models.ImageModel;
import com.bezkoder.spring.security.postgresql.models.Role;
import com.bezkoder.spring.security.postgresql.models.membruSenat;
import com.bezkoder.spring.security.postgresql.payload.request.UsersOrganizationRequest;
import com.bezkoder.spring.security.postgresql.payload.response.GetMembersResponse;
import com.bezkoder.spring.security.postgresql.payload.response.UserResponse;
import com.bezkoder.spring.security.postgresql.repository.ImageRepository;
import com.bezkoder.spring.security.postgresql.repository.RoleRepository;
import com.bezkoder.spring.security.postgresql.repository.membruSenatRepository;
import com.bezkoder.spring.security.postgresql.security.exceptions.UserNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import javax.transaction.Transactional;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

@Service
@Transactional
public class MembruSenatService {
    @Autowired
    private ImageRepository imageRepository;

    @Autowired
    private RoleRepository roleRepository;

    private final membruSenatRepository membruRepo;

    @Autowired
    public MembruSenatService(membruSenatRepository membruRepo) {
        this.membruRepo = membruRepo;
    }

    public membruSenat findMemberById(Long id) {
        return membruRepo.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Member by id " + id + " was not found"));
    }

    public membruSenat findMemberByEmail(String email) {
        return membruRepo.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("Member by email " + email + " was not found"));
    }

    public void deleteMemberById(Long id){
        membruRepo.deleteById(id);
    }

    public List<membruSenat> returnAll() {
        return membruRepo.findAll(Sort.by(Sort.Direction.ASC, "name"));
    }

    public GetMembersResponse SortUsersByRequest(UsersOrganizationRequest usersOrganizationRequest) {
        List<UserResponse> result;
        result = getAllUserResponses();
        sortByActivatedAccount(result, usersOrganizationRequest.isFilterByActivatedAccount(), usersOrganizationRequest.isActivatedAccount());
        sortByDisabledAccount(result, usersOrganizationRequest.isFilterByDisabledAccount(), usersOrganizationRequest.isDisabledAccount());
        sortByActivatedEmail(result, usersOrganizationRequest.isFilterByActivatedEmail(), usersOrganizationRequest.isActivatedEmail());
        sortUsersBy(result, usersOrganizationRequest.getSortParameter(), usersOrganizationRequest.getSortDirection());
        int resultLength = result.size();
        result = usersPerPage(result, usersOrganizationRequest.getPage(), usersOrganizationRequest.getPerPage());
        if(!usersOrganizationRequest.getERoles().isEmpty()) {
            filterByRoles(result, getRolesFromERoles(usersOrganizationRequest.getERoles()));
        }
        List<ImageModel> usersImages = getUsersImages(result);
        return new GetMembersResponse(result, usersImages, resultLength);
    }

    private boolean imageBelongsToAUser(List<UserResponse> allUsers, ImageModel image) {
        AtomicBoolean belongs = new AtomicBoolean(false);
        allUsers.forEach(user -> {
            if(user.getEmail().equals(image.getName())) {
                belongs.set(true);
            }
        });
        return belongs.get();
    }

    private List<ImageModel> getUsersImages(List<UserResponse> allUsers) {
        List<ImageModel> retrievedImages = new ArrayList<>();
        this.imageRepository.findAll().forEach(image -> {
            if(imageBelongsToAUser(allUsers, image)) {
                retrievedImages.add(new ImageModel(image.getName(), image.getType(),
                        decompressBytes(image.getPicByte())));
            }
        });
        return retrievedImages;
    }

    private static byte[] decompressBytes(byte[] data) {
        Inflater inflater = new Inflater();
        inflater.setInput(data);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length);
        byte[] buffer = new byte[1024];
        try {
            while (!inflater.finished()) {
                int count = inflater.inflate(buffer);
                outputStream.write(buffer, 0, count);
            }
            outputStream.close();
        } catch (IOException | DataFormatException ignored) {
        }
        return outputStream.toByteArray();
    }

    private List<Role> getRolesFromERoles(List<ERole> eroles) {
        List<Role> result = new ArrayList<>();
        eroles.forEach(eRole -> roleRepository.findByName(eRole).ifPresent(result::add));
        return result;
    }

    private List<UserResponse> sortUsersBy(List<UserResponse> allUsers, String sortParameter, String sortDirection) {
        if(sortParameter.equals("application")) {
            if(sortDirection.equals("asc")) {
                allUsers.sort(Comparator.comparing(UserResponse::getDateOfApplication).thenComparing(UserResponse::getName));
            }
            else {
                allUsers.sort(Comparator.comparing(UserResponse::getDateOfApplication).reversed().thenComparing(UserResponse::getName));
            }
        }
        else {
            if(sortDirection.equals("asc")) {
                allUsers.sort(Comparator.comparing(UserResponse::getName, String.CASE_INSENSITIVE_ORDER));
            }
            else {
                allUsers.sort(Comparator.comparing(UserResponse::getName, String.CASE_INSENSITIVE_ORDER).reversed());
            }
        }
        return allUsers;
    }

    private List<UserResponse> usersPerPage(List<UserResponse> allUsers, int page, int perPage) {
        return allUsers.subList(perPage*page, Math.min(allUsers.size(), (perPage * (page + 1))));
    }

    private List<UserResponse> filterByRoles(List<UserResponse> allUsers, List<Role> roles) {
        allUsers.removeIf(currentResponse -> (Collections.disjoint(userResponseRoles(currentResponse.getId()), roles)));
        return  allUsers;
    }

    private List<Role> userResponseRoles(Long id) {
        membruSenat member = membruRepo.findById(id).orElse(null);
        assert member != null;
        return new ArrayList<>(member.getRoles());
    }

    private List<UserResponse> sortByActivatedEmail(List<UserResponse> allUsers, boolean filterByActivatedEmail, boolean activatedEmail) {
        allUsers.removeIf(currentResponse -> filterByActivatedEmail && (activatedEmail && !currentResponse.isVerifiedEmail()
                || (!activatedEmail && currentResponse.isVerifiedEmail())));
        return allUsers;
    }

    private List<UserResponse> sortByActivatedAccount(List<UserResponse> allUsers, boolean filterByActivatedAccount, boolean activatedAccount) {
        allUsers.removeIf(currentResponse -> filterByActivatedAccount && (activatedAccount && !currentResponse.isVerifiedApplication()
                || (!activatedAccount && currentResponse.isVerifiedApplication())));
                return allUsers;
    }

    private List<UserResponse> sortByDisabledAccount(List<UserResponse> allUsers, boolean filterByDisabledAccount, boolean disabledAccount) {
        allUsers.removeIf(currentResponse -> filterByDisabledAccount && (disabledAccount && !currentResponse.isDisabled()
                || (!disabledAccount && currentResponse.isDisabled())));
        return allUsers;
    }

    private List<UserResponse> getAllUserResponses() {
        List<UserResponse> result = new ArrayList<>();
        membruRepo.findAll().forEach((member) -> result.add(memberToUserResponse(member)));
        return result;
    }

    private String ERoleToString(ERole erole) {
        switch(erole) {
            case ROLE_ADMIN:
                return "Administrator";
            case ROLE_MODERATOR:
                return "Moderator";
            case ROLE_USER:
                return "Utilizator";
            case ROLE_DIDACTIC:
                return "Comisia didactica";
            case ROLE_STIINTIFIC:
                return "Comisia stiintifica";
            case ROLE_CALITATE:
                return "Comisia de asigurare a calitatii si relatii internationale";
            case ROLE_DREPTURI:
                return "Comisia privind drepturile si obligatiile studentilor";
            case ROLE_BUGET:
                return "Comisia de buget–finante";
            case ROLE_JURIDIC:
                return "Comisia juridica";
            default:
                return "NO_ROLE";
        }
    }

    public UserResponse memberToUserResponse(membruSenat member) {
        Set<Role> userRoles = member.getRoles();
        Iterator<Role> it = userRoles.iterator();
        List<String> roles = new ArrayList<>();
        while(it.hasNext()) {
            ERole roleName = it.next().getName();
            roles.add(ERoleToString(roleName));
        }
        roles.sort(Comparator.comparingInt(String::length));
        return new UserResponse( member.getId(),
                member.getEmail(),
                member.getName(),
                member.getAddress(),
                member.getInstitutionalCode(),
                member.getApplicationDate(),
                member.getLoginLocation(),
                member.getWebsite(),
                member.getLandline(),
                member.getPhoneNumber(),
                member.isDisabled(),
                member.isVerifiedApplication(),
                member.isVerifiedEmail(),
                member.isActivated2FA(),
                roles );
    }
}