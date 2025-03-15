package com.next.nest.entity;

import com.next.nest.entity.enums.Gender;
import com.next.nest.entity.enums.RoommateRequestStatus;
import com.next.nest.entity.enums.RoommateRequestType;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "roommate_requests")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoommateRequest extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RoommateRequestType requestType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RoommateRequestStatus status;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 1000)
    private String description;

    @Column(nullable = false)
    private String location;

    private String specificArea;

    @Column(nullable = false)
    private BigDecimal budget;

    private BigDecimal rentAmount;

    private String propertyType;

    private Integer bhkType;

    @Enumerated(EnumType.STRING)
    private Gender preferredGender;

    private String preferredAgeRange;

    @Column(nullable = false)
    private Boolean nonSmoker;

    @Column(nullable = false)
    private Boolean noPets;

    @ElementCollection
    private Set<String> lifestyle = new HashSet<>();

    @Column(nullable = false)
    private LocalDate moveInDate;

    @ElementCollection
    private List<String> imageUrls = new ArrayList<>();

    @Column(nullable = false)
    private Boolean isVerified;

    @OneToMany(mappedBy = "roommateRequest")
    private List<RoommateResponse> responses = new ArrayList<>();
}