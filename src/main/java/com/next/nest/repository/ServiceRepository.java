package com.next.nest.repository;

import com.next.nest.entity.Service;
import com.next.nest.entity.ServiceStatus;
import com.next.nest.entity.enums.ServiceCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ServiceRepository extends JpaRepository<Service, Long> {
    
    Page<Service> findByCategory(ServiceCategory category, Pageable pageable);
    
    Page<Service> findByStatus(ServiceStatus status, Pageable pageable);
    
    Page<Service> findByFeaturedTrue(Pageable pageable);
    
    @Query("SELECT s FROM Service s WHERE :area MEMBER OF s.serviceAreas AND s.status = 'ACTIVE'")
    Page<Service> findByServiceAreaAndActive(@Param("area") String area, Pageable pageable);
    
    @Query("SELECT s FROM Service s WHERE s.category = :category AND s.status = 'ACTIVE' AND " +
           ":area MEMBER OF s.serviceAreas")
    Page<Service> findByCategoryAndServiceAreaAndActive(
            @Param("category") ServiceCategory category,
            @Param("area") String area,
            Pageable pageable);
    
    @Query("SELECT s FROM Service s WHERE s.status = 'ACTIVE' AND " +
           "s.basePrice BETWEEN :minPrice AND :maxPrice")
    Page<Service> findByActivePriceRange(
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            Pageable pageable);
    
    @Query("SELECT s FROM Service s WHERE s.status = 'ACTIVE' AND " +
           "s.category = :category AND s.basePrice BETWEEN :minPrice AND :maxPrice")
    Page<Service> findByCategoryAndActivePriceRange(
            @Param("category") ServiceCategory category,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            Pageable pageable);
    
    @Query("SELECT s FROM Service s WHERE s.status = 'ACTIVE' ORDER BY s.rating DESC")
    Page<Service> findActiveServicesOrderByRating(Pageable pageable);
    
    @Query("SELECT DISTINCT s.category FROM Service s WHERE s.status = 'ACTIVE' ORDER BY s.category")
    List<ServiceCategory> findDistinctActiveCategories();
    
    @Query("SELECT DISTINCT sa FROM Service s JOIN s.serviceAreas sa WHERE s.status = 'ACTIVE' ORDER BY sa")
    List<String> findDistinctActiveServiceAreas();
    
    @Query("SELECT COUNT(s) FROM Service s WHERE s.category = :category AND s.status = 'ACTIVE'")
    Long countActiveByCategoryAndStatus(@Param("category") ServiceCategory category);
}