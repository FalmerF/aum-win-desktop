package ru.ilug.aumwindesktop.data.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class User {

    private String login;
    private long id;
    private String name;
    @JsonProperty("avatar_url")
    private String avatarUrl;

}
