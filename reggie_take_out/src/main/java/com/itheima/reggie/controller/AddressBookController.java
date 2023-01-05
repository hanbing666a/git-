package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.AddressBook;
import com.itheima.reggie.service.AddressBookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
// 我的

/**
 * 业务
 */
@RestController
@RequestMapping("/addressBook")
public class AddressBookController {
    @Autowired
    private AddressBookService addressBookService;

    /**
     * http://localhost:8080/addressBook POST
     * {consignee: "苏寒冰", phone: "15565049009", sex: "1", detail: "南阳理工学院14号公寓楼", label: "学校"}
     * 新增信息
     */
    @PostMapping
    public R<String> add(@RequestBody AddressBook addressBook){
        addressBook.setUserId(BaseContext.getCurrentId());
        LambdaQueryWrapper<AddressBook> queryWrapper=new LambdaQueryWrapper<>();
        addressBookService.save(addressBook);
        return R.success("添加地址信息成功");
    }

    /**
     * http://localhost:8080/addressBook/list  GET
     * 获取地址信息列表
     */
    @GetMapping("/list")
    public R<List<AddressBook>> list(){
        LambdaQueryWrapper<AddressBook> queryWrapper=new LambdaQueryWrapper<>();
        Long currentId = BaseContext.getCurrentId();
        queryWrapper.eq(AddressBook::getUserId,currentId);
        List<AddressBook> list = addressBookService.list(queryWrapper);
        return R.success(list);
    }
    /**
     * http://localhost:8080/addressBook/default PUT
     * {id: "1569238150436384770"}
     * 设置默认地址
     */
    @PutMapping("/default")
    public R<AddressBook> setDefault(@RequestBody AddressBook addressBook){
        LambdaUpdateWrapper<AddressBook> updateWrapper=new LambdaUpdateWrapper<>();
        updateWrapper.eq(AddressBook::getUserId,BaseContext.getCurrentId()).set(AddressBook::getIsDefault,0);
        addressBookService.update(updateWrapper);

        addressBook.setIsDefault(1);
        addressBookService.updateById(addressBook);
        return R.success(addressBook);
    }
    /**
     * http://localhost:8080/addressBook/default  GET
     */
    @GetMapping("/default")
    public R<AddressBook> getDefault(){
        Long currentId = BaseContext.getCurrentId();
        LambdaQueryWrapper<AddressBook> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(AddressBook::getUserId,currentId);
        queryWrapper.eq(AddressBook::getIsDefault,1);
        //select * from address_book where user_id=? and isDefault=1
        AddressBook addressBook = addressBookService.getOne(queryWrapper);
        return R.success(addressBook);

    }
}
