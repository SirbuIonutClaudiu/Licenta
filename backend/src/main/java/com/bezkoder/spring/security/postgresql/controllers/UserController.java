package com.bezkoder.spring.security.postgresql.controllers;

import com.bezkoder.spring.security.postgresql.awsecrets.TwilioSecrets;
import com.bezkoder.spring.security.postgresql.models.*;
import com.bezkoder.spring.security.postgresql.payload.request.NewPasswordRequest;
import com.bezkoder.spring.security.postgresql.payload.request.PasswordRequest;
import com.bezkoder.spring.security.postgresql.payload.request.UsersOrganizationRequest;
import com.bezkoder.spring.security.postgresql.payload.request.WebsiteRequest;
import com.bezkoder.spring.security.postgresql.payload.response.GetMembersResponse;
import com.bezkoder.spring.security.postgresql.payload.response.MemberNamesSearchResponse;
import com.bezkoder.spring.security.postgresql.payload.response.MessageResponse;
import com.bezkoder.spring.security.postgresql.payload.response.UserResponse;
import com.bezkoder.spring.security.postgresql.repository.ImageRepository;
import com.bezkoder.spring.security.postgresql.repository.PasswordResetTokenRepository;
import com.bezkoder.spring.security.postgresql.repository.RoleRepository;
import com.bezkoder.spring.security.postgresql.repository.membruSenatRepository;
import com.bezkoder.spring.security.postgresql.security.jwt.JwtUtils;
import com.bezkoder.spring.security.postgresql.security.services.MembruSenatService;
import com.twilio.rest.verify.v2.Service;
import com.twilio.rest.verify.v2.service.Verification;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.twilio.Twilio;
import com.twilio.rest.verify.v2.service.VerificationCheck;
import javax.validation.Valid;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

