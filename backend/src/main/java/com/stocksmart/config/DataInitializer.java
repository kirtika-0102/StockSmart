package com.stocksmart.config;

import java.util.Set;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.stocksmart.entity.Role;
import com.stocksmart.entity.RoleName;
import com.stocksmart.entity.User;
import com.stocksmart.repository.RoleRepository;
import com.stocksmart.repository.UserRepository;

@Component
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(
            RoleRepository roleRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        for (RoleName name : RoleName.values()) {
            roleRepository.findByName(name).orElseGet(() -> {
                Role role = new Role();
                role.setName(name);
                return roleRepository.save(role);
            });
        }

        seedUser("admin", "admin@stocksmart.local", "Admin@123", "System", "Admin", RoleName.ADMIN);
        seedUser("manager", "manager@stocksmart.local", "Manager@123", "Inventory", "Manager",
                RoleName.INVENTORY_MANAGER);
        seedUser("staff", "staff@stocksmart.local", "Staff@123", "Store", "Staff", RoleName.STAFF);
    }

    private void seedUser(String username, String email, String rawPassword, String firstName, String lastName,
            RoleName roleName) {
        if (userRepository.existsByUsername(username)) {
            return;
        }
        Role role = roleRepository.findByName(roleName).orElseThrow();
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setActive(true);
        user.setRoles(Set.of(role));
        userRepository.save(user);
    }
}
