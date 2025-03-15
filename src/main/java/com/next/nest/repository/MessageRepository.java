package com.next.nest.repository;

import com.next.nest.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    
    Page<Message> findByConversationId(Long conversationId, Pageable pageable);
    
    List<Message> findByConversationIdOrderByCreatedAtAsc(Long conversationId);
    
    @Query("SELECT m FROM Message m WHERE m.conversation.id = :conversationId AND m.createdAt > :since ORDER BY m.createdAt ASC")
    List<Message> findByConversationIdAndCreatedAtAfterOrderByCreatedAtAsc(
            @Param("conversationId") Long conversationId,
            @Param("since") LocalDateTime since);
    
    @Query("SELECT m FROM Message m WHERE m.conversation.id = :conversationId AND m.isRead = false AND m.sender.id != :userId")
    List<Message> findUnreadMessagesByConversationAndUser(
            @Param("conversationId") Long conversationId,
            @Param("userId") Long userId);
    
    @Query("SELECT COUNT(m) FROM Message m WHERE m.conversation.id = :conversationId AND m.isRead = false AND m.sender.id != :userId")
    Long countUnreadMessagesByConversationAndUser(
            @Param("conversationId") Long conversationId,
            @Param("userId") Long userId);
    
    @Query("SELECT COUNT(m) FROM Message m WHERE m.conversation.id IN " +
           "(SELECT c.id FROM Conversation c JOIN c.participants p WHERE p.id = :userId) " +
           "AND m.isRead = false AND m.sender.id != :userId")
    Long countAllUnreadMessagesForUser(@Param("userId") Long userId);
    
    @Query("SELECT m FROM Message m WHERE m.property.id = :propertyId ORDER BY m.createdAt DESC")
    Page<Message> findByPropertyIdOrderByCreatedAtDesc(@Param("propertyId") Long propertyId, Pageable pageable);
    
    @Query("SELECT m FROM Message m WHERE m.roommateRequest.id = :requestId ORDER BY m.createdAt DESC")
    Page<Message> findByRoommateRequestIdOrderByCreatedAtDesc(@Param("requestId") Long requestId, Pageable pageable);
}