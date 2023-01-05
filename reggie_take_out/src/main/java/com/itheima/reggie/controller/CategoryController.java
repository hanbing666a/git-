package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.CustomException;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.DishService;
import com.itheima.reggie.service.SetmealService;
import com.itheima.reggie.service.impl.CategoryServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 分类管理
 */
@RestController
@RequestMapping("/category")
public class CategoryController {
    @Autowired
    private CategoryService categoryService;

    @Autowired
    private DishService dishService;

    @Autowired
    private SetmealService setmealService;
    @PostMapping
    public R<String> save(@RequestBody Category category){
        //公共字段自动填充
        categoryService.save(category);
        return R.success("新增分类成功");
    }

    /**
     * 分页查询
     * http://localhost:8080/category/page?page=1&pageSize=10  GET
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize){
        //分页构造器
        Page<Category> pageInfo=new Page<>(page,pageSize);
        //条件构造器
        LambdaQueryWrapper<Category> queryWrapper= new LambdaQueryWrapper<Category>();
        queryWrapper.orderByAsc(Category::getSort);
        //执行分页
        categoryService.page(pageInfo,queryWrapper);
        return R.success(pageInfo);
    }

    /**
     * 删除菜品类
     * http://localhost:8080/category?ids=1567797448183689217  DELETE
     * @return
     */
    @DeleteMapping
    public R<String> delete(Long ids){
        LambdaQueryWrapper<Category> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(Category::getId,ids);
        //判断该菜品类和中是否有菜
        //select * from dish where  category_id=?
        LambdaQueryWrapper<Dish> queryWrapperDish=new LambdaQueryWrapper<>();
        queryWrapperDish.eq(Dish::getCategoryId,ids);
        List<Dish> list = dishService.list(queryWrapperDish);
        if (list.size()!=0){
            throw new CustomException("该菜品中含有其他菜，删除失败");
        }
        //delete * from category where ids=?
         categoryService.remove(queryWrapper);
        return R.success("删除成功");
    }

    /**
     * 1. http://localhost:8080/category  PUT
     * {id: "1397844263642378242", name: "湘菜", sort: 1}
     * @param
     * @return
     */
    @PutMapping
    public R<String> update(@RequestBody Category category){
        //update category set name=湘菜，sort=1? where id=1397844263642378242
        categoryService.updateById(category);
        return R.success("修改菜品成功");
    }

    /**
     * 菜品管理添加菜单时请求添加时回显菜品分类数据数据   http://localhost:8080/category/list?type=1   1是菜品  2是套餐
     * 添加套餐时，套餐分类数据回显  http://localhost:8080/category/list?type=2&page=1&pageSize=1000
     * 添加套餐时，添加菜品回显菜品种类
     * 用户登录点击菜品回显http://localhost:8080/category/list
     * @param category
     * @return
     */
    @GetMapping("/list")
    public R<List<Category>> list(Category category){
        //条件构造器
        LambdaQueryWrapper<Category> queryWrapper=new LambdaQueryWrapper<>();
        //添加条件
        queryWrapper.eq(category.getType()!=null,Category::getType,category.getType());
        //添加排序条件
        queryWrapper.orderByAsc(Category::getSort).orderByDesc(Category::getCreateTime);
        List<Category> list = categoryService.list(queryWrapper);
        return R.success(list);
    }


}
