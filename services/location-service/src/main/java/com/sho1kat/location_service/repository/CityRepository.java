package com.sho1kat.location_service.repository;

import com.sho1kat.location_service.entity.City;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CityRepository extends JpaRepository<City, Long> {
    boolean existsByCityCode(String cityCode);
    boolean existsByCityCodeAndIdNot(String cityCode, Long id);
    Page<City> findByCountryCodeIgnoreCase(String countryCode, Pageable pageable);

    @Query("""
            SELECT c
            FROM City c
            WHERE LOWER(c.cityName) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(c.cityCode) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(c.countryCode) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(c.countryName) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(c.regionCode) LIKE LOWER(CONCAT('%', :keyword, '%'))
            """)
    Page<City> searchByKeyWordIgnoreCase(
            @Param("keyword") String keyword,
            Pageable pageable
    );



}
