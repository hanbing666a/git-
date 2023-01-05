package com.itheima.reggie.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLIntegrityConstraintViolationException;

@Slf4j
@ResponseBody
@ControllerAdvice(annotations = {RestController.class, Controller.class})  //通知    对标有注解@RestController和@Controller的进行拦截
public class GlobalExceptionHandler {

    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)  //标出要处理的异常
    public  R<String> exceptionHandler(SQLIntegrityConstraintViolationException exception){
        log.info(exception.getMessage());
        String exceptionMessage = exception.getMessage();
        if (exceptionMessage.contains("Duplicate entry")) {
            String[] split = exceptionMessage.split(" ");//使用空白把异常分开    Duplicate entry '111' for key 'employee.idx_username'
            //取出数组中第三个元素即为用户名
            String username = split[2];
            return R.error(username+"已经存在");
        }
        return R.error("未知错误");
    }





    @ExceptionHandler(CustomException.class)  //标出要处理的异常
    public  R<String> exceptionHandler(CustomException exception){
        log.info(exception.getMessage());
        String exceptionMessage = exception.getMessage();

        return R.error(exceptionMessage);
    }

}