package es.moki.ratelimij.dropwizard.component.app.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class LoginRequest {

    public final String username;

    public final String password;

    @JsonCreator
    public LoginRequest(@JsonProperty("username") String username,
                        @JsonProperty("password") String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}
