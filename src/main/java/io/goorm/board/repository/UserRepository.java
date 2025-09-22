package io.goorm.board.repository;

import io.goorm.board.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByEmail(String email);
    
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.company WHERE u.email = :email")
    Optional<User> findByEmailWithCompany(@Param("email") String email);
    
    boolean existsByEmail(String email);
}