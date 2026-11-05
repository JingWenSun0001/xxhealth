package com.it.xxhealth.controller;

import com.it.xxhealth.common.Result;
import com.it.xxhealth.entity.Setmeal;
import com.it.xxhealth.service.SetmealService;
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

    @RequestMapping(value = "/listSetmeal", method = {RequestMethod.GET, RequestMethod.POST})
    public Result<List<Setmeal>> listSetmeal() {
        return Result.success(setmealService.listSetmeal());
    }
}
