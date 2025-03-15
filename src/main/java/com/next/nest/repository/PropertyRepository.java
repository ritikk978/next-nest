package com.next.nest.repository;

import com.next.nest.entity.Property;
import com.next.nest.entity.enums.ListingStatus;
import com.next.nest.entity.enums.PropertyType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface PropertyRepository extends JpaRepository<Property, Long>, JpaSpecificationExecutor<Property> {

    @Query(value = "SELECT * FROM properties WHERE is_active = true",
            countQuery = "SELECT COUNT(*) FROM properties WHERE is_active = true",
            nativeQuery = true)
    Page<Property> findByIsActiveTrue(Pageable pageable);

    @Query(value = "SELECT * FROM properties WHERE owner_id = :ownerId AND is_active = true",
            countQuery = "SELECT COUNT(*) FROM properties WHERE owner_id = :ownerId AND is_active = true",
            nativeQuery = true)
    Page<Property> findByOwnerIdAndIsActiveTrue(@Param("ownerId") Long ownerId, Pageable pageable);

    @Query(value = "SELECT * FROM properties WHERE is_active = true AND status = :status",
            countQuery = "SELECT COUNT(*) FROM properties WHERE is_active = true AND status = :status",
            nativeQuery = true)
    Page<Property> findByStatusAndIsActiveTrue(@Param("status") String status, Pageable pageable);

    @Query(value = "SELECT * FROM properties WHERE is_active = true AND city = :city AND status = 'ACTIVE'",
            countQuery = "SELECT COUNT(*) FROM properties WHERE is_active = true AND city = :city AND status = 'ACTIVE'",
            nativeQuery = true)
    Page<Property> findByCityAndActiveStatus(@Param("city") String city, Pageable pageable);

    @Query(value = "SELECT * FROM properties WHERE is_active = true AND city = :city AND locality = :locality AND status = 'ACTIVE'",
            countQuery = "SELECT COUNT(*) FROM properties WHERE is_active = true AND city = :city AND locality = :locality AND status = 'ACTIVE'",
            nativeQuery = true)
    Page<Property> findByCityAndLocalityAndActiveStatus(
            @Param("city") String city,
            @Param("locality") String locality,
            Pageable pageable);

    @Query(value = "SELECT * FROM properties WHERE is_active = true AND city = :city AND property_type = :propertyType AND bhk_type = :bhkType AND status = 'ACTIVE'",
            countQuery = "SELECT COUNT(*) FROM properties WHERE is_active = true AND city = :city AND property_type = :propertyType AND bhk_type = :bhkType AND status = 'ACTIVE'",
            nativeQuery = true)
    Page<Property> findByCityAndPropertyTypeAndBhkTypeAndActiveStatus(
            @Param("city") String city,
            @Param("propertyType") String propertyType,
            @Param("bhkType") Integer bhkType,
            Pageable pageable);

    @Query(value = "SELECT * FROM properties WHERE is_active = true AND city = :city AND rent_amount BETWEEN :minRent AND :maxRent AND status = 'ACTIVE'",
            countQuery = "SELECT COUNT(*) FROM properties WHERE is_active = true AND city = :city AND rent_amount BETWEEN :minRent AND :maxRent AND status = 'ACTIVE'",
            nativeQuery = true)
    Page<Property> findByCityAndRentRangeAndActiveStatus(
            @Param("city") String city,
            @Param("minRent") BigDecimal minRent,
            @Param("maxRent") BigDecimal maxRent,
            Pageable pageable);

    @Query(value = "SELECT DISTINCT city FROM properties WHERE is_active = true ORDER BY city",
            nativeQuery = true)
    List<String> findDistinctCities();

    @Query(value = "SELECT DISTINCT locality FROM properties WHERE city = :city AND is_active = true ORDER BY locality",
            nativeQuery = true)
    List<String> findDistinctLocalitiesByCity(@Param("city") String city);

    @Query(value = "SELECT COUNT(*) FROM properties WHERE status = :status",
            nativeQuery = true)
    Long countByStatus(@Param("status") String status);

    // Fix for the date comparison issue
    @Query(value = "SELECT COUNT(*) FROM properties WHERE DATE(created_at) = CURRENT_DATE",
            nativeQuery = true)
    Long countPropertiesAddedToday();

    @Query(value = "SELECT * FROM properties " +
            "WHERE is_active = true AND status = 'ACTIVE' " +
            "AND ST_DistanceSphere(ST_MakePoint(longitude, latitude), ST_MakePoint(:lng, :lat)) <= :radiusInMeters",
            countQuery = "SELECT COUNT(*) FROM properties " +
                    "WHERE is_active = true AND status = 'ACTIVE' " +
                    "AND ST_DistanceSphere(ST_MakePoint(longitude, latitude), ST_MakePoint(:lng, :lat)) <= :radiusInMeters",
            nativeQuery = true)
    Page<Property> findPropertiesWithinRadius(
            @Param("lat") Double latitude,
            @Param("lng") Double longitude,
            @Param("radiusInMeters") Double radiusInMeters,
            Pageable pageable);
}