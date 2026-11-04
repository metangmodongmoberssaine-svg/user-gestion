package com.labo.usergestion.exception;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(Long id) {
        super("Utilisateur introuvable avec l'id " + id);
    }
}
