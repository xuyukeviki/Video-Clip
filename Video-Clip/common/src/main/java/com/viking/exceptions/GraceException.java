package com.viking.exceptions;

import com.viking.grace.result.ResponseStatusEnum;

/**
 * 优雅的处理异常，统一封装
 * 当调用dispaly方法时会抛出指定的自定义异常(MyCustomException)，然后被GraceExceptionHandler捕获
 * GraceExceptionHandler捕获到特定异常就会执行指定的方法，通过调用GraceJSONResult.exception()返回GraceJSONResult的形式
 */
public class GraceException {

    public static void display(ResponseStatusEnum responseStatusEnum) {
        throw new MyCustomException(responseStatusEnum);
    }

}
