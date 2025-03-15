package com.next.nest.repository;

import com.next.nest.entity.Conversation;
import com.next.nest.entity.enums.ConversationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {
    
    @Query("SELECT c FROM Conversation c JOIN c.participants p WHERE p.id = :userId ORDER BY c.lastMessageAt DESC")
    Page<Conversation> findByParticipantIdOrderByLastMessageAtDesc(@Param("userId") Long userId, Pageable pageable);
    
    @Query("SELECT c FROM Conversation c JOIN c.participants p1 JOIN c.participants p2 " +
           "WHERE c.type = 'DIRECT' AND p1.id = :user1Id AND p2.id = :user2Id")
    Optional<Conversation> findDirectConversationBetweenUsers(
            @Param("user1Id") Long user1Id,
            @Param("user2Id") Long user2Id);
    
    @Query("SELECT c FROM Conversation c WHERE c.type = :type AND " +
           "EXISTS (SELECT 1 FROM c.participants p WHERE p.id = :userId)")
    Page<Conversation> findByTypeAndParticipantId(
            @Param("type") ConversationType type,
            @Param("userId") Long userId,
            Pageable pageable);
    
    @Query("SELECT c FROM Conversation c JOIN c.messages m WHERE m.property.id = :propertyId AND " +
           "EXISTS (SELECT 1 FROM c.participants p WHERE p.id = :userId) " +
           "GROUP BY c ORDER BY MAX(m.createdAt) DESC")
    List<Conversation> findByPropertyIdAndParticipantIdOrderByLastMessageAtDesc(
            @Param("propertyId") Long propertyId,
            @Param("userId") Long userId);
    
    @Query("SELECT c FROM Conversation c JOIN c.messages m WHERE m.roommateRequest.id = :requestId AND " +
           "EXISTS (SELECT 1 FROM c.participants p WHERE p.id = :userId) " +
           "GROUP BY c ORDER BY MAX(m.createdAt) DESC")
    List<Conversation> findByRoommateRequestIdAndParticipantIdOrderByLastMessageAtDesc(
            @Param("requestId") Long requestId,
            @Param("userId") Long userId);
    
    @Query("SELECT COUNT(c) FROM Conversation c JOIN c.participants p " +
           "WHERE p.id = :userId AND EXISTS (SELECT 1 FROM Message m WHERE m.conversation = c AND m.isRead = false AND m.sender.id != :userId)")
    Long countConversationsWithUnreadMessagesForUser(@Param("userId") Long userId);
}