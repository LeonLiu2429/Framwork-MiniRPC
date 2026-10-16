package com.leon.provider.service.impl;

import com.leon.framework.annotation.RpcService;
import com.leon.api.ProductService;

@RpcService(ProductService.class)
public class ProductServiceImpl implements ProductService {
    @Override
    public String getById(Long id) {
        return "商品id：" + id;
    }
}
