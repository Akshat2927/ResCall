package com.backend.ResCall.repository;

import com.backend.ResCall.entity.User;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface UserRepository extends MongoRepository<User , String> {
    Optional<User> findByEmail(String email);
}
