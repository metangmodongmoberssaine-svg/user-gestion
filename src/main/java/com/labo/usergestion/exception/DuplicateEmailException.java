package com.labo.usergestion.exception;

public class DuplicateEmailException extends RuntimeException {

    public DuplicateEmailException(String email) {
        super("Un utilisateur existe déjà avec l'email " + email);
    }
}
