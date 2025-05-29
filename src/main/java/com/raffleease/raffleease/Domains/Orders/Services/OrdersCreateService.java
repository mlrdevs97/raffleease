package com.raffleease.raffleease.Domains.Orders.Services;

import com.raffleease.raffleease.Domains.Orders.DTOs.AdminOrderCreate;
import com.raffleease.raffleease.Domains.Orders.DTOs.OrderDTO;

public interface OrdersCreateService {
    OrderDTO create(AdminOrderCreate adminOrder, Long associationID);
}
