package org.petpals.dto;

import java.math.BigDecimal;

public class AnalyticsDTO {
    private Long totalUsers;
    private Long totalOrders;
    private BigDecimal totalRevenue;
    private BigDecimal averageOrderValue;
    private Long newUsersToday;
    private Long newUsersThisMonth;
    private Double orderGrowthRate;
    private Double revenueGrowthRate;
    private Long activeUsersToday;

    public AnalyticsDTO() {}

    public Long getTotalUsers() { return totalUsers; }
    public void setTotalUsers(Long totalUsers) { this.totalUsers = totalUsers; }

    public Long getTotalOrders() { return totalOrders; }
    public void setTotalOrders(Long totalOrders) { this.totalOrders = totalOrders; }

    public BigDecimal getTotalRevenue() { return totalRevenue; }
    public void setTotalRevenue(BigDecimal totalRevenue) { this.totalRevenue = totalRevenue; }

    public BigDecimal getAverageOrderValue() { return averageOrderValue; }
    public void setAverageOrderValue(BigDecimal averageOrderValue) { this.averageOrderValue = averageOrderValue; }

    public Long getNewUsersToday() { return newUsersToday; }
    public void setNewUsersToday(Long newUsersToday) { this.newUsersToday = newUsersToday; }

    public Long getNewUsersThisMonth() { return newUsersThisMonth; }
    public void setNewUsersThisMonth(Long newUsersThisMonth) { this.newUsersThisMonth = newUsersThisMonth; }

    public Double getOrderGrowthRate() { return orderGrowthRate; }
    public void setOrderGrowthRate(Double orderGrowthRate) { this.orderGrowthRate = orderGrowthRate; }

    public Double getRevenueGrowthRate() { return revenueGrowthRate; }
    public void setRevenueGrowthRate(Double revenueGrowthRate) { this.revenueGrowthRate = revenueGrowthRate; }

    public Long getActiveUsersToday() { return activeUsersToday; }
    public void setActiveUsersToday(Long activeUsersToday) { this.activeUsersToday = activeUsersToday; }
}
