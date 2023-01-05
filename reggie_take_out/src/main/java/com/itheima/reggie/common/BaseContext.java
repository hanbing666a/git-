package com.itheima.reggie.common;

/**
 * ThreadLocal实现单个线程内的数据共享
 */
public class BaseContext {
    private static final ThreadLocal<Long> threadLocal=new ThreadLocal<Long>();

    /**
     * 存入id
     * @param id
     */
    public static void setCurrentId(Long id){
        threadLocal.set(id);
    }

    public static Long getCurrentId(){
        threadLocal.get();
        return threadLocal.get();
    }

}
