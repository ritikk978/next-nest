package com.next.nest.repository;

import com.next.nest.entity.User;
import com.next.nest.entity.enums.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    @Query(value = "SELECT * FROM users WHERE email = :email", nativeQuery = true)
    Optional<User> findByEmail(@Param("email") String email);

    @Query(value = "SELECT * FROM users WHERE phone_number = :phoneNumber", nativeQuery = true)
    Optional<User> findByPhoneNumber(@Param("phoneNumber") String phoneNumber);

    @Query(value = "SELECT CASE WHEN COUNT(id) > 0 THEN true ELSE false END FROM users WHERE email = :email", nativeQuery = true)
    boolean existsByEmail(@Param("email") String email);

    @Query(value = "SELECT CASE WHEN COUNT(id) > 0 THEN true ELSE false END FROM users WHERE phone_number = :phoneNumber", nativeQuery = true)
    boolean existsByPhoneNumber(@Param("phoneNumber") String phoneNumber);

    @Query(value = "SELECT * FROM users WHERE role = :role",
            countQuery = "SELECT COUNT(*) FROM users WHERE role = :role",
            nativeQuery = true)
    Page<User> findAllByRole(@Param("role") String role, Pageable pageable);

    @Query(value = "SELECT * FROM users WHERE role = :role AND " +
            "(LOWER(first_name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(last_name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(email) LIKE LOWER(CONCAT('%', :search, '%')))",
            countQuery = "SELECT COUNT(*) FROM users WHERE role = :role AND " +
                    "(LOWER(first_name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                    "LOWER(last_name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                    "LOWER(email) LIKE LOWER(CONCAT('%', :search, '%')))",
            nativeQuery = true)
    Page<User> findAllByRoleAndSearchText(@Param("role") String role,
                                          @Param("search") String search,
                                          Pageable pageable);

    @Query(value = "SELECT u.* FROM users u " +
            "JOIN user_properties up ON u.id = up.user_id " +
            "WHERE up.property_id = :propertyId",
            nativeQuery = true)
    Optional<User> findPropertyOwnerByPropertyId(@Param("propertyId") Long propertyId);

    @Query(value = "SELECT COUNT(*) FROM users WHERE role = :role", nativeQuery = true)
    Long countByRole(@Param("role") String role);

    @Query(value = "SELECT COUNT(*) FROM users WHERE DATE(created_at) = CURRENT_DATE", nativeQuery = true)
    Long countTodayRegistrations();

    @Query(value = "SELECT * FROM users WHERE email_verified = false AND created_at < :date", nativeQuery = true)
    List<User> findByEmailVerifiedFalseAndCreatedAtBefore(@Param("date") java.time.LocalDateTime date);
}