package com.bezkoder.spring.security.postgresql.payload.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.*;

@AllArgsConstructor
@Data
public class UserResponse {
    private Long id;
    private String email;
    private String name;
    private String address;
    private String institutionalCode;
    private String applicationDate;
    private String loginLocation;
    private String website;
    private String landline;
    private String phoneNumber;
    private boolean disabled;
    private boolean verifiedApplication;
    private boolean verifiedEmail;
    private boolean activated2FA;
    private List<String> roles;

    public Date getDateOfApplication() {
        Map<String, Integer> monthMap = new HashMap<String, Integer>() {{
            put("January", Calendar.JANUARY);
            put("February", Calendar.FEBRUARY);
            put("March", Calendar.MARCH);
            put("April", Calendar.APRIL);
            put("May", Calendar.MAY);
            put("June", Calendar.JUNE);
            put("July", Calendar.JULY);
            put("August", Calendar.AUGUST);
            put("September", Calendar.SEPTEMBER);
            put("October", Calendar.OCTOBER);
            put("November", Calendar.NOVEMBER);
            put("December", Calendar.DECEMBER);
        }};

        Calendar calendar = Calendar.getInstance();
        String dayString = this.applicationDate.substring(this.applicationDate.indexOf(' ') + 1, this.applicationDate.indexOf(','));
        String monthString = this.applicationDate.substring(0, this.applicationDate.indexOf(' '));
        String yearString = this.applicationDate.substring(this.applicationDate.indexOf(',') + 2);

        Integer monthInteger = monthMap.get(monthString);
        int dayInteger = Integer.parseInt(dayString);
        int yearInteger = Integer.parseInt(yearString);

        calendar.set(yearInteger, monthInteger, dayInteger);
        return calendar.getTime();
    }
}
