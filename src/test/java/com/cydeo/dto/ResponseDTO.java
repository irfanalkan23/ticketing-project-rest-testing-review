package com.cydeo.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

//when we try to retrieve token from Keycloak, we don't need other fields
//we only need token field. we create a ResponseDTO class with only token field
//so, other fields coming from Keycloak will not be deserialized
@Getter
@Setter
@NoArgsConstructor
public class ResponseDTO {

    //Keycloak is using field name as "access_token" in the json body when Keycloak sends the token
    private String access_token;

}
