package com.bezkoder.spring.security.postgresql.controllers;

import com.bezkoder.spring.security.postgresql.models.*;
import com.bezkoder.spring.security.postgresql.payload.request.NewPasswordRequest;
import com.bezkoder.spring.security.postgresql.payload.request.PasswordRequest;
import com.bezkoder.spring.security.postgresql.payload.request.WebsiteRequest;
import com.bezkoder.spring.security.postgresql.payload.response.MessageResponse;
import com.bezkoder.spring.security.postgresql.payload.response.UserResponse;
import com.bezkoder.spring.security.postgresql.repository.ImageRepository;
import com.bezkoder.spring.security.postgresql.repository.PasswordResetTokenRepository;
import com.bezkoder.spring.security.postgresql.repository.RoleRepository;
import com.bezkoder.spring.security.postgresql.repository.membruSenatRepository;
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

    private final String username = "AC315b0b103eacf332065bb30dca612446";

    private final String password = "674683a5ef30a6d98ad9b12b1dab95d3";

    @GetMapping("/return_all")
    public List<UserResponse> returnAll() {
        List<UserResponse> result = new ArrayList<UserResponse>();
        membruSenatService.returnAll().forEach((member) -> {
            result.add(memberToUserResponse(member));
        });
        return result;
    }

    @GetMapping("/still_pending")
    public List<UserResponse> stillPending() {
        List<UserResponse> result = new ArrayList<UserResponse>();
        membruSenatService.returnAll().forEach((member) -> {
            if(!member.isVerifiedApplication()) {
                result.add(memberToUserResponse(member));
            }
        });
        return result;
    }

    @PostMapping("/sendPhoneVerification/{id}/{number}")
    public ResponseEntity<?> sendPhoneVerification(@PathVariable("id") Long id, @PathVariable("number") String number) {
        if(!membruSenatRepo.existsById(id)) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Member does not exist !"));
        }
        membruSenat member = membruSenatService.findMemberById(id);
        Twilio.init(this.username, this.password);
        if(member.getVerificationSID() != null) {
            Service.deleter(member.getVerificationSID()).delete();
        }
        Service service = Service.creator("UNITBV Voting").create();
        Verification verification = Verification.creator(
                service.getSid(),
                number,
                "sms")
                .create();
        member.setVerificationSID(service.getSid());
        member.setPhoneNumber2BVerified(number);
        membruSenatRepo.save(member);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/confirmPhone/{id}/{code}")
    public ResponseEntity<?> confirmPhone(@PathVariable("id") Long id, @PathVariable("code") String code) {
        membruSenat member = membruSenatRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("User does not exist!"));
        if(member.getVerificationSID() == null) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Try sending another verification request !"));
        }
        Twilio.init(this.username, this.password);
        try {
            VerificationCheck verificationCheck = VerificationCheck.creator(
                    member.getVerificationSID(),
                    code)
                    .setTo(member.getPhoneNumber2BVerified()).create();
            System.out.println(verificationCheck.getStatus().toLowerCase(Locale.ROOT));
            if(verificationCheck.getStatus().toLowerCase(Locale.ROOT).compareToIgnoreCase("approved") == 0) {
                member.setVerificationSID(null);
                member.setPhoneNumber(member.getPhoneNumber2BVerified());
                member.setPhoneNumber2BVerified(null);
                membruSenatRepo.save(member);
            }
            else {
                return ResponseEntity
                        .badRequest()
                        .body(new MessageResponse("Wrong code.Try again !"));
            }
        } catch(Exception e) {
            Service.deleter(member.getVerificationSID()).delete();
            member.setVerificationSID(null);
            member.setPhoneNumber2BVerified(null);
            membruSenatRepo.save(member);
            return ResponseEntity
                    .badRequest()
                    .body("Code request expired.Try sending another one !");
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/update_roles/{id}/{role}")
    public ResponseEntity<?> updateRoles(@PathVariable("id") Long id, @PathVariable("role") ERole role) {
        if(!membruSenatRepo.existsById(id)) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Member does not exist !"));
        }
        membruSenat member = membruSenatService.findMemberById(id);
        Set<Role> roles = new HashSet<>();
        Set<Role> userRoles = member.getRoles();
        if(userRoles.contains(new Role(1, ERole.ROLE_USER))) {
            roles.add(new Role(1, ERole.ROLE_USER));
        }
        else if(userRoles.contains(new Role(1, ERole.ROLE_ADMIN))) {
            roles.add(new Role(1, ERole.ROLE_ADMIN));
        }
        if(role != ERole.ROLE_DELETE) {
            Role otherRole = roleRepository.findByName(role)
                    .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
            roles.add(otherRole);
        }
        member.setRoles(roles);
        membruSenatRepo.save(member);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/update_address/{id}/{address}")
    public ResponseEntity<?> updateAddress(@PathVariable("id") Long id, @PathVariable("address") String address) {
        if(!membruSenatRepo.existsById(id)) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Member does not exist !"));
        }
        membruSenat member = membruSenatService.findMemberById(id);
        member.setAddress(address);
        membruSenatRepo.save(member);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/update_name/{id}/{name}")
    public ResponseEntity<?> updateName(@PathVariable("id") Long id, @PathVariable("name") String name) {
        if(!membruSenatRepo.existsById(id)) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Member does not exist !"));
        }
        membruSenat member = membruSenatService.findMemberById(id);
        member.setName(name);
        membruSenatRepo.save(member);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/update_landline/{id}/{landline}")
    public ResponseEntity<?> updateLandline(@PathVariable("id") Long id, @PathVariable("landline") String landline) {
        if(!membruSenatRepo.existsById(id)) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Member does not exist !"));
        }
        membruSenat member = membruSenatService.findMemberById(id);
        member.setLandline(landline);
        membruSenatRepo.save(member);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/update_email/{id}/{email}")
    public ResponseEntity<?> updateEmail(@PathVariable("id") Long id, @PathVariable("email") String email) {
        if(!membruSenatRepo.existsById(id)) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Member does not exist !"));
        }
        membruSenat member = membruSenatService.findMemberById(id);
        ImageModel image = imageRepository.findByName(member.getEmail())
                .orElseThrow(() -> new RuntimeException("Email unavailable!"));
        image.setName(email);
        member.setEmail(email);
        membruSenatRepo.save(member);
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
    public ResponseEntity<?> acceptApplication(@Valid @RequestBody WebsiteRequest websiteRequest) throws IOException {
        if(!membruSenatRepo.existsById(websiteRequest.getId())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Member does not exist !"));
        }
        URL url = new URL(websiteRequest.getWebsite());
        HttpURLConnection huc = (HttpURLConnection) url.openConnection();
        int responseCode = huc.getResponseCode();
        if(responseCode == HttpURLConnection.HTTP_NOT_FOUND) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Site URL is unavailable.Try another !"));
        }
        membruSenat member = membruSenatService.findMemberById(websiteRequest.getId());
        member.setWebsite(websiteRequest.getWebsite());
        membruSenatRepo.save(member);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/toggle_2FA/{id}")
    public ResponseEntity<?> toggle2FA(@PathVariable("id") Long id) {
        if(!membruSenatRepo.existsById(id)) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Member does not exist !"));
        }
        membruSenat member = membruSenatService.findMemberById(id);
        boolean twoFactor = member.isActivated2FA();
        member.setActivated2FA(!twoFactor);
        membruSenatRepo.save(member);
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
        UserResponse userResult = memberToUserResponse(member);
        return new ResponseEntity<>(userResult, HttpStatus.OK);
    }

    @GetMapping("/check2FA/{email}")
    public boolean check2FA(@PathVariable("email") String email) {
        membruSenat member = membruSenatService.findMemberByEmail(email);
        return member.isActivated2FA();
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteMember(@PathVariable("id") Long id) {
        membruSenatService.deleteMemberById(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/check_password")
    public ResponseEntity<?> checkPassword(@Valid @RequestBody PasswordRequest passwordRequest) {
        if(!membruSenatRepo.existsById(passwordRequest.getId())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Member does not exist !"));
        }
        membruSenat member = membruSenatService.findMemberById(passwordRequest.getId());
        BCryptPasswordEncoder bEn = new BCryptPasswordEncoder();
        if(bEn.matches(passwordRequest.getPassword(), member.getPassword())) {
            return new ResponseEntity<>(HttpStatus.OK);
        }
        else {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Wrong password !"));
        }
    }

    @PostMapping("/change_password")
    public ResponseEntity<?> changePassword(@Valid @RequestBody NewPasswordRequest newPasswordRequest) {
        if(!membruSenatRepo.existsById(newPasswordRequest.getId())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Member does not exist !"));
        }
        membruSenat member = membruSenatService.findMemberById(newPasswordRequest.getId());
        member.setPassword(encoder.encode(newPasswordRequest.getNewPassword()));
        membruSenatRepo.save(member);
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
    public List<ImageModel> getAllImages() throws IOException {
        List<ImageModel> retrievedImages = new ArrayList<ImageModel>();
        imageRepository.findAll().forEach((image) -> {
            retrievedImages.add(new ImageModel(image.getName(), image.getType(),
                    decompressBytes(image.getPicByte())));
        });
        return retrievedImages;
    }

    @GetMapping(path = { "/get/{imageName}" })
    public ImageModel getImage(@PathVariable("imageName") String imageName) throws IOException {
        final Optional<ImageModel> retrievedImage = imageRepository.findByName(imageName);
        ImageModel img = new ImageModel(retrievedImage.get().getName(), retrievedImage.get().getType(),
                decompressBytes(retrievedImage.get().getPicByte()));
        return img;
    }

    public String ERoleToString(ERole erole) {
        switch(erole) {
            case ROLE_USER:
                return "Utilizator";
            case ROLE_ADMIN:
                return "Administrator";
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

    private UserResponse memberToUserResponse(membruSenat member) {
        Set<Role> userRoles = member.getRoles();
        Iterator<Role> it = userRoles.iterator();
        List<String> roles = new ArrayList<>();
        while(it.hasNext()) {
            ERole roleName = it.next().getName();
            roles.add(ERoleToString(roleName));
        }
        Collections.sort(roles, Comparator.comparingInt(String::length));
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
                                 member.isVerifiedApplication(),
                                 member.isVerifiedEmail(),
                                 member.isActivated2FA(),
                                 member.isAdminPriviledge(),
                                 roles );
    }

    public static byte[] compressBytes(byte[] data) {
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
        } catch (IOException e) {
        }
        return outputStream.toByteArray();
    }

    public static byte[] decompressBytes(byte[] data) {
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
        } catch (IOException ioe) {
        } catch (DataFormatException e) {
        }
        return outputStream.toByteArray();
    }
}