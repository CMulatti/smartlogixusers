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

    //GET USER BY ID: returns an optional bc user might not exist, so we let Controller decide what to do if not found
    public Optional<WebUser> getUserById(Long userId) {
        return repository.findById(userId);
    }

    //GET USER BY USERNAME
    public Optional<WebUser> getUserByUsername(String username) {
        return repository.findByUsername(username);
    }

    // REGISTER NEW USER
    public WebUser registerUser(String username, String password) {
        // Check if username already exists
        if (repository.findByUsername(username).isPresent()) {
            throw new RuntimeException("Usuario ya existe");
        }
        //build new user object
        WebUser newUser = new WebUser();
        newUser.setUsername(username);
        newUser.setUserPassword(password); // This in the future will need to be hashed
        newUser.setUserRole("USER"); //Default role

        return repository.save(newUser); //save new user to database
    }

    //UPDATE PASSWORD
    public void updatePassword(Long userId, String currentPassword, String newPassword) {
        WebUser user = repository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        //Verify current password
        if (!user.getUserPassword().equals(currentPassword)) {
            throw new RuntimeException("Contraseña actual incorrecta");
        }

        user.setUserPassword(newPassword); //to be hashed in the future
        repository.save(user);
    }

    // Delete user
    public void deleteUser(Long userId) {
        if (!repository.existsById(userId)) {
            throw new RuntimeException("Usuario no encontrado");
        }
        repository.deleteById(userId);
    }

    //Check if user exists
    public boolean userExists(String username) {
        return repository.findByUsername(username).isPresent();
    }
}