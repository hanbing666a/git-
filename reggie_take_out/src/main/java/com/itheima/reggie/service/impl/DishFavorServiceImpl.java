package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.entity.DishFlavor;
import com.itheima.reggie.mapper.DishFlavorMapper;
import com.itheima.reggie.service.DishFavorService;
import org.springframework.stereotype.Service;

@Service
public class DishFavorServiceImpl extends ServiceImpl<DishFlavorMapper, DishFlavor> implements DishFavorService {
}
