package com.labo.usergestion.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.labo.usergestion.dto.UserRequest;
import com.labo.usergestion.dto.UserResponse;
import com.labo.usergestion.exception.DuplicateEmailException;
import com.labo.usergestion.exception.ResourceNotFoundException;
import com.labo.usergestion.service.UserService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    private final UserRequest request = new UserRequest("Dupont", "Jean", "jean@mail.com");
    private final UserResponse response = new UserResponse(1L, "Dupont", "Jean", "jean@mail.com");

    @Test
    void getAll_retourne200() throws Exception {
        when(userService.findAll()).thenReturn(List.of(response));

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].email").value("jean@mail.com"));
    }

    @Test
    void getById_retourne200() throws Exception {
        when(userService.findById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nom").value("Dupont"));
    }

    @Test
    void getById_inexistant_retourne404() throws Exception {
        when(userService.findById(9L)).thenThrow(new ResourceNotFoundException(9L));

        mockMvc.perform(get("/api/users/9"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.erreur").exists());
    }

    @Test
    void post_valide_retourne201() throws Exception {
        when(userService.create(any(UserRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void post_invalide_retourne400() throws Exception {
        UserRequest invalide = new UserRequest("", "Jean", "pas-un-email");

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalide)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.nom").exists())
                .andExpect(jsonPath("$.email").exists());
    }

    @Test
    void post_emailDuplique_retourne409() throws Exception {
        when(userService.create(any(UserRequest.class)))
                .thenThrow(new DuplicateEmailException("jean@mail.com"));

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void put_valide_retourne200() throws Exception {
        when(userService.update(eq(1L), any(UserRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.prenom").value("Jean"));
    }

    @Test
    void delete_existant_retourne204() throws Exception {
        doNothing().when(userService).delete(1L);

        mockMvc.perform(delete("/api/users/1")).andExpect(status().isNoContent());
    }

    @Test
    void delete_inexistant_retourne404() throws Exception {
        doThrow(new ResourceNotFoundException(7L)).when(userService).delete(7L);

        mockMvc.perform(delete("/api/users/7")).andExpect(status().isNotFound());
    }
}
