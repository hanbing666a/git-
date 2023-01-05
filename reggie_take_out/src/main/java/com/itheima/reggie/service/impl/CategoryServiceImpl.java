package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.common.CustomException;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.Setmeal;
import com.itheima.reggie.mapper.CategoryMapper;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.DishService;
import com.itheima.reggie.service.SetmealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.function.Function;
@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper,Category> implements CategoryService {
    @Autowired
    private DishService dishService;
    @Autowired
    private SetmealService setmealService;

    /**
     * 根据菜品id删除
     * 并且菜品中没有菜
     * 并且当前分类没有关联套餐
     * @return
     */
    @Override
    public void remove(Long ids) {
        //查看是否有菜属于这个菜品
        //条件构造器
        LambdaQueryWrapper<Dish> dishLambdaQueryWrapper=new LambdaQueryWrapper<>();
        //添加查询条件
        dishLambdaQueryWrapper.eq(Dish::getCategoryId,ids);
        //该菜品中菜的数量
        int count = dishService.count(dishLambdaQueryWrapper);
        //如果有菜品，抛出异常
        if (count>0){
            //抛异常
            throw new CustomException("该菜品和菜有关联,不能删除");
        }


        //查看套餐中是否有该菜品
        //条件构造器构造
        LambdaQueryWrapper<Setmeal>  setmealLambdaQueryWrapper=new LambdaQueryWrapper<>();
        //添加构造条件
        setmealLambdaQueryWrapper.eq(Setmeal::getCategoryId,ids);
        //含有该菜品的套餐的数量
        int count1 = setmealService.count(setmealLambdaQueryWrapper);
        //如果有抛出异常
        if (count1>0){
            //抛出异常
            throw new CustomException("该菜品和套餐有关联，不能删除");
        }

        //该菜品没有和菜和套餐关联，正常删除
        removeById(ids);
    }
}
