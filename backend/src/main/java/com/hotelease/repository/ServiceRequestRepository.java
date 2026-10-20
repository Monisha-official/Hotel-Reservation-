package com.hotelease.repository;

import com.hotelease.model.ServiceRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServiceRequestRepository extends JpaRepository<ServiceRequest, Long> {
    List<ServiceRequest> findByStatusOrderByCreatedAtDesc(String status);
    List<ServiceRequest> findByPriorityOrderByCreatedAtDesc(String priority);
    List<ServiceRequest> findByBookingId(Long bookingId);
    List<ServiceRequest> findAllByOrderByCreatedAtDesc();
}
