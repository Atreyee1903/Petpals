package org.petpals.service;

import org.petpals.model.*;
import org.petpals.repository.CartItemRepository;
import org.petpals.repository.OrderRepository;
import org.petpals.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;

    public OrderService(OrderRepository orderRepository,
                        CartItemRepository cartItemRepository,
                        UserRepository userRepository) {
        this.orderRepository = orderRepository;
        this.cartItemRepository = cartItemRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Order placeOrder(Long userId, String street, String city, String state,
                            String postalCode, String phone, String upiId) {
        User user = userRepository.findById(userId).orElseThrow();
        List<CartItem> cartItems = cartItemRepository.findByUserId(userId);

        if (cartItems.isEmpty()) {
            throw new IllegalStateException("Cart is empty");
        }

        BigDecimal total = cartItems.stream()
                .map(CartItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Order order = new Order();
        order.setUser(user);
        order.setTotalAmount(total);
        order.setShippingStreet(street);
        order.setShippingCity(city);
        order.setShippingState(state);
        order.setShippingPostalCode(postalCode);
        order.setShippingPhone(phone);
        order.setPaymentUpiId(upiId);

        for (CartItem ci : cartItems) {
            OrderItem oi = new OrderItem(ci.getProduct(), ci.getQuantity(), ci.getProduct().getPrice());
            order.addItem(oi);
        }

        Order saved = orderRepository.save(order);
        cartItemRepository.deleteByUserId(userId);
        return saved;
    }

    public List<Order> getOrdersByUser(Long userId) {
        return orderRepository.findByUserIdOrderByOrderDateDesc(userId);
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAllByOrderByOrderDateDesc();
    }

    public Optional<Order> getOrderById(Long id) {
        return orderRepository.findById(id);
    }

    @Transactional
    public boolean updateOrderStatus(Long orderId, String newStatus) {
        Optional<Order> opt = orderRepository.findById(orderId);
        if (opt.isPresent()) {
            opt.get().setStatus(newStatus);
            orderRepository.save(opt.get());
            return true;
        }
        return false;
    }
}

