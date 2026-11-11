package com.it.xxhealth.controller;

import com.it.xxhealth.dto.SetmealDto;
import com.it.xxhealth.entity.Setmeal;
import com.it.xxhealth.service.SetmealService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户端 - 体检套餐接口（列表 / 详情）
 * <p>
 * 按文档口径：成功返回裸对象，失败返回 null 或空集合，不再包 Result。
 * 完整路径带 context-path：/tijian/setmeal/...
 */
@RestController
@RequestMapping("/setmeal")
public class SetmealController {

    @Autowired
    private SetmealService setmealService;

    @PostMapping("/listSetmeal")
    public com.it.xxhealth.common.Result<List<Setmeal>> listSetmeal() {
        return com.it.xxhealth.common.Result.success(setmealService.listSetmeal());
    }

    /**
     * 按类型查套餐：type 传 1 男士 / 0 女士，传 null 查全部
     */
    @PostMapping("/listSetmealByType")
    public List<Setmeal> listSetmealByType(@RequestBody(required = false) Setmeal setmeal) {
        Integer type = setmeal == null ? null : setmeal.getType();
        return setmealService.listSetmealByType(type);
    }

    /**
     * 套餐详情：返回套餐信息 + items 检查项列表，套餐不存在返回 null
     */
    @PostMapping("/getSetmealById")
    public SetmealDto getSetmealById(@RequestBody Setmeal setmeal) {
        if (setmeal == null || setmeal.getSmid() == null) {
            return null;
        }
        return setmealService.getSetmealById(setmeal.getSmid());
    }
}
