package com.it.xxhealth.controller;

import com.it.xxhealth.common.Result;
import com.it.xxhealth.dto.SetmealDto;
import com.it.xxhealth.entity.Setmeal;
import com.it.xxhealth.service.SetmealService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/setmeal")
public class SetmealController {

    private final SetmealService setmealService;

    public SetmealController(SetmealService setmealService) {
        this.setmealService = setmealService;
    }

    /**
     * 查询全部套餐（订单模块使用）
     */
    @RequestMapping(value = "/listSetmeal", method = {RequestMethod.GET, RequestMethod.POST})
    public Result<List<Setmeal>> listSetmeal() {
        return Result.success(setmealService.listSetmeal());
    }

    /**
     * 按类型查询套餐（医生端体检套餐页面）
     */
    @PostMapping("/listSetmealByType")
    public Result<List<Setmeal>> listSetmealByType(@RequestBody(required = false) Setmeal setmeal) {
        Integer type = setmeal == null ? null : setmeal.getType();
        return Result.success(setmealService.listSetmealByType(type));
    }

    /**
     * 套餐详情（含检查项）
     */
    @PostMapping("/getSetmealById")
    public Result<SetmealDto> getSetmealById(@RequestBody Setmeal setmeal) {
        if (setmeal == null || setmeal.getSmid() == null) {
            return Result.error(400, "套餐编号不能为空");
        }
        return Result.success(setmealService.getSetmealById(setmeal.getSmid()));
    }
}
