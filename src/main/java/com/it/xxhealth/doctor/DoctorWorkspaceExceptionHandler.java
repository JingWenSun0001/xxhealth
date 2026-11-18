package com.it.xxhealth.doctor;

import com.it.xxhealth.common.Result;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import java.time.DateTimeException;

/**
 * 医生工作台异常处理器，将参数错误和数据冲突映射为对应 HTTP 状态。
 *
 * 创建日期：2026-11-16
 *
 * @author 熙心健康项目组
 * @since 2026-11-16
 */
@Order(-10)
@RestControllerAdvice(basePackageClasses=DoctorWorkspaceController.class)
public class DoctorWorkspaceExceptionHandler {
    /**
     * 处理请求参数、日期或 JSON 格式错误。
     * @param e 捕获到的异常
     * @return 方法处理结果
     */
    @ExceptionHandler({IllegalArgumentException.class,DateTimeException.class,HttpMessageNotReadableException.class})
    public ResponseEntity<Result<Void>> invalid(Exception e) {
        String message=e instanceof DateTimeException?"日期格式错误，请使用有效日期":e instanceof HttpMessageNotReadableException?"请求数据格式错误":e.getMessage();
        return ResponseEntity.badRequest().body(Result.error(400,message));
    }
    /**
     * 处理唯一约束、字段长度或关联关系冲突。
     * @param e 捕获到的异常
     * @return 方法处理结果
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Result<Void>> conflict(Exception e) {
        return ResponseEntity.status(409).body(Result.error(409,"数据重复、字段超长或关联记录不存在，请检查输入"));
    }
}
