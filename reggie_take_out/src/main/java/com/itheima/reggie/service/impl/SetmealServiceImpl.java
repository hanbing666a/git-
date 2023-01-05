package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.common.CustomException;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.entity.Setmeal;
import com.itheima.reggie.entity.SetmealDish;
import com.itheima.reggie.mapper.SetmealMapper;
import com.itheima.reggie.service.SetmealDishService;
import com.itheima.reggie.service.SetmealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService {
    @Autowired
    private SetmealService setmealService;
    @Autowired
    private SetmealDishService setmealDishService;
    @Transactional
    public void saveWithDish(SetmealDto setmealDto) {
        //添加信息到套餐表
        setmealService.save(setmealDto);

        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        setmealDishes = setmealDishes.stream().peek((item) -> {
            Long id = setmealDto.getId();
            item.setSetmealId(id);
        }).collect(Collectors.toList());


        //添加信息到套餐菜品表
        setmealDishService.saveBatch(setmealDishes);
        System.out.println(" ");
    }

    @Transactional
    public void removeWithDish( List<Long> ids) {
        //判断是否停售select count(*) from setmeal where id in (ids) in status=1
        LambdaQueryWrapper<Setmeal> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.in(Setmeal::getId,ids);
        queryWrapper.eq(Setmeal::getStatus,1);
        int count = setmealService.count(queryWrapper);
        if (count>0){
            throw new CustomException("套餐正在售卖，删除失败");
        }
        //删除套餐表
        setmealService.removeByIds(ids);
        //删除套餐菜品表
        LambdaQueryWrapper<SetmealDish>  queryWrapper1=new LambdaQueryWrapper<>();
        queryWrapper1.eq(SetmealDish::getDishId,ids);
        setmealDishService.remove(queryWrapper1);
    }
}
