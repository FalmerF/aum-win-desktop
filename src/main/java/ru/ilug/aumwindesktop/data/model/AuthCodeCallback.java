package ru.ilug.aumwindesktop.data.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthCodeCallback {

    private String code;
    private String redirectUri;

}
