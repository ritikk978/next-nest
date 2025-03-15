package com.next.nest.entity;

import com.next.nest.entity.enums.RoommateResponseStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "roommate_responses")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoommateResponse extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "roommate_request_id", nullable = false)
    private RoommateRequest roommateRequest;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responder_id", nullable = false)
    private User responder;

    @Column(nullable = false, length = 1000)
    private String message;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private RoommateResponseStatus status;

    private boolean isRead;

    private String contactInformation;

    @Column(length = 1000)
    private String notes;
}