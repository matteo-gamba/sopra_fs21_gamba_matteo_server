package ch.uzh.ifi.hase.soprafs21.rest.dto;
import java.time.LocalDate;

public class UserPostDTO {

    private String password;

    private String username;

    private String birthdate;

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getBirthdate() {
        return birthdate;
    }

    public void setBirthdate(String birthdate) {
        this.birthdate = birthdate;
    }
}
