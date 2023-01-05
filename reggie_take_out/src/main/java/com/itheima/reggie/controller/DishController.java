package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.DishFlavor;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.DishFavorService;
import com.itheima.reggie.service.DishService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 *
 */
@RestController
@RequestMapping("/dish")
public class DishController {
    @Autowired
    private DishService dishService;
    @Autowired
    private DishFavorService dishFavorService;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 添加菜品
     * Http://localhost:8080/dish
     * 添加菜品到菜品表
     * 添加口味到口味表
     * @param dishDto
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody DishDto dishDto){
        dishService.saveWithFlavor(dishDto);
        String keys = "dish_"+dishDto.getCategoryId()+"_1";
        redisTemplate.delete(keys);
        return R.success("新建菜品成功");
    }

    /**
     * 分页查询:查询结果缺一个菜品分类
     * 解决：将返回值类型Dish更改为DishDto，在DishDto中添加菜品分类名
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page,int pageSize,String name){
        
        //分页构造器对象
        Page<Dish> pageInfo=new Page<>(page,pageSize);
        Page<DishDto> dtoPage=new Page<>();
        //条件构造器
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(name!=null,Dish::getName,name);
        queryWrapper.orderByDesc(Dish::getUpdateTime);
        //select * from dish where name=? order by updateTime;
        dishService.page(pageInfo, queryWrapper);

        //对象拷贝
        BeanUtils.copyProperties(pageInfo,dtoPage,"records");

        //获取信息dishes信息
        List<Dish> records = pageInfo.getRecords();
        //遍历Dish的records，在里边添加相应的菜品名之后交赋值给DishDto的records
        List<DishDto> dishDtoRecords=records.stream().map((item) ->{
            DishDto dishDto=new DishDto();

            BeanUtils.copyProperties(item,dishDto);
            //找出菜品类的id
            Long categoryId = item.getCategoryId();
            //根据id在分类表中查找对象
            Category category = categoryService.getById(categoryId);
            //根据对象找菜品名
            if (category!=null) {
                String name1 = category.getName();
                dishDto.setCategoryName(name1);
            }
            return dishDto;
        }).collect(Collectors.toList());
        dtoPage.setRecords(dishDtoRecords);

        return R.success(dtoPage);
    }

    /**
     * http://localhost:8080/dish/1568438207966654466   GET
     * 修改菜品回显数据
     * @param id
     * @return
     */
    @GetMapping("{id}")
    public R<DishDto> get(@PathVariable Long id){
        DishDto dishDto = dishService.getWithFlavor(id);
        return R.success(dishDto);
    }

    /**修改菜品信息
     * http://localhost:8080/dish
     * id: "1568438207966654466", name: "222", categoryId: "1413384954989060097", price: 200, code: "",…}
     * flavors:{...}
     * @param dishDto
     * @return
     */
    @PutMapping
    public R<String> update(@RequestBody DishDto dishDto){
        dishService.updateWithFlavor(dishDto);
        String keys="dish_"+dishDto.getCategoryId()+"_1";
        redisTemplate.delete(keys);
        return R.success("菜品修改成功");
    }





    /**
     * 菜品停售
     * http://localhost:8080/dish/status/0?ids=1568438031571070978,1568436446451273730 POST
     */
    @PostMapping("/status/{status}")
    public R<String> statusS(@PathVariable Integer status,Long[] ids){
        LambdaUpdateWrapper<Dish> updateWrapper=new LambdaUpdateWrapper<>();
        updateWrapper.set(Dish::getStatus,status).in(Dish::getId,ids);
        dishService.update(updateWrapper);
        Set keys = redisTemplate.keys("dish_*");
        redisTemplate.delete(keys);
        return R.success("批量修改菜品状态成功");
    }







    /**
     * 批量删除
     * http://localhost:8080/dish?ids=1568438031571070978,1568436446451273730
     */
    @DeleteMapping
    public R<String> deletes(Long[] ids){
        dishService.removeWithFlavor(ids);
        return R.success("删除菜品成功");
    }


    /**
     * 添加套餐时，添加菜品，根据菜品id回显菜品的详细信息
     * http://localhost:8080/dish/list?categoryId=1397844263642378242   GET
     */
/*    @GetMapping("/list")
    public R<List<Dish>> get(Dish dish){
        LambdaQueryWrapper<Dish> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(Dish::getCategoryId,dish.getCategoryId());
        queryWrapper.eq(Dish::getStatus,1);
        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);
        List<Dish> list = dishService.list(queryWrapper);
        return R.success(list);
    }*/
    @GetMapping("/list")
    public R<List<DishDto>> get(Dish dish){
        List<DishDto> dishDtoList=null;
        //动态构造key
        String key="dish_"+dish.getCategoryId()+"_"+dish.getStatus();
        //获取redis中数据
        dishDtoList  = (List<DishDto>) redisTemplate.opsForValue().get(key);
        if (dishDtoList!=null){
            //从Redis中取出成功
            return R.success(dishDtoList);
        }


        //没有从redis中获取成功，要从数据库查询
        LambdaQueryWrapper<Dish> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(Dish::getCategoryId,dish.getCategoryId());
        queryWrapper.eq(Dish::getStatus,1);
        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);
        List<Dish> list = dishService.list(queryWrapper);


        //把口味添加到dishDto，回显
        dishDtoList=list.stream().map((item)->{
            DishDto dishDto=new DishDto();
            Long categoryId = item.getCategoryId();
            Category category = categoryService.getById(categoryId);
            if (category != null) {
                String name = category.getName();
                dishDto.setCategoryName(name);
            }
            BeanUtils.copyProperties(item,dishDto);
            //当前菜品id
            Long id = item.getId();
            LambdaQueryWrapper<DishFlavor> wrapper=new LambdaQueryWrapper<>();
            wrapper.eq(DishFlavor::getDishId,id);
            List<DishFlavor> dishFlavors = dishFavorService.list(wrapper);
            dishDto.setFlavors(dishFlavors);
            return dishDto;
        }).collect(Collectors.toList());
        //从数据库中查询成功，将返回值dishDtoList存入Redis
        redisTemplate.opsForValue().set(key,dishDtoList,60, TimeUnit.MINUTES);


        return R.success(dishDtoList);
    }
}
