package com.smartlogix.userservice.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "webusers")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WebUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "username", length = 30, nullable = false, unique = true)
    private String username;

    @JsonIgnore //When Spring converts our WebUser object to JSON it will skip the userPassword field, we want to ensure passwords are never returned in the JSON
    @Column(name = "user_password", length = 60, nullable = false)
    private String userPassword;  // Changed field name to match DB column

    @Column(name = "user_role", length = 10)
    private String userRole;  // Changed field name to match DB column
}