package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.DishFlavor;
import com.itheima.reggie.entity.Employee;
import com.itheima.reggie.mapper.DishMapper;
import com.itheima.reggie.service.DishFavorService;
import com.itheima.reggie.service.DishService;
import com.itheima.reggie.service.EmployeeService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {
    @Autowired
    private DishService dishService;
    @Autowired
    private DishFavorService dishFavorService;
    @Autowired
    private EmployeeService employeeService;

    @Transactional
    public void saveWithFlavor(DishDto dishDto) {
        //保存添加菜品的基本信息
        dishService.save(dishDto);
        Long dishDtoId = dishDto.getId();
        List<DishFlavor> flavors = dishDto.getFlavors();
        //遍历口味，给遍历出的口味添加上菜品id
        flavors = flavors.stream().map((item) -> {
            item.setDishId(dishDtoId);
            return item;
        }).collect(Collectors.toList());
        //保存添加菜品口味到菜品口味表中
        //Insert into dish_flavors dto=?
        dishFavorService.saveBatch(flavors);
    }

    @Override
    @Transactional
    public DishDto getWithFlavor(Long id) {
        Dish dish = this.getById(id);
        DishDto dishDto=new DishDto();
        BeanUtils.copyProperties(dish,dishDto);
        LambdaQueryWrapper<DishFlavor> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId,dish.getId());
        //select * from dish_flavor where dishID=?
        List<DishFlavor> flavors = dishFavorService.list(queryWrapper);
        dishDto.setFlavors(flavors);
        return dishDto;
    }

    @Override
    @Transactional
    public void updateWithFlavor(DishDto dishDto) {
        //修改菜品表
        dishService.updateById(dishDto);
        //修改菜品口味表
        //删除原来了，再添加
        LambdaQueryWrapper<DishFlavor> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId,dishDto.getId());
        //delete * from dish_flavor where dish_id=?
        dishFavorService.remove(queryWrapper);

        //添加当前的口味数据
        List<DishFlavor> flavors = dishDto.getFlavors();
        flavors.stream().map((item)->{
            item.setDishId(dishDto.getId());
            return item;
        }).collect(Collectors.toList());
        dishFavorService.saveBatch(flavors);
    }
    @Transactional
    @Override
    public void removeWithFlavor(Long[] ids) {

        for (Long id:
             ids) {
            //删除多个菜品的菜品信息
            //delete * from dish where id=?
            dishService.removeById(id);

            LambdaQueryWrapper<DishFlavor> queryWrapper=new LambdaQueryWrapper<>();
            queryWrapper.eq(DishFlavor::getDishId,id);
            //删除多个菜品的口味信息
            //delete * from dish_flavor where id=?
            dishFavorService.remove(queryWrapper);
        }




    }




}
