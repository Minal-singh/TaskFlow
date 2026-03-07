package com.minal.taskflow.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserRequestDto {
    @NonNull
    private String userName;

    @NonNull
    private String email;

    @NonNull
    private String password;
}
