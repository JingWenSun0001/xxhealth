package com.it.xxhealth.doctor;

import com.it.xxhealth.common.Result;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import java.time.DateTimeException;

@Order(-10)
@RestControllerAdvice(basePackageClasses=DoctorWorkspaceController.class)
public class DoctorWorkspaceExceptionHandler {
    @ExceptionHandler({IllegalArgumentException.class,DateTimeException.class,HttpMessageNotReadableException.class})
    public ResponseEntity<Result<Void>> invalid(Exception e) {
        String message=e instanceof DateTimeException?"日期格式错误，请使用有效日期":e instanceof HttpMessageNotReadableException?"请求数据格式错误":e.getMessage();
        return ResponseEntity.badRequest().body(Result.error(400,message));
    }
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Result<Void>> conflict(Exception e) {
        return ResponseEntity.status(409).body(Result.error(409,"数据重复、字段超长或关联记录不存在，请检查输入"));
    }
}
