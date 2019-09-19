package com.drcnet.highway.controller.advice;


import com.drcnet.highway.constants.TipsConsts;
import com.drcnet.highway.exception.MyException;
import com.drcnet.highway.exception.TimeOutException;
import com.drcnet.highway.util.Result;
import com.drcnet.response.exception.UnAuthenticationException;
import com.drcnet.response.exception.UnAuthorizedException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Created by ml on 2018/11/23.
 */
@RestControllerAdvice
@Slf4j
public class ControllerAdvice {


    /**
     * 登陆验证的异常
     */
    @ExceptionHandler(value = UnAuthenticationException.class)
    @ResponseStatus(HttpStatus.OK)
    public Result unAuthenticationExceptionHandler(UnAuthenticationException e) {
        return Result.error(401, TipsConsts.UNAUTHENTICATION);
    }

    /**
     * 权限验证的异常
     */
    @ExceptionHandler(value = UnAuthorizedException.class)
    @ResponseStatus(HttpStatus.OK)
    public Result unAuthorizedExceptionHandler(UnAuthorizedException e) {
        return Result.error(409, TipsConsts.UNAUTHORIZED);
    }

    /**
     * BindingResult的异常
     *
     * @param e
     * @return
     */
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.OK)
    public Result methodArgumentNotValidException(MethodArgumentNotValidException e) {
        return Result.error(e.getBindingResult().getAllErrors().get(0).getDefaultMessage());
    }

    /**
     * BindException
     *
     * @param e
     * @return
     */
    @ExceptionHandler(value = BindException.class)
    @ResponseStatus(HttpStatus.OK)
    public Result bindException(BindException e) {
        return Result.error(e.getBindingResult().getAllErrors().get(0).getDefaultMessage());
    }

    /**
     * BindException
     *
     * @param e
     * @return
     */
    @ExceptionHandler(value = MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.OK)
    public Result missingServletRequestParameterException(MissingServletRequestParameterException e) {
        log.error("{}", e);
        return Result.error("请求参数不完整:" + e.getParameterName());
    }

    /**
     * 请求超时的异常
     *
     * @param e
     * @return
     */
    @ExceptionHandler(value = TimeOutException.class)
    @ResponseStatus(HttpStatus.OK)
    public Result timeOutException(TimeOutException e) {
        if (StringUtils.isEmpty(e.getMessage()))
            return Result.error(TipsConsts.REQUEST_TIME_OUT);
        return Result.error(e.getMessage());
    }

    @ExceptionHandler(value = MyException.class)
    @ResponseStatus(HttpStatus.OK)
    public Result myException(MyException e) {
        if (StringUtils.isEmpty(e.getMessage()))
            return Result.error(TipsConsts.SERVER_ERROR);
        return Result.error(e.getMessage());
    }

    /**
     * 总异常拦截
     *
     * @param e
     * @return
     */
    @ExceptionHandler(value = Exception.class)
    @ResponseStatus(HttpStatus.OK)
    public Result exception(Exception e) {
        log.error("{}", e);
        return Result.error(TipsConsts.SERVER_ERROR);
    }
}
