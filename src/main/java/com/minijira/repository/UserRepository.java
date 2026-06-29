package com.minijira.repository;

import com.minijira.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {

    List<User> findByUsernameAndPassword(String username, String password);

    Optional<User> findByUsername(String username);
}
