package com.Skill.Marketplace.SM.Services;
import com.Skill.Marketplace.SM.DTO.OrderDTO.createOrderDTO;
import com.Skill.Marketplace.SM.Entities.*;
import com.Skill.Marketplace.SM.Exception.BadRequestException;
import com.Skill.Marketplace.SM.Exception.ConflictException;
import com.Skill.Marketplace.SM.Exception.ForbiddenException;
import com.Skill.Marketplace.SM.Exception.ResourceNotFoundException;
import com.Skill.Marketplace.SM.Repo.OrderRepo;
import com.Skill.Marketplace.SM.Repo.SkillsRepo;
import com.Skill.Marketplace.SM.Repo.UserRepo;
import com.Skill.Marketplace.SM.Repo.UserSkillRepo;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrderService {

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private SkillsRepo skillsRepo;

    @Autowired
    private UserSkillRepo userSkillRepo;

    @Autowired
    private OrderRepo orderRepo;

    public Order placeOrder(String username , createOrderDTO orderDTO) {

        UserModel consumer = userRepo.getUserByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        UserModel provider = userRepo.findById(orderDTO.getProviderId())
                .orElseThrow(() -> new ResourceNotFoundException("Provider not found"));

        if (consumer.getId().equals(provider.getId())) {
            throw new BadRequestException("You cannot order your own service");
        }

        Skill skill = skillsRepo.findById(orderDTO.getSkillId())
                .orElseThrow(() -> new ResourceNotFoundException("Skill not found"));

        UserSkill listing = userSkillRepo.findByUserAndSkillAndIsActiveTrue(provider, skill)
                .orElseThrow(() -> new BadRequestException("Provider does not offer this skill"));

        if(orderDTO.getEstimatedHours() <= 0) {
            throw new BadRequestException("Estimated hours must be greater than zero");
        }

        Order order = new Order();
        order.setConsumer(consumer);
        order.setProvider(provider);
        order.setSkill(skill);
        order.setDescription(orderDTO.getDescription());
        order.setEstimatedHours(orderDTO.getEstimatedHours());
        order.setAgreedPrice(listing.getRate()*orderDTO.getEstimatedHours());
        order.setStatus(OrderStatus.PAYMENT_PENDING);

        return orderRepo.save(order);
    }


    @Transactional
    public void acceptOrder(Long orderId, String username, LocalDateTime deadline) {
        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        if (!order.getProvider().getUsername().equals(username))
            throw new ForbiddenException("Not authorized");

        if(order.getStatus() != OrderStatus.PENDING)
            throw new ConflictException("Order is not ready for acceptance");

        if(deadline.isBefore(LocalDateTime.now().plusHours(1)))
            throw new BadRequestException("Deadline must be at least 1 hour from now");

        order.setDeadline(deadline);
        order.setStatus(OrderStatus.ACCEPTED);
        orderRepo.save(order);
    }

    @Transactional
    public void deliverOrder(Long orderId, String username) {
        Order order = orderRepo.findById(orderId).orElseThrow();

        if (!order.getProvider().getUsername().equals(username))
            throw new ForbiddenException("Not authorized");

        if(order.getStatus() != OrderStatus.ACCEPTED)
            throw new ConflictException("Order must be accepted first");

        order.setStatus(OrderStatus.DELIVERED);
        order.setCompletedAt(LocalDateTime.now());
        orderRepo.save(order);

    }

    @Transactional
    public void cancelOrder(Long orderId, String username) {
        Order order = orderRepo.findById(orderId)
                .orElseThrow(()-> new ResourceNotFoundException("Order not found"));

        if (!order.getConsumer().getUsername().equals(username))
            throw new ForbiddenException("Not authorized");

        if (order.getStatus() != OrderStatus.PENDING)
            throw new ConflictException("Cannot cancel now");

        order.setStatus(OrderStatus.CANCELLED);

    }

    public List<Order> getMyOrders(String username) {
        return orderRepo.findByConsumer_Username(username);
    }

    public List<Order> getReceivedOrders(String username) {
        return orderRepo.findByProvider_Username(username);
    }



}
