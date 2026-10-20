package com.hotelease.repository;

import com.hotelease.model.Hotel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HotelRepository extends JpaRepository<Hotel, Long> {
    List<Hotel> findByCityIgnoreCase(String city);
    List<Hotel> findByCityContainingIgnoreCase(String city);
    List<Hotel> findByCountryIgnoreCase(String country);
    List<Hotel> findByStateIgnoreCase(String state);
    List<Hotel> findByDistrictIgnoreCase(String district);

    @Query("SELECT h FROM Hotel h WHERE " +
           "(:country IS NULL OR :country = '' OR LOWER(h.country) = LOWER(:country)) AND " +
           "(:state IS NULL OR :state = '' OR LOWER(h.state) = LOWER(:state)) AND " +
           "(:district IS NULL OR :district = '' OR LOWER(h.district) LIKE LOWER(CONCAT('%', :district, '%'))) AND " +
           "(:city IS NULL OR :city = '' OR LOWER(h.city) LIKE LOWER(CONCAT('%', :city, '%'))) AND " +
           "(:landmark IS NULL OR :landmark = '' OR LOWER(h.landmark) LIKE LOWER(CONCAT('%', :landmark, '%'))) AND " +
           "(:category IS NULL OR :category = '' OR LOWER(h.category) = LOWER(:category)) AND " +
           "(:keyword IS NULL OR :keyword = '' OR " +
           " LOWER(h.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           " LOWER(h.city) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           " LOWER(h.district) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           " LOWER(h.state) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           " LOWER(h.country) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           " LOWER(h.landmark) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           " LOWER(h.address) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Hotel> searchHotels(@Param("country") String country,
                             @Param("state") String state,
                             @Param("district") String district,
                             @Param("city") String city,
                             @Param("landmark") String landmark,
                             @Param("category") String category,
                             @Param("keyword") String keyword);
}
