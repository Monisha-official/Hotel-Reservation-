package com.hotelease.dto;

import java.util.List;
import java.util.Map;

public class AdminDashboardDTO {
    private long totalBookings;
    private long todayCheckinsCount;
    private long todayCheckoutsCount;
    private long availableRoomsCount;
    private long occupiedRoomsCount;
    private long pendingPaymentsCount;
    private double totalRevenue;
    private long pendingServiceRequestsCount;
    private long urgentComplaintsCount;
    private List<Map<String, Object>> recentBookings;
    private List<Map<String, Object>> urgentServiceRequests;

    public AdminDashboardDTO() {}

    public long getTotalBookings() { return totalBookings; }
    public void setTotalBookings(long totalBookings) { this.totalBookings = totalBookings; }

    public long getTodayCheckinsCount() { return todayCheckinsCount; }
    public void setTodayCheckinsCount(long todayCheckinsCount) { this.todayCheckinsCount = todayCheckinsCount; }

    public long getTodayCheckoutsCount() { return todayCheckoutsCount; }
    public void setTodayCheckoutsCount(long todayCheckoutsCount) { this.todayCheckoutsCount = todayCheckoutsCount; }

    public long getAvailableRoomsCount() { return availableRoomsCount; }
    public void setAvailableRoomsCount(long availableRoomsCount) { this.availableRoomsCount = availableRoomsCount; }

    public long getOccupiedRoomsCount() { return occupiedRoomsCount; }
    public void setOccupiedRoomsCount(long occupiedRoomsCount) { this.occupiedRoomsCount = occupiedRoomsCount; }

    public long getPendingPaymentsCount() { return pendingPaymentsCount; }
    public void setPendingPaymentsCount(long pendingPaymentsCount) { this.pendingPaymentsCount = pendingPaymentsCount; }

    public double getTotalRevenue() { return totalRevenue; }
    public void setTotalRevenue(double totalRevenue) { this.totalRevenue = totalRevenue; }

    public long getPendingServiceRequestsCount() { return pendingServiceRequestsCount; }
    public void setPendingServiceRequestsCount(long pendingServiceRequestsCount) { this.pendingServiceRequestsCount = pendingServiceRequestsCount; }

    public long getUrgentComplaintsCount() { return urgentComplaintsCount; }
    public void setUrgentComplaintsCount(long urgentComplaintsCount) { this.urgentComplaintsCount = urgentComplaintsCount; }

    public List<Map<String, Object>> getRecentBookings() { return recentBookings; }
    public void setRecentBookings(List<Map<String, Object>> recentBookings) { this.recentBookings = recentBookings; }

    public List<Map<String, Object>> getUrgentServiceRequests() { return urgentServiceRequests; }
    public void setUrgentServiceRequests(List<Map<String, Object>> urgentServiceRequests) { this.urgentServiceRequests = urgentServiceRequests; }
}
