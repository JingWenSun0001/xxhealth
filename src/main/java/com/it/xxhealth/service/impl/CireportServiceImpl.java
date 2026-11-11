package com.it.xxhealth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.it.xxhealth.dto.CiReportDto;
import com.it.xxhealth.entity.Checkitem;
import com.it.xxhealth.entity.Checkitemdetailed;
import com.it.xxhealth.entity.Cidetailedreport;
import com.it.xxhealth.entity.Cireport;
import com.it.xxhealth.entity.Orders;
import com.it.xxhealth.entity.Setmealdetailed;
import com.it.xxhealth.mapper.CheckitemMapper;
import com.it.xxhealth.mapper.CheckitemdetailedMapper;
import com.it.xxhealth.mapper.CidetailedreportMapper;
import com.it.xxhealth.mapper.CireportMapper;
import com.it.xxhealth.mapper.OrdersMapper;
import com.it.xxhealth.mapper.SetmealdetailedMapper;
import com.it.xxhealth.service.CireportService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 报告检查项 Service 实现
 */
@Service
public class CireportServiceImpl extends ServiceImpl<CireportMapper, Cireport> implements CireportService {

    @Resource
    private OrdersMapper ordersMapper;

    @Resource
    private SetmealdetailedMapper setmealdetailedMapper;

    @Resource
    private CheckitemMapper checkitemMapper;

    @Resource
    private CheckitemdetailedMapper checkitemdetailedMapper;

    @Resource
    private CidetailedreportMapper cidetailedreportMapper;

    /**
     * 根据预约订单信息创建体检报告模板。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int createReportTemplate(Orders orders) {
        if (orders == null || orders.getOrderid() == null) {
            return 0;
        }
        Orders order = ordersMapper.selectById(orders.getOrderid());
        if (order == null || order.getSmid() == null) {
            return 0;
        }
        // 同一个预约只生成一次模板，避免重复点击造成重复数据
        Long existsCount = this.baseMapper.selectCount(
                new LambdaQueryWrapper<Cireport>().eq(Cireport::getOrderid, order.getOrderid()));
        if (existsCount != null && existsCount > 0) {
            return 0;
        }
        // 查询套餐下的检查项
        List<Setmealdetailed> setmealItems = setmealdetailedMapper.selectList(
                new LambdaQueryWrapper<Setmealdetailed>().eq(Setmealdetailed::getSmid, order.getSmid()));
        if (setmealItems.isEmpty()) {
            return 0;
        }

        int createdReportCount = 0;
        for (Setmealdetailed setmealItem : setmealItems) {
            if (setmealItem.getCiid() == null) {
                continue;
            }
            Checkitem checkitem = checkitemMapper.selectById(setmealItem.getCiid());
            if (checkitem == null) {
                continue;
            }
            // 1. 写入检查项报告
            Cireport cireport = new Cireport();
            cireport.setCiid(checkitem.getCiid());
            cireport.setCiname(checkitem.getCiname());
            cireport.setOrderid(order.getOrderid());
            this.baseMapper.insert(cireport);
            createdReportCount++;

            // 2. 写入该检查项下的明细模板
            List<Checkitemdetailed> checkitemDetaileds = checkitemdetailedMapper.selectList(
                    new LambdaQueryWrapper<Checkitemdetailed>()
                            .eq(Checkitemdetailed::getCiid, checkitem.getCiid()));
            for (Checkitemdetailed detailed : checkitemDetaileds) {
                Cidetailedreport report = new Cidetailedreport();
                report.setName(detailed.getName());
                report.setUnit(detailed.getUnit());
                report.setMinrange(detailed.getMinrange());
                report.setMaxrange(detailed.getMaxrange());
                report.setNormalvalue(detailed.getNormalvalue());
                report.setNormalvaluestring(detailed.getNormalvaluestring());
                report.setType(detailed.getType());
                // 模板阶段结果为空、默认无异常
                report.setValue("");
                report.setIserror(0);
                // 注意：cidetailedreport.ciId 外键指向 cireport.cirId（检查项报告主键）
                report.setCiid(cireport.getCirid());
                report.setOrderid(order.getOrderid());
                cidetailedreportMapper.insert(report);
            }
        }
        return createdReportCount;
    }

    /**
     * 根据体检预约编号查询检查项列表及明细信息。
     */
    @Override
    public List<CiReportDto> listCiReport(Cireport cireport) {
        if (cireport == null || cireport.getOrderid() == null) {
            return Collections.emptyList();
        }
        List<Cireport> cireports = this.baseMapper.selectList(
                new LambdaQueryWrapper<Cireport>()
                        .eq(Cireport::getOrderid, cireport.getOrderid())
                        .orderByAsc(Cireport::getCirid));
        if (cireports.isEmpty()) {
            return Collections.emptyList();
        }
        // 一次性把明细查出来，再按检查项报告主键分组，避免循环查询
        List<Integer> cirids = cireports.stream().map(Cireport::getCirid).collect(Collectors.toList());
        List<Cidetailedreport> detailedReports = cidetailedreportMapper.selectList(
                new LambdaQueryWrapper<Cidetailedreport>()
                        .in(Cidetailedreport::getCiid, cirids)
                        .eq(Cidetailedreport::getOrderid, cireport.getOrderid())
                        .orderByAsc(Cidetailedreport::getCidrid));
        Map<Integer, List<Cidetailedreport>> detailedMap = detailedReports.stream()
                .collect(Collectors.groupingBy(Cidetailedreport::getCiid));

        List<CiReportDto> result = new ArrayList<>(cireports.size());
        for (Cireport item : cireports) {
            CiReportDto dto = new CiReportDto();
            BeanUtils.copyProperties(item, dto);
            dto.setCidetailedreports(detailedMap.getOrDefault(item.getCirid(), new ArrayList<>()));
            result.add(dto);
        }
        return result;
    }
}
