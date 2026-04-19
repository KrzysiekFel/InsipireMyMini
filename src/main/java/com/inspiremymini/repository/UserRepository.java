package com.inspiremymini.repository;

import com.inspiremymini.model.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
}
