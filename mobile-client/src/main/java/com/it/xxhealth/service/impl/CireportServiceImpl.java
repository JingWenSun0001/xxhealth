package com.it.xxhealth.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.it.xxhealth.dto.CiReportDto;
import com.it.xxhealth.entity.Cidetailedreport;
import com.it.xxhealth.entity.Cireport;
import com.it.xxhealth.mapper.CidetailedreportMapper;
import com.it.xxhealth.mapper.CireportMapper;
import com.it.xxhealth.service.CireportService;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 报告检查项 Service 实现
 */
@Service
public class CireportServiceImpl extends ServiceImpl<CireportMapper, Cireport> implements CireportService {

    @Autowired
    private CidetailedreportMapper cidetailedreportMapper;

    @Override
    public List<CiReportDto> listByOrderId(Integer orderid) {
        List<Cireport> list = this.list(Wrappers.<Cireport>lambdaQuery()
                .eq(Cireport::getOrderid, orderid)
                .orderByAsc(Cireport::getCirid));

        List<Cidetailedreport> details = cidetailedreportMapper.selectList(
                Wrappers.<Cidetailedreport>lambdaQuery()
                        .eq(Cidetailedreport::getOrderid, orderid)
                        .orderByAsc(Cidetailedreport::getCidrid));

        Map<Integer, List<Cidetailedreport>> group = details.stream()
                .collect(Collectors.groupingBy(Cidetailedreport::getCiid));

        List<CiReportDto> result = new ArrayList<>();
        for (Cireport c : list) {
            CiReportDto dto = new CiReportDto();
            dto.setCirid(c.getCirid());
            dto.setCiid(c.getCiid());
            dto.setCiname(c.getCiname());
            dto.setOrderid(c.getOrderid());
            dto.setDetails(group.getOrDefault(c.getCirid(), new ArrayList<>()));
            result.add(dto);
        }
        return result;
    }
}
