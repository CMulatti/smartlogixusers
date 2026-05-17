

package com.smartlogix.userservice.controller;

import com.smartlogix.userservice.dto.CreateUserRequest;
import com.smartlogix.userservice.dto.UpdatePasswordRequest;
import com.smartlogix.userservice.entity.WebUser;
import com.smartlogix.userservice.service.WebUserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
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



    //Fetch own profile, now identity comes from JWT, no ID needed, no way to access someone else's data
    @GetMapping("/me")
    public ResponseEntity<?> getMyProfile(Authentication authentication) {
        String username = authentication.getName();
        return userService.getUserByUsername(username)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }


    //No ownership check needed: the JWT IS the identity
    @PutMapping("/me/password")
    public ResponseEntity<?> updateMyPassword(@RequestBody UpdatePasswordRequest request, Authentication authentication) {
        try {
            String username = authentication.getName();
            userService.updatePassword(username,
                    request.getCurrentPassword(),
                    request.getNewPassword());
            return ResponseEntity.ok("Contraseña actualizada exitosamente");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    //-------------ADMIN ONLY ----- SecurityCongig already enforces this rule ------


    // No open registration: external users are not allowed in the system, only ADMIN (manager) creates USER (employees) accounts
    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody CreateUserRequest request) {
        try {
            userService.createUser(request.getUsername(), request.getPassword());
            return ResponseEntity.ok("Usuario creado exitosamente");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    //ADMIN can get all users from his admin dashboard
    @GetMapping
    public ResponseEntity<List<WebUser>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    //ADMIN deletes USER accounts, but NOT his own admin account
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id, Authentication authentication) {
        return userService.getUserById(id).map(userToDelete -> {
                    if (userToDelete.getUsername().equals(authentication.getName())) {
                        return ResponseEntity.status(403).body("Los administradores no pueden eliminar su propia cuenta");
                    }
                    try {
                        userService.deleteUser(id);
                        return ResponseEntity.ok("Usuario eliminado exitosamente");
                    } catch (Exception e) {
                        return ResponseEntity.badRequest().body(e.getMessage());
                    }
                })
                .orElse(ResponseEntity.notFound().build());
    }

    //This will be needed when ADMIN looks for a user in the frontend
    //@RequestParam is cleaner than {id} for a search box, frontend will send whatever admin typed and get the user back or a 404
    @GetMapping("/search")
    public ResponseEntity<?> getUserByUsername(@RequestParam String username) {
        return userService.getUserByUsername(username)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    //This will be needed when ADMIN creates the USER account
    @GetMapping("/exists/{username}")
    public ResponseEntity<Boolean> userExists(@PathVariable String username) {
        boolean exists = userService.userExists(username);
        return ResponseEntity.ok(exists);
    }


}