package com.priya.devflow.config;

import com.priya.devflow.entity.AppUser;
import com.priya.devflow.entity.Role;
import com.priya.devflow.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.HexFormat;

@Component
@Order(1)
public class DataInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final AppProperties props;

    public DataInitializer(UserRepository users, PasswordEncoder encoder, AppProperties props) {
        this.users = users;
        this.encoder = encoder;
        this.props = props;
    }

    @Override
    public void run(ApplicationArguments args) {
        String username = props.admin().username();
        if (users.existsByUsername(username)) {
            return;
        }
        String password = props.admin().password();
        if (password == null || password.isBlank()) {
            byte[] bytes = new byte[9];
            new SecureRandom().nextBytes(bytes);
            password = HexFormat.of().formatHex(bytes);
            log.warn("ADMIN_PASSWORD not set. Generated one-time admin password for '{}': {}", username, password);
        }
        users.save(new AppUser(username, encoder.encode(password), Role.ADMIN));
        log.info("Created admin user '{}'", username);
    }
}
