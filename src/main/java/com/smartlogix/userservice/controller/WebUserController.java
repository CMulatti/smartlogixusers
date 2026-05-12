

package com.smartlogix.userservice.controller;

import com.smartlogix.userservice.dto.RegisterRequest;
import com.smartlogix.userservice.dto.UpdatePasswordRequest;
import com.smartlogix.userservice.entity.WebUser;
import com.smartlogix.userservice.service.WebUserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:5173") // Allow React frontend,this is the port when we run the app in npm run dev
@RestController
@RequestMapping("/users")
public class WebUserController {

    private final WebUserService userService;

    public WebUserController(WebUserService userService) {
        this.userService = userService;
    }

    //Register new user (PUBLIC, anyone can register)
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        try {
            WebUser newUser = userService.registerUser(request.getUsername(), request.getPassword());
            return ResponseEntity.ok("Usuario registrado exitosamente");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Get all users (ADMIN ONLY , already protected by SecurityConfig)
    @GetMapping
    public ResponseEntity<List<WebUser>> getAllUsers() {
        List<WebUser> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    // Get user by ID (AUTHENTICATED:  user can see their own data, admin can see any)
    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id, Authentication authentication) {
        // Check if user is admin or requesting their own data
        if (!isAdminOrOwner(authentication, id)) {
            return ResponseEntity.status(403).body("No tienes permiso para ver este usuario");
        }

        return userService.getUserById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    //fetching user data to get my own profile as regular user
    @GetMapping("/me")
    public ResponseEntity<?> getMyProfile(Authentication authentication) {
        String username = authentication.getName();

        return userService.getAllUsers().stream()
                .filter(user -> user.getUsername().equals(username))
                .findFirst()
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }


    // // Update password (ONLY regular USERS can update their own password, ADMIN cannot update any passwords)
    // in react MyAccount.jsx, password change form calls this
    @PutMapping("/{id}/password")
    public ResponseEntity<?> updatePassword(@PathVariable Long id,
                                            @RequestBody UpdatePasswordRequest request,
                                            Authentication authentication) {

        String currentUsername = authentication.getName();

        return userService.getUserById(id)
                .map(userToUpdate -> {
                    // Block ALL ADMIN users from changing any password (their own or others')
                    if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
                        return ResponseEntity.status(403)
                                .body("Los administradores no pueden cambiar contraseñas");
                    }

                    //Only allow users to change their OWN password
                    if (!userToUpdate.getUsername().equals(currentUsername)) {
                        return ResponseEntity.status(403)
                                .body("Solo puedes cambiar tu propia contraseña");
                    }

                    //proceed with password update
                    try {
                        userService.updatePassword(id, request.getCurrentPassword(), request.getNewPassword());
                        return ResponseEntity.ok("Contraseña actualizada exitosamente");
                    } catch (Exception e) {
                        return ResponseEntity.badRequest().body(e.getMessage());
                    }
                })
                .orElse(ResponseEntity.notFound().build());
    }


    // Delete user (ADMIN can delete any user EXCEPT themselves, USER can delete their own account)
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id, Authentication authentication) {
        // Check if user is admin or deleting their own account
        if (!isAdminOrOwner(authentication, id)) {
            return ResponseEntity.status(403).body("No tienes permiso para eliminar este usuario");
        }

        // Get the user to be deleted
        String currentUsername = authentication.getName();
        return userService.getUserById(id)
                .map(userToDelete -> {
                    // Prevent ADMIN from deleting their own account
                    if (userToDelete.getUsername().equals(currentUsername) &&
                            "ADMIN".equals(userToDelete.getUserRole())) {
                        return ResponseEntity.status(403)
                                .body("Los administradores no pueden eliminar su propia cuenta");
                    }

                    // Proceed with deletion
                    try {
                        userService.deleteUser(id);
                        return ResponseEntity.ok("Usuario eliminado exitosamente");
                    } catch (Exception e) {
                        return ResponseEntity.badRequest().body(e.getMessage());
                    }
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // Check if username exists (PUBLIC: useful for registration form validation)
    @GetMapping("/exists/{username}")
    public ResponseEntity<Boolean> userExists(@PathVariable String username) {
        boolean exists = userService.userExists(username);
        return ResponseEntity.ok(exists);
    }

    // Helper method to check if user is admin OR is the owner of the resource
    // SimpleGrantedAuthority is a small Spring Security class whose only job is to represent a role or permission the user has,
    // we can think of it like a badge (admin badge, normal user badge, etc)
    //These badges are stored inside Authentication. We need SimpleGrantedAuthority because Spring security doesn't store a user's permission
    // as plain Strings internally. Instead, each permission is an object implementing the GrantedAuthority interface.
    private boolean isAdminOrOwner(Authentication authentication, Long userId) {
        // Check if user is admin
        if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
            return true;
        }

        // Check if user is the owner (username matches the user's ID)
        String username = authentication.getName();
        return userService.getUserById(userId)
                .map(user -> user.getUsername().equals(username))
                .orElse(false);
    }
}