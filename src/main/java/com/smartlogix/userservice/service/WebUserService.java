package com.smartlogix.userservice.service;

import com.smartlogix.userservice.entity.WebUser;
import com.smartlogix.userservice.repository.WebUserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class WebUserService {

    private final WebUserRepository repository;

    public WebUserService(WebUserRepository repository) {
        this.repository = repository;
    }

    //GET ALL USERS (for admin dashboard)
    public List<WebUser> getAllUsers() {
        return repository.findAll();
    }

    // Called by GET /users/me
    // Called by GET /users/search?username=  in admin search box
    //same method, controller decides the context (own profile vs admin looking for sm else)
    public Optional<WebUser> getUserByUsername(String username) {
        return repository.findByUsername(username);
    }

    // Called by DELETE /users/{id} — needs ID to locate the target user
    public Optional<WebUser> getUserById(Long userId) {
        return repository.findById(userId);
    }

    //----ADMIN (manager) creates USER (employee) account
    //Called by POST /users
    public WebUser createUser(String username, String password) {
        if (repository.findByUsername(username).isPresent()) {
            throw new RuntimeException("Usuario ya existe");
        }
        WebUser newUser = new WebUser();
        newUser.setUsername(username);
        newUser.setUserPassword(password); // hash in future
        newUser.setUserRole("USER");
        newUser.setMustChangePassword(true); // employee must set own password on first login
        return repository.save(newUser);
    }

    // Called by PUT /users/me/password — works for both ADMIN and USER
    // Username comes from JWT so no ID needed, no ownership check needed
    public void updatePassword(String username, String currentPassword, String newPassword) {
        WebUser user = repository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (!user.getUserPassword().equals(currentPassword)) {
            throw new RuntimeException("Contraseña actual incorrecta");
        }

        user.setUserPassword(newPassword); // hash in future
        user.setMustChangePassword(false); // clears the first-login flag
        repository.save(user);
    }


    // Called by DELETE /users/{id} — ADMIN only, SecurityConfig already enforces this
    public void deleteUser(Long userId) {
        if (!repository.existsById(userId)) {
            throw new RuntimeException("Usuario no encontrado");
        }
        repository.deleteById(userId);
    }

    // for ADMIN's user registration form validation
    // Called by GET /users/exists/{username}
    public boolean userExists(String username) {
        return repository.findByUsername(username).isPresent();
    }


}