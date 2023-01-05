package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.Employee;
import com.itheima.reggie.service.EmployeeService;
import com.itheima.reggie.service.impl.EmployeeServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import sun.dc.pr.PRError;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

/**
 * 员工管理
 */
@Slf4j
@RestController
@RequestMapping("/employee")
public class EmployeeController {
    @Autowired
    private EmployeeService employeeService;


    /**
     * 员工登录  通过返回给前端 （ R.error和R.success）  返回r   r中含有code来判断是否跳转登录页面
     * @param request
     * @param employee
     * @return
     */
    @PostMapping("/login")
    public R<Employee> login(HttpServletRequest request, @RequestBody Employee employee){
        //1、从页面获取的密码进行md5加密处理
        String password = DigestUtils.md5DigestAsHex(employee.getPassword().getBytes());

        //2、根据页面提交的用户名查询数据库
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        // select * from where name =？  如果有  返回结果到queryWrapper
        queryWrapper.eq(Employee::getUsername,employee.getUsername());
        Employee emp = employeeService.getOne(queryWrapper);

        //3、如果没有查询到用户名
        if (emp==null){
            return R.error("登陆失败，用户名不存在");
        }

        //4、密码比对
        if (!emp.getPassword().equals(password)){
            return R.error("登陆失败，密码输入错误");
        }

        //5、查看与员工状态
        if(emp.getStatus()==0){
            return R.error("登陆失败，您的账号已冻结");
        }
        //6、登陆成功，将员工id存入session
        request.getSession().setAttribute("employee",emp.getId());
        return  R.success(emp);
    }


    /**
     * 员工退出登录   单击退出图片  前段发送给请求到logout
     * @param request
     * @return
     */
    @PostMapping("/logout")
    public R<String> logout(HttpServletRequest request){
        request.getSession().removeAttribute("employee");
        return R.success("退出成功");
    }

    /**
     * 新增员工
     * 用户名重复，用户名在数据库中的设计是Unique ，是唯一的，此处加了一个全局异常处理
     *  http://localhost:8080/employee
     *  POST
     *  {name: "1111", phone: "15565049009", sex: "1", idNumber: "411422200008183918", username: "333"}
     * @param request
     * @param employee
     * @return
     */
    @PostMapping
    public R<String> save(HttpServletRequest request,@RequestBody Employee employee){
        employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));
//        Long employeeBefore = (Long) request.getSession().getAttribute("employee");
//        employee.setCreateTime(LocalDateTime.now());
//        employee.setUpdateTime(LocalDateTime.now());
//        employee.setCreateUser(employeeBefore);
//        employee.setUpdateUser(employeeBefore);
        log.info("新增人员信息："+employee.toString());
        employeeService.save(employee);
        return R.success("新增员工成功");
    }

    /**
     * 分页+模糊查询
     * http://localhost:8080/employee/page?page=1&pageSize=10&name=11
     * GET
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page,int pageSize,String name){ //前端需要page，所以泛型是MP里的page   返回R<Page>
        log.info("page={},pageSize={},name={}",page,pageSize,name);
        //1、构造分页构造器
        Page pageInfo = new Page(page, pageSize);
        //2、构造条件构造器
        LambdaQueryWrapper<Employee> lambdaQueryWrapper=new LambdaQueryWrapper<Employee>();
        //3、添加模糊查询条件
        lambdaQueryWrapper.like(!StringUtils.isEmpty(name),Employee::getName,name);//当name不为空的时候，查找名字是name的employee对象
        //4、添加排序条件
        lambdaQueryWrapper.orderByDesc(Employee::getUpdateTime);
        //5、执行查询
        employeeService.page(pageInfo,lambdaQueryWrapper);
        return R.success(pageInfo);
    }

    /**
     * 1、修改员工的状态
     * http://localhost:8080/employee
     * {id: "1568102118538121218", status: 0}
     * 前端请求发送的员工的id和数据库中的员工id不同
     * 原因是js在处理long类型数据发生精度损失，
     * 处理方案：在mvc框架中，将默认的消息转换器改成自定义的消息转换器
     * 即将long类型的数响应给前端的时候都转化成String类型
     * 2、修改员工信息
     * @param request
     * @param employee
     * @return
     */
    @PutMapping
    public R<String> update(HttpServletRequest request,@RequestBody Employee employee){
//       Long empID= (Long) request.getSession().getAttribute("employee");
//       employee.setUpdateUser(empID);
//       employee.setUpdateTime(LocalDateTime.now());
//        long id = Thread.currentThread().getId();//
//        log.info("当前线程id{}",id);

        //自动填充字段
        //update status=? from emp where id=?
        employeeService.updateById(employee);

        return R.success("修改状态成功");
    }

    /**
     * 编辑员工信息
     * 1、根据员工的id查询用户信息响应前端
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public  R<Employee> getById(@PathVariable long id){
        Employee employee = employeeService.getById(id);
        return R.success(employee);
    }


}
