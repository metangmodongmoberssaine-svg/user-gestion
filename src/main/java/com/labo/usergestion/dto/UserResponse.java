package com.labo.usergestion.dto;

import com.labo.usergestion.model.User;

public record UserResponse(Long id, String nom, String prenom, String email) {

    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getNom(), user.getPrenom(), user.getEmail());
    }
}
