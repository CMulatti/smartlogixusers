
package com.smartlogix.userservice.controller;

import com.smartlogix.userservice.dto.LoginRequest;
import com.smartlogix.userservice.dto.LoginResponse;
import com.smartlogix.userservice.entity.WebUser;
import com.smartlogix.userservice.repository.WebUserRepository;
import com.smartlogix.userservice.service.JwtService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final WebUserRepository userRepository;
    private final JwtService jwtService;

    public AuthController(WebUserRepository userRepository, JwtService jwtService) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        // Find user in database
        Optional<WebUser> userOpt = userRepository.findByUsername(request.getUsername());

        if (userOpt.isEmpty()) {
            return ResponseEntity.status(401).body("Usuario no encontrado");
        }

        WebUser user = userOpt.get();

        // Simple password check
        if (!user.getUserPassword().equals(request.getPassword())) {
            return ResponseEntity.status(401).body("Contraseña incorrecta");
        }

        //Generate JWT token
        String token = jwtService.generateToken(user.getUsername(), user.getUserRole());

        // return token and user info
        LoginResponse response = new LoginResponse(token, user.getUsername(), user.getUserRole());
        return ResponseEntity.ok(response);
    }
}