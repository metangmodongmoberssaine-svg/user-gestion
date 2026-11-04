package com.labo.usergestion.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.labo.usergestion.dto.UserRequest;
import com.labo.usergestion.dto.UserResponse;
import com.labo.usergestion.exception.DuplicateEmailException;
import com.labo.usergestion.exception.ResourceNotFoundException;
import com.labo.usergestion.model.User;
import com.labo.usergestion.repository.UserRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private final UserRequest request = new UserRequest("Dupont", "Jean", "jean@mail.com");

    @Test
    void findAll_retourneLesUtilisateurs() {
        when(userRepository.findAll()).thenReturn(List.of(new User("Dupont", "Jean", "jean@mail.com")));

        List<UserResponse> result = userService.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).email()).isEqualTo("jean@mail.com");
    }

    @Test
    void findById_existant_retourneLUtilisateur() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(new User("Dupont", "Jean", "jean@mail.com")));

        UserResponse result = userService.findById(1L);

        assertThat(result.nom()).isEqualTo("Dupont");
        assertThat(result.prenom()).isEqualTo("Jean");
    }

    @Test
    void findById_inexistant_leveUneException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void create_emailLibre_sauvegarde() {
        when(userRepository.existsByEmail("jean@mail.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        UserResponse result = userService.create(request);

        assertThat(result.email()).isEqualTo("jean@mail.com");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void create_emailExistant_leveUneException() {
        when(userRepository.existsByEmail("jean@mail.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.create(request))
                .isInstanceOf(DuplicateEmailException.class);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void update_memeEmail_metAJourSansVerifierLeDoublon() {
        User existing = new User("Ancien", "Nom", "jean@mail.com");
        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepository.save(existing)).thenReturn(existing);

        UserResponse result = userService.update(1L, request);

        assertThat(result.nom()).isEqualTo("Dupont");
        verify(userRepository, never()).existsByEmail(any());
    }

    @Test
    void update_nouvelEmailLibre_metAJour() {
        User existing = new User("Ancien", "Nom", "ancien@mail.com");
        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepository.existsByEmail("jean@mail.com")).thenReturn(false);
        when(userRepository.save(existing)).thenReturn(existing);

        UserResponse result = userService.update(1L, request);

        assertThat(result.email()).isEqualTo("jean@mail.com");
    }

    @Test
    void update_nouvelEmailDejaPris_leveUneException() {
        User existing = new User("Ancien", "Nom", "ancien@mail.com");
        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepository.existsByEmail("jean@mail.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.update(1L, request))
                .isInstanceOf(DuplicateEmailException.class);
    }

    @Test
    void delete_existant_supprime() {
        User existing = new User("Dupont", "Jean", "jean@mail.com");
        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));

        userService.delete(1L);

        verify(userRepository).delete(existing);
    }

    @Test
    void delete_inexistant_leveUneException() {
        when(userRepository.findById(5L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.delete(5L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
