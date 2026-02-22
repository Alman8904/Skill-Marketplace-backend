package com.Skill.Marketplace.SM.Services;

import com.Skill.Marketplace.SM.Entities.Order;
import com.Skill.Marketplace.SM.Entities.OrderStatus;
import com.Skill.Marketplace.SM.Entities.PaymentStatus;
import com.Skill.Marketplace.SM.Repo.OrderRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class OrderExpiryScheduler {

    @Autowired
    private OrderRepo orderRepo;

    @Autowired
    private MockPaymentService mockPaymentService;

    @Scheduled(fixedRate = 3600000) //hourly
    @Transactional
    public void handleExpiredOrders() {
        List<Order> all = orderRepo.findAll();
        LocalDateTime now = LocalDateTime.now();

        for (Order order : all) {

            // Rule 1: Provider accepted but missed their own deadline → refund consumer
            boolean providerMissedDeadline =
                    (order.getStatus() == OrderStatus.ACCEPTED || order.getStatus() == OrderStatus.IN_PROGRESS)
                            && order.getDeadline() != null
                            && order.getDeadline().isBefore(now)
                            && order.getMockPaymentStatus() == PaymentStatus.AUTHORIZED;

            if (providerMissedDeadline) {
                mockPaymentService.refundPayment(order.getOrderId());
                order.setStatus(OrderStatus.CANCELLED);
                order.setCompletedAt(now);
                orderRepo.save(order);
                System.out.println("[Scheduler] Order " + order.getOrderId()
                        + " auto-cancelled — provider missed deadline. Consumer refunded.");
            }

            // Rule 2: Delivered but consumer hasn't approved in 3 days → pay provider
            boolean consumerGhosted =
                    order.getStatus() == OrderStatus.DELIVERED
                            && order.getDeliveredAt() != null
                            && order.getDeliveredAt().isBefore(now.minusDays(3))
                            && order.getMockPaymentStatus() == PaymentStatus.AUTHORIZED;

            if (consumerGhosted) {
                mockPaymentService.capturePayment(order.getOrderId());
                order.setStatus(OrderStatus.COMPLETED);
                order.setApprovedAt(now);
                order.setCompletedAt(now);
                orderRepo.save(order);
                System.out.println("[Scheduler] Order " + order.getOrderId()
                        + " auto-completed — consumer didn't approve in 3 days. Provider paid.");
            }
        }
    }
}