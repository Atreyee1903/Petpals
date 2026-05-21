package org.petpals.service;

import org.petpals.dto.ActivityLogDTO;
import org.petpals.dto.AnalyticsDTO;
import org.petpals.dto.UserDTO;
import org.petpals.model.ActivityLog;
import org.petpals.model.BlockedUser;
import org.petpals.model.Order;
import org.petpals.model.User;
import org.petpals.repository.ActivityLogRepository;
import org.petpals.repository.BlockedUserRepository;
import org.petpals.repository.OrderRepository;
import org.petpals.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class AdminService {

    private final UserRepository userRepository;
    private final BlockedUserRepository blockedUserRepository;
    private final OrderRepository orderRepository;
    private final ActivityLogRepository activityLogRepository;
    private final UserActivityService activityService;

    public AdminService(UserRepository userRepository, BlockedUserRepository blockedUserRepository, 
                       OrderRepository orderRepository, ActivityLogRepository activityLogRepository,
                       UserActivityService activityService) {
        this.userRepository = userRepository;
        this.blockedUserRepository = blockedUserRepository;
        this.orderRepository = orderRepository;
        this.activityLogRepository = activityLogRepository;
        this.activityService = activityService;
    }

    public Page<UserDTO> getAllUsers(Pageable pageable) {
        Page<User> users = userRepository.findAll(pageable);
        return new PageImpl<>(convertUsersToDTOs(users.getContent()), pageable, users.getTotalElements());
    }

    public Page<UserDTO> searchUsers(String searchTerm, Pageable pageable) {
        Page<User> users = userRepository.findAll(pageable);
        List<User> filtered = users.getContent().stream()
            .filter(u -> u.getUsername().contains(searchTerm) || 
                        u.getEmail().contains(searchTerm) ||
                        u.getFullName().contains(searchTerm))
            .collect(Collectors.toList());
        return new PageImpl<>(convertUsersToDTOs(filtered), pageable, filtered.size());
    }

    public UserDTO getUserById(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new InvalidOperationException("User not found: " + userId));
        return convertUserToDTO(user);
    }

    public void updateUser(Long userId, String fullName, String email, String phone, String address) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new InvalidOperationException("User not found: " + userId));
        
        user.setFullName(fullName);
        user.setEmail(email);
        user.setPhone(phone);
        user.setAddress(address);
        userRepository.save(user);
    }

    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new InvalidOperationException("User not found: " + userId));
        
        if (user.isAdmin()) {
            throw new InvalidOperationException("Cannot delete admin users");
        }
        
        userRepository.delete(user);
    }

    public void blockUser(Long userId, String reason) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new InvalidOperationException("User not found: " + userId));
        
        if (user.isAdmin()) {
            throw new InvalidOperationException("Cannot block admin users");
        }
        
        if (!user.isBlocked()) {
            user.setBlocked(true);
            userRepository.save(user);
        }
        
        BlockedUser blockedUser = new BlockedUser(user, null, reason);
        blockedUserRepository.save(blockedUser);
    }

    public void unblockUser(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new InvalidOperationException("User not found: " + userId));
        
        BlockedUser blockedUser = blockedUserRepository.findActiveBlockByUserId(userId)
            .orElseThrow(() -> new InvalidOperationException("User is not blocked"));
        
        user.setBlocked(false);
        userRepository.save(user);
        
        blockedUser.setActive(false);
        blockedUser.setUnblockedAt(LocalDateTime.now());
        blockedUserRepository.save(blockedUser);
    }

    // Dashboard metrics
    public long getTotalUsers() {
        return userRepository.count();
    }

    public long getTotalOrders() {
        return orderRepository.count();
    }

    public BigDecimal getTotalRevenue() {
        return orderRepository.findAll().stream()
            .map(o -> o.getTotalAmount() != null ? o.getTotalAmount() : BigDecimal.ZERO)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public long getActiveUsersCount() {
        return userRepository.findAll().stream()
            .filter(u -> !u.isBlocked())
            .count();
    }

    public long getBlockedUsersCount() {
        return blockedUserRepository.findAllActive().size();
    }

    public List<ActivityLogDTO> getRecentActivities(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return activityLogRepository.findAll(pageable).getContent().stream()
            .map(this::convertActivityToDTO)
            .collect(Collectors.toList());
    }

    public List<UserDTO> getTopCustomersBySpending(int limit) {
        return userRepository.findAll().stream()
            .map(u -> {
                UserDTO dto = convertUserToDTO(u);
                BigDecimal spent = orderRepository.findByUserId(u.getId()).stream()
                    .map(o -> o.getTotalAmount() != null ? o.getTotalAmount() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
                dto.setTotalSpent(spent.toString());
                return dto;
            })
            .sorted((a, b) -> {
                BigDecimal aSpent = new BigDecimal(a.getTotalSpent() != null ? a.getTotalSpent() : "0");
                BigDecimal bSpent = new BigDecimal(b.getTotalSpent() != null ? b.getTotalSpent() : "0");
                return bSpent.compareTo(aSpent);
            })
            .limit(limit)
            .collect(Collectors.toList());
    }

    public List<ActivityLogDTO> getUserActivities(Long userId, int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return activityLogRepository.findByUserId(userId, pageable).getContent().stream()
            .map(this::convertActivityToDTO)
            .collect(Collectors.toList());
    }

    public List<Order> getUserOrders(Long userId) {
        return orderRepository.findByUserId(userId);
    }

    public Page<ActivityLogDTO> getActivities(Pageable pageable) {
        Page<ActivityLog> activities = activityLogRepository.findAll(pageable);
        return new PageImpl<>(
            activities.getContent().stream().map(this::convertActivityToDTO).collect(Collectors.toList()),
            pageable,
            activities.getTotalElements()
        );
    }

    public Page<UserDTO> getBlockedUsers(Pageable pageable) {
        List<User> blockedUsersList = userRepository.findAll().stream()
            .filter(User::isBlocked)
            .collect(Collectors.toList());
        
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), blockedUsersList.size());
        
        List<UserDTO> pageContent = convertUsersToDTOs(blockedUsersList.subList(start, end));
        return new PageImpl<>(pageContent, pageable, blockedUsersList.size());
    }

    // Analytics
    public AnalyticsDTO getAnalytics() {
        AnalyticsDTO analytics = new AnalyticsDTO();
        analytics.setTotalUsers(getTotalUsers());
        analytics.setTotalOrders(getTotalOrders());
        analytics.setTotalRevenue(getTotalRevenue());
        analytics.setAverageOrderValue(calculateAverageOrderValue());
        analytics.setActiveUsersToday(getActiveUsersCount());
        return analytics;
    }

    public Map<String, Object> getOrdersOverTime() {
        Map<String, Object> chart = new HashMap<>();
        List<String> labels = new ArrayList<>();
        List<Integer> data = new ArrayList<>();
        
        LocalDateTime startDate = LocalDateTime.now().minusDays(30);
        for (int i = 0; i < 30; i++) {
            final int day = i;
            LocalDate targetDate = startDate.plusDays(day).toLocalDate();
            labels.add(targetDate.toString());
            int count = (int) orderRepository.findAll().stream()
                .filter(o -> o.getOrderDate() != null && 
                    o.getOrderDate().toLocalDate().equals(targetDate))
                .count();
            data.add(count);
        }
        
        chart.put("labels", labels);
        chart.put("data", data);
        return chart;
    }

    public Map<String, Object> getRevenueOverTime() {
        Map<String, Object> chart = new HashMap<>();
        List<String> labels = new ArrayList<>();
        List<Double> data = new ArrayList<>();
        
        LocalDateTime startDate = LocalDateTime.now().minusDays(30);
        for (int i = 0; i < 30; i++) {
            final int day = i;
            LocalDate targetDate = startDate.plusDays(day).toLocalDate();
            labels.add(targetDate.toString());
            BigDecimal revenue = orderRepository.findAll().stream()
                .filter(o -> o.getOrderDate() != null && 
                    o.getOrderDate().toLocalDate().equals(targetDate))
                .map(o -> o.getTotalAmount() != null ? o.getTotalAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            data.add(revenue.doubleValue());
        }
        
        chart.put("labels", labels);
        chart.put("data", data);
        return chart;
    }

    public Map<String, Object> getRevenueByCategoryChart() {
        Map<String, Object> chart = new HashMap<>();
        // This would require category data from products
        chart.put("labels", Arrays.asList("Category 1", "Category 2", "Category 3"));
        chart.put("data", Arrays.asList(1200, 800, 600));
        return chart;
    }

    public Map<String, Object> getTopProductsChart() {
        Map<String, Object> chart = new HashMap<>();
        // This would require product sales data
        chart.put("labels", Arrays.asList("Product 1", "Product 2", "Product 3"));
        chart.put("data", Arrays.asList(45, 38, 32));
        return chart;
    }

    // Helper methods
    private BigDecimal calculateAverageOrderValue() {
        List<Order> orders = orderRepository.findAll();
        if (orders.isEmpty()) return BigDecimal.ZERO;
        BigDecimal total = orders.stream()
            .map(o -> o.getTotalAmount() != null ? o.getTotalAmount() : BigDecimal.ZERO)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        return total.divide(new BigDecimal(orders.size()), 2, java.math.RoundingMode.HALF_UP);
    }

    private UserDTO convertUserToDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setFullName(user.getFullName());
        dto.setPhone(user.getPhone());
        dto.setAddress(user.getAddress());
        dto.setAdmin(user.isAdmin());
        dto.setBlocked(user.isBlocked());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setLastLogin(user.getLastLogin());
        dto.setLoginCount(user.getLoginCount());
        
        long ordersCount = orderRepository.countByUserId(user.getId());
        dto.setOrdersCount(ordersCount);
        
        if (ordersCount > 0) {
            BigDecimal totalSpent = orderRepository.findByUserId(user.getId())
                .stream()
                .map(o -> o.getTotalAmount() != null ? o.getTotalAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            dto.setTotalSpent(totalSpent.toString());
        } else {
            dto.setTotalSpent("0.00");
        }
        
        return dto;
    }

    private List<UserDTO> convertUsersToDTOs(List<User> users) {
        return users.stream().map(this::convertUserToDTO).collect(Collectors.toList());
    }

    private ActivityLogDTO convertActivityToDTO(ActivityLog activity) {
        ActivityLogDTO dto = new ActivityLogDTO();
        dto.setId(activity.getId());
        dto.setUserName(activity.getUser().getUsername());
        dto.setAction(activity.getAction());
        dto.setDescription(activity.getDescription());
        dto.setTimestamp(activity.getTimestamp());
        dto.setEntityType(activity.getEntityType());
        dto.setEntityId(activity.getEntityId());
        return dto;
    }
}
