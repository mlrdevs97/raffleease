package com.raffleease.raffleease.Domains.Cart.Mappers.Impls;

import com.raffleease.raffleease.Domains.Cart.DTO.CartDTO;
import com.raffleease.raffleease.Domains.Cart.Mappers.ICartsMapper;
import com.raffleease.raffleease.Domains.Cart.Model.Cart;
import com.raffleease.raffleease.Domains.Tickets.Mappers.ITicketsMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class CartsMapperImpl implements ICartsMapper {
    private final ITicketsMapper ticketsMapper;

    @Override
    public CartDTO fromCart(Cart cart) {
        return CartDTO.builder()
                .cartId(cart.getId())
                .raffleId(cart.getRaffle().getId())
                .status(cart.getStatus())
                .tickets(ticketsMapper.fromTicketList(cart.getTickets()))
                .lastModified(cart.getLastModified())
                .build();
    }
}
