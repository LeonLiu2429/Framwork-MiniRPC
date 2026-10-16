package com.leon.provider.service.impl;

import com.leon.framework.annotation.RpcService;
import com.leon.api.OrderService;

@RpcService(OrderService.class)
public class OrderServiceImpl implements OrderService{
    @Override
    public String getOrderById(Long id) {
        return "订单id：" + id;
    }
}