@AllArgsConstructor
@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    ImageRepository imageRepository;

    @Autowired
    private final MembruSenatService membruSenatService;

    @Autowired
    membruSenatRepository membruSenatRepo;

    @Autowired
    PasswordResetTokenRepository PRTrepository;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    JwtUtils jwtUtils;

    @GetMapping("/get_member_names")
    public ResponseEntity<List<MemberNamesSearchResponse>> getMemberNames(@RequestHeader("Authorization") String auth) {
        membruSenat adminMember = getMemberFromAuthentication(auth);
        Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
                .orElseThrow(() -> new RuntimeException("Role Admin does not exist !"));
        Role modRole = roleRepository.findByName(ERole.ROLE_MODERATOR)
                .orElseThrow(() -> new RuntimeException("Role Moderator does not exist !"));
        if(!adminMember.getRoles().contains(adminRole) && !adminMember.getRoles().contains(modRole)) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
        List<MemberNamesSearchResponse> membersList = new ArrayList<>();
        membruSenatService.returnAll().forEach(member -> membersList.add(new MemberNamesSearchResponse(member.getId(), member.getName())));
        return new ResponseEntity<>(membersList, HttpStatus.OK);
    }

    @PostMapping("/get_members")
    public ResponseEntity<GetMembersResponse> getMembers(@RequestHeader("Authorization") String auth,
                                                         @Valid @RequestBody UsersOrganizationRequest usersOrganizationRequest) {
        membruSenat adminMember = getMemberFromAuthentication(auth);
        Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
                .orElseThrow(() -> new RuntimeException("Role Admin does not exist !"));
        Role modRole = roleRepository.findByName(ERole.ROLE_MODERATOR)
                .orElseThrow(() -> new RuntimeException("Role Moderator does not exist !"));
        if(!adminMember.getRoles().contains(adminRole) && !adminMember.getRoles().contains(modRole)) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(membruSenatService.SortUsersByRequest(usersOrganizationRequest), HttpStatus.OK);
    }

    @PostMapping("/sendPhoneVerification/{id}/{number}")
    public ResponseEntity<?> sendPhoneVerification(@RequestHeader("Authorization") String auth,
                                                   @PathVariable("id") Long id, @PathVariable("number") String number) {
        membruSenat adminMember = getMemberFromAuthentication(auth);
        membruSenat memberToBeModified = membruSenatRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Member to be modified does not exist !"));
        if(!adminMember.getId().equals(memberToBeModified.getId())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Only the owner of the account can request phone verification !"));
        }
        Twilio.init(new TwilioSecrets("TwilioAccountSID").getSecret(),
                new TwilioSecrets("TwilioAuthToken").getSecret());
        if(memberToBeModified.getVerificationSID() != null) {
            Service.deleter(memberToBeModified.getVerificationSID()).delete();
        }
        Service service = Service.creator("UnitbVoting").create();
        Verification verification = Verification.creator(
                service.getSid(),
                number,
                "sms")
                .create();
        memberToBeModified.setVerificationSID(service.getSid());
        memberToBeModified.setPhoneNumber2BVerified(number);
        membruSenatRepo.save(memberToBeModified);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/confirmPhone/{id}/{code}")
    public ResponseEntity<?> confirmPhone(@RequestHeader("Authorization") String auth,
                                          @PathVariable("id") Long id, @PathVariable("code") String code) {
        membruSenat adminMember = getMemberFromAuthentication(auth);
        membruSenat memberToBeModified = membruSenatRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Member to be modified does not exist !"));
        if(!adminMember.getId().equals(memberToBeModified.getId())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Only the owner of the account can confirm phone number !"));
        }
        if(memberToBeModified.getVerificationSID() == null) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Try sending another verification request !"));
        }
        Twilio.init(new TwilioSecrets("TwilioAccountSID").getSecret(),
                new TwilioSecrets("TwilioAuthToken").getSecret());
        try {
            VerificationCheck verificationCheck = VerificationCheck.creator(
                    memberToBeModified.getVerificationSID(),
                    code)
                    .setTo(memberToBeModified.getPhoneNumber2BVerified()).create();
            if(verificationCheck.getStatus().toLowerCase(Locale.ROOT).compareToIgnoreCase("approved") == 0) {
                memberToBeModified.setVerificationSID(null);
                memberToBeModified.setPhoneNumber(memberToBeModified.getPhoneNumber2BVerified());
                memberToBeModified.setPhoneNumber2BVerified(null);
                membruSenatRepo.save(memberToBeModified);
            }
            else {
                return ResponseEntity
                        .badRequest()
                        .body(new MessageResponse("Wrong code.Try again !"));
            }
        } catch(Exception e) {
            Service.deleter(memberToBeModified.getVerificationSID()).delete();
            memberToBeModified.setVerificationSID(null);
            memberToBeModified.setPhoneNumber2BVerified(null);
            membruSenatRepo.save(memberToBeModified);
            return ResponseEntity
                    .badRequest()
                    .body("Code request expired.Try sending another one !");
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/moderator_role/{id}/{assignation}")
    public ResponseEntity<?> ModeratorRole(@RequestHeader("Authorization") String auth,
                                           @PathVariable("id") Long id, @PathVariable("assignation") boolean assignation) {
        membruSenat adminMember = getMemberFromAuthentication(auth);
        membruSenat memberToBeModified = membruSenatRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Member to be modified does not exist !"));
        Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
                .orElseThrow(() -> new RuntimeException("Role Admin does not exist !"));
        Role modRole = roleRepository.findByName(ERole.ROLE_MODERATOR)
                .orElseThrow(() -> new RuntimeException("Role Moderator does not exist !"));
        Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                .orElseThrow(() -> new RuntimeException("Role Moderator does not exist !"));
        if(!adminMember.getRoles().contains(adminRole)) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Administrator role is required to perform this action !"));
        }
        if(memberToBeModified.getRoles().contains(adminRole)) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse(
                            assignation ? "Cannot assign Moderator role to an Administrator !" :
                            "Cannot deassign Moderator role from an Administrator"));
        }
        if(memberToBeModified.getRoles().contains(modRole) && assignation) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("User to be promoted is already a Moderator !"));
        }
        if(memberToBeModified.getRoles().contains(userRole) && !assignation) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Cannot deassign Moderator role from a User !"));
        }
        memberToBeModified.getRoles().remove(assignation ? userRole : modRole);
        memberToBeModified.getRoles().add(assignation ? modRole : userRole);
        membruSenatRepo.save(memberToBeModified);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    private membruSenat getMemberFromAuthentication(String auth) {
        String token = auth.substring(7);
        String email = jwtUtils.getEmailFromJwtToken(token);
        return membruSenatService.findMemberByEmail(email);
    }

    @PostMapping("/update_commission_role/{id}/{role}")
    public ResponseEntity<?> updateRoles(@RequestHeader("Authorization") String auth,
                                         @PathVariable("id") Long id, @PathVariable("role") ERole role) {
        membruSenat adminMember = getMemberFromAuthentication(auth);
        membruSenat memberToBeModified = membruSenatRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Member to be modified does not exist !"));
        Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
                .orElseThrow(() -> new RuntimeException("Role Admin does not exist !"));
        Role modRole = roleRepository.findByName(ERole.ROLE_MODERATOR)
                .orElseThrow(() -> new RuntimeException("Role Moderator does not exist !"));
        if(!adminMember.getRoles().contains(adminRole) && !adminMember.getRoles().contains(modRole)) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Administrator or Moderator role is required to perform this action !"));
        }
        if(adminMember.getId().equals(memberToBeModified.getId())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Cannot set role on own account !"));
        }
        if(adminMember.getRoles().contains(adminRole)) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Administrator cannot be part of a commission !"));
        }
        Role newRole = roleRepository.findByName(role)
                .orElseThrow(() -> new RuntimeException("Role " + role.name() + " not found !"));
        if(memberToBeModified.getRoles().size() == 2) {
            memberToBeModified.getRoles().removeIf(currentRole -> (!currentRole.getName().equals(ERole.ROLE_ADMIN)) &&
                    (!currentRole.getName().equals(ERole.ROLE_MODERATOR)) &&
                    (!currentRole.getName().equals(ERole.ROLE_USER)) );
        }
        if(!newRole.getName().equals(ERole.ROLE_DELETE)) {
            memberToBeModified.getRoles().add(newRole);
        }
        membruSenatRepo.save(memberToBeModified);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/update_address/{id}/{address}")
    public ResponseEntity<?> updateAddress(@RequestHeader("Authorization") String auth,
                                           @PathVariable("id") Long id, @PathVariable("address") String address) {
        membruSenat adminMember = getMemberFromAuthentication(auth);
        Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
                .orElseThrow(() -> new RuntimeException("Role Admin does not exist !"));
        Role modRole = roleRepository.findByName(ERole.ROLE_MODERATOR)
                .orElseThrow(() -> new RuntimeException("Role Moderator does not exist !"));
        membruSenat memberToBeModified = membruSenatRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Member to be modified does not exist !"));
        boolean requestIsFromTheOwner = adminMember.getId().equals(memberToBeModified.getId());
        boolean requestIsFromAdminOrMod = adminMember.getRoles().contains(adminRole) || adminMember.getRoles().contains(modRole);
        if(!requestIsFromTheOwner && !requestIsFromAdminOrMod) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Only the owner of the account, the Admin or a Moderator can change the address of residence of the account !"));
        }
        memberToBeModified.setAddress(address);
        membruSenatRepo.save(memberToBeModified);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/update_name/{id}/{name}")
    public ResponseEntity<?> updateName(@RequestHeader("Authorization") String auth,
                                        @PathVariable("id") Long id, @PathVariable("name") String name) {
        membruSenat adminMember = getMemberFromAuthentication(auth);
        Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
                .orElseThrow(() -> new RuntimeException("Role Admin does not exist !"));
        Role modRole = roleRepository.findByName(ERole.ROLE_MODERATOR)
                .orElseThrow(() -> new RuntimeException("Role Moderator does not exist !"));
        membruSenat memberToBeModified = membruSenatRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Member to be modified does not exist !"));
        if(!adminMember.getRoles().contains(adminRole) && !adminMember.getRoles().contains(modRole)) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Only the Admin or a Moderator can change the name of the account !"));
        }
        memberToBeModified.setName(name);
        membruSenatRepo.save(memberToBeModified);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/update_landline/{id}/{landline}")
    public ResponseEntity<?> updateLandline(@RequestHeader("Authorization") String auth,
                                            @PathVariable("id") Long id, @PathVariable("landline") String landline) {
        membruSenat adminMember = getMemberFromAuthentication(auth);
        membruSenat memberToBeModified = membruSenatRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Member to be modified does not exist !"));
        if(!adminMember.getId().equals(memberToBeModified.getId())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Only the owner of the account can change the landline !"));
        }
        memberToBeModified.setLandline(landline);
        membruSenatRepo.save(memberToBeModified);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/update_email/{id}/{email}")
    public ResponseEntity<?> updateEmail(@RequestHeader("Authorization") String auth,
                                         @PathVariable("id") Long id, @PathVariable("email") String email) {
        membruSenat adminMember = getMemberFromAuthentication(auth);
        membruSenat memberToBeModified = membruSenatRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Member to be modified does not exist !"));
        if(!adminMember.getId().equals(memberToBeModified.getId())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Only the owner of the account can change the email !"));
        }
        ImageModel image = imageRepository.findByName(memberToBeModified.getEmail())
                .orElseThrow(() -> new RuntimeException("Email unavailable!"));
        image.setName(email);
        memberToBeModified.setEmail(email);
        membruSenatRepo.save(memberToBeModified);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/deny_application/{id}")
    public ResponseEntity<?> denyApplication(@PathVariable("id") Long id) {
        if(!membruSenatRepo.existsById(id)) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Member does not exist !"));
        }
        membruSenat member = membruSenatService.findMemberById(id);
        ImageModel imageModel = imageRepository.findByName(member.getEmail())
                .orElseThrow(() -> new RuntimeException("Image not found !"));
        imageRepository.deleteById(imageModel.getId());
        if(PRTrepository.existsByMemberId(member.getId())) {
            PasswordResetToken passwordResetToken = PRTrepository.findByMemberId(member.getId());
            PRTrepository.deleteById(passwordResetToken.getId());
        }
        membruSenatService.deleteMemberById(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/accept_application/{id}")
    public ResponseEntity<?> acceptApplication(@PathVariable("id") Long id) {
        membruSenat member = membruSenatService.findMemberById(id);
        member.setVerifiedApplication(true);
        membruSenatRepo.save(member);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/change_website")
    public ResponseEntity<?> acceptApplication(@RequestHeader("Authorization") String auth,
                                               @Valid @RequestBody WebsiteRequest websiteRequest) throws IOException {
        membruSenat adminMember = getMemberFromAuthentication(auth);
        membruSenat memberToBeModified = membruSenatRepo.findById(websiteRequest.getId())
                .orElseThrow(() -> new RuntimeException("Member to be modified does not exist !"));
        if(!adminMember.getId().equals(memberToBeModified.getId())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Only the owner of the account can change the website !"));
        }
        URL url = new URL(websiteRequest.getWebsite());
        HttpURLConnection huc = (HttpURLConnection) url.openConnection();
        int responseCode = huc.getResponseCode();
        if(responseCode == HttpURLConnection.HTTP_NOT_FOUND) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Site URL is unavailable.Try another !"));
        }
        memberToBeModified.setWebsite(websiteRequest.getWebsite());
        membruSenatRepo.save(memberToBeModified);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/toggle_2FA/{id}")
    public ResponseEntity<?> toggle2FA(@RequestHeader("Authorization") String auth, @PathVariable("id") Long id) {
        membruSenat adminMember = getMemberFromAuthentication(auth);
        membruSenat memberToBeModified = membruSenatRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Member to be modified does not exist !"));
        if(!adminMember.getId().equals(memberToBeModified.getId())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Only the owner of the account can toggle 2FA !"));
        }
        boolean twoFactor = memberToBeModified.isActivated2FA();
        memberToBeModified.setActivated2FA(!twoFactor);
        membruSenatRepo.save(memberToBeModified);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/sleep/{seconds}")
    public ResponseEntity<?> Sleep(@PathVariable("seconds") int seconds) throws InterruptedException {
        TimeUnit.SECONDS.sleep(seconds);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/find/{id}")
    public ResponseEntity<UserResponse> getMemberById(@PathVariable("id") Long id) {
        membruSenat member = membruSenatService.findMemberById(id);
        UserResponse userResult = membruSenatService.memberToUserResponse(member);
        return new ResponseEntity<>(userResult, HttpStatus.OK);
    }

    @GetMapping("/check2FA/{email}")
    public boolean check2FA(@PathVariable("email") String email) {
        membruSenat member = membruSenatService.findMemberByEmail(email);
        return member.isActivated2FA();
    }

    @PostMapping("/delete_user/{id}")
    public ResponseEntity<?> deleteMember(@RequestHeader("Authorization") String auth, @PathVariable("id") Long id) {
        membruSenat adminMember = getMemberFromAuthentication(auth);
        membruSenat memberToBeDeleted = membruSenatRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Member to be modified does not exist !"));
        Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
                .orElseThrow(() -> new RuntimeException("Role Admin does not exist !"));
        Role modRole = roleRepository.findByName(ERole.ROLE_MODERATOR)
                .orElseThrow(() -> new RuntimeException("Role Moderator does not exist !"));
        if(!(adminMember.getRoles().contains(adminRole) ||adminMember.getRoles().contains(modRole))) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Administrator or Moderator role is required to perform this action !"));
        }
        if(memberToBeDeleted.getRoles().contains(adminRole)) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Cannot delete an Administrator account !"));
        }
        if(memberToBeDeleted.getRoles().contains(modRole) && !adminMember.getRoles().contains(adminRole)) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Moderator accounts can only be deleted by the Administrator !"));
        }
        ImageModel imageModel = imageRepository.findByName(memberToBeDeleted.getEmail())
                .orElseThrow(() -> new RuntimeException("Image not found !"));
        imageRepository.deleteById(imageModel.getId());
        if(PRTrepository.existsByMemberId(memberToBeDeleted.getId())) {
            PasswordResetToken passwordResetToken = PRTrepository.findByMemberId(memberToBeDeleted.getId());
            PRTrepository.deleteById(passwordResetToken.getId());
        }
        membruSenatService.deleteMemberById(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/disable_reinstate_user/{id}/{disable}")
    public ResponseEntity<?> disableMember(@RequestHeader("Authorization") String auth,
                                           @PathVariable("id") Long id, @PathVariable("disable") boolean disable) {
        membruSenat adminMember = getMemberFromAuthentication(auth);
        membruSenat memberToBeDisabled = membruSenatRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Member to be modified does not exist !"));
        Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
                .orElseThrow(() -> new RuntimeException("Role Admin does not exist !"));
        Role modRole = roleRepository.findByName(ERole.ROLE_MODERATOR)
                .orElseThrow(() -> new RuntimeException("Role Moderator does not exist !"));
        String action = disable ? "disable" : "enable";
        if(!(adminMember.getRoles().contains(adminRole) ||adminMember.getRoles().contains(modRole))) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Administrator or Moderator role is required to perform this action !"));
        }
        if(memberToBeDisabled.getRoles().contains(adminRole)) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Cannot " + action + " an Administrator account !"));
        }
        if(memberToBeDisabled.getRoles().contains(modRole) && !adminMember.getRoles().contains(adminRole)) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Moderator accounts can only be " + action + "d by the Administrator !"));
        }
        memberToBeDisabled.setDisabled(disable);
        membruSenatRepo.save(memberToBeDisabled);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/check_password")
    public ResponseEntity<?> checkPassword(@RequestHeader("Authorization") String auth,
                                           @Valid @RequestBody PasswordRequest passwordRequest) {
        membruSenat adminMember = getMemberFromAuthentication(auth);
        membruSenat memberToBeModified = membruSenatRepo.findById(passwordRequest.getId())
                .orElseThrow(() -> new RuntimeException("Member to be modified does not exist !"));
        if(!adminMember.getId().equals(memberToBeModified.getId())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Only the owner of the account can check the password validity !"));
        }
        BCryptPasswordEncoder bEn = new BCryptPasswordEncoder();
        if(bEn.matches(passwordRequest.getPassword(), memberToBeModified.getPassword())) {
            return new ResponseEntity<>(HttpStatus.OK);
        }
        else {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Wrong password !"));
        }
    }

    @PostMapping("/change_password")
    public ResponseEntity<?> changePassword(@RequestHeader("Authorization") String auth,
                                            @Valid @RequestBody NewPasswordRequest newPasswordRequest) {
        membruSenat adminMember = getMemberFromAuthentication(auth);
        membruSenat memberToBeModified = membruSenatRepo.findById(newPasswordRequest.getId())
                .orElseThrow(() -> new RuntimeException("Member to be modified does not exist !"));
        if(!adminMember.getId().equals(memberToBeModified.getId())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Only the owner of the account can change the password!"));
        }
        memberToBeModified.setPassword(encoder.encode(newPasswordRequest.getNewPassword()));
        membruSenatRepo.save(memberToBeModified);
        return new ResponseEntity<>(HttpStatus.OK);
    }

	@PostMapping("/upload")
    public ResponseEntity<?> uploadImage(@RequestParam("imageFile") MultipartFile file, @RequestParam("memberEmail") String email)
            throws IOException {
        if (file.getBytes().length > 2100000) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("File is larger than 2MB !"));
        }
        if(imageRepository.existsByName(email)) {
            ImageModel img2Bdeleted = imageRepository.findByName(email)
                    .orElseThrow(() -> new RuntimeException("Image file error!"));
            imageRepository.delete(img2Bdeleted);
        }
        ImageModel img = new ImageModel(email, file.getContentType(),
                compressBytes(file.getBytes()));
        imageRepository.save(img);
        return ResponseEntity.ok(new MessageResponse("Image uploaded successfully!"));
    }

    @GetMapping(path = { "/get_images" })
    public List<ImageModel> getAllImages() {
        List<ImageModel> retrievedImages = new ArrayList<>();
        imageRepository.findAll().forEach((image) -> retrievedImages.add(new ImageModel(image.getName(), image.getType(),
                decompressBytes(image.getPicByte()))));
        return retrievedImages;
    }

    @GetMapping(path = { "/get/{imageName}" })
    public ImageModel getImage(@PathVariable("imageName") String imageName) {
        final Optional<ImageModel> retrievedImage = imageRepository.findByName(imageName);
        return new ImageModel(retrievedImage.get().getName(), retrievedImage.get().getType(),
                decompressBytes(retrievedImage.get().getPicByte()));
    }

    private static byte[] compressBytes(byte[] data) {
        Deflater deflater = new Deflater();
        deflater.setInput(data);
        deflater.finish();		ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length);
        byte[] buffer = new byte[1024];
        while (!deflater.finished()) {
            int count = deflater.deflate(buffer);
            outputStream.write(buffer, 0, count);
        }
        try {
            outputStream.close();
        } catch (IOException ignored) {
        }
        return outputStream.toByteArray();
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
}