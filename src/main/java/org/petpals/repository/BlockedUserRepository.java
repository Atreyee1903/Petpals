package org.petpals.repository;

import org.petpals.model.BlockedUser;
import org.petpals.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BlockedUserRepository extends JpaRepository<BlockedUser, Long> {
    
    @Query("SELECT bu FROM BlockedUser bu WHERE bu.user.id = :userId AND bu.active = true")
    Optional<BlockedUser> findActiveBlockByUserId(@Param("userId") Long userId);
    
    @Query("SELECT bu FROM BlockedUser bu WHERE bu.user.id = :userId")
    Optional<BlockedUser> findByUserId(@Param("userId") Long userId);
    
    @Query("SELECT bu FROM BlockedUser bu WHERE bu.active = true ORDER BY bu.blockedAt DESC")
    List<BlockedUser> findAllActive();
    
    boolean existsByUserIdAndActiveTrue(Long userId);
}
