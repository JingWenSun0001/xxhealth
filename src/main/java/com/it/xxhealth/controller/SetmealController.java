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
 * 用户端体检套餐接口，提供套餐筛选和详情查询功能。
 *
 * 创建日期：2026-11-16
 *
 * @author 熙心健康项目组
 * @since 2026-11-16
 */
@RestController
@RequestMapping("/setmeal")
public class SetmealController {

    /**
     * 体检套餐业务服务。
     */
    @Autowired
    private SetmealService setmealService;

    /**
     * 查询全部体检套餐及其检查项目。
     * @return 查询到的业务数据；不存在时按接口约定返回 null 或空集合
     */
    @PostMapping("/listSetmeal")
    public com.it.xxhealth.common.Result<List<Setmeal>> listSetmeal() {
        return com.it.xxhealth.common.Result.success(setmealService.listSetmeal());
    }

    /**
     * 按适用性别查询体检套餐。
     * @param setmeal 体检套餐查询条件
     * @return 符合条件的数据集合
     */
    @PostMapping("/listSetmealByType")
    public List<Setmeal> listSetmealByType(@RequestBody(required = false) Setmeal setmeal) {
        Integer type = setmeal == null ? null : setmeal.getType();
        return setmealService.listSetmealByType(type);
    }

    /**
     * 根据套餐编号查询套餐及检查项目详情。
     * @param setmeal 体检套餐查询条件
     * @return 查询到的业务数据；不存在时按接口约定返回 null 或空集合
     */
    @PostMapping("/getSetmealById")
    public SetmealDto getSetmealById(@RequestBody Setmeal setmeal) {
        if (setmeal == null || setmeal.getSmid() == null) {
            return null;
        }
        return setmealService.getSetmealById(setmeal.getSmid());
    }
}
