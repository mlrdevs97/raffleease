package com.raffleease.raffleease.Domains.Carts.Mappers.Impls;

import com.raffleease.raffleease.Domains.Carts.DTO.CartDTO;
import com.raffleease.raffleease.Domains.Carts.Mappers.ICartsMapper;
import com.raffleease.raffleease.Domains.Carts.Model.Cart;
import com.raffleease.raffleease.Domains.Tickets.Mappers.TicketsMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class CartsMapperImpl implements ICartsMapper {
    private final TicketsMapper ticketsMapper;

    @Override
    public CartDTO fromCart(Cart cart) {
        return CartDTO.builder()
                .cartId(cart.getId())
                .raffleId(cart.getRaffle().getId())
                .status(cart.getStatus())
                .tickets(ticketsMapper.fromTicketList(cart.getTickets()))
                .lastModified(cart.getUpdatedAt())
                .build();
    }
}
