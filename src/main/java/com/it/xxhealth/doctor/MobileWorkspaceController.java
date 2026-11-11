package com.it.xxhealth.doctor;

import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Patient-facing adapter over the doctor's appointment and report workflow. */
@RestController
@RequestMapping("/mobile/workspace")
public class MobileWorkspaceController {
    private final MobileWorkspaceService service;

    public MobileWorkspaceController(MobileWorkspaceService service) {
        this.service = service;
    }

    @PostMapping("/packages")
    public List<Map<String, Object>> packages() {
        return service.packages();
    }

    @PostMapping("/packages/detail")
    public Map<String, Object> packageDetail(@RequestBody Map<String, Object> body) {
        return service.packageDetail(number(body, "packageid"));
    }

    @PostMapping("/book")
    public Map<String, Object> book(@RequestBody Map<String, Object> body) {
        Object hospitalId = body.get("hpid");
        Object orderDate = body.get("orderdate");
        if (hospitalId == null || orderDate == null) {
            throw new IllegalArgumentException("体检机构和预约日期不能为空");
        }
        return service.book(userId(body), number(body, "packageid"),
                (int) number(body, "hpid"), orderDate.toString());
    }

    @PostMapping("/appointments")
    public List<Map<String, Object>> appointments(@RequestBody Map<String, Object> body) {
        return service.appointments(userId(body));
    }

    @PostMapping("/reports")
    public List<Map<String, Object>> reports(@RequestBody Map<String, Object> body) {
        return service.reports(userId(body));
    }

    @PostMapping("/reports/detail")
    public Map<String, Object> report(@RequestBody Map<String, Object> body) {
        Object reportId = body.get("reportid");
        if (reportId == null) {
            throw new IllegalArgumentException("报告编号不能为空");
        }
        try {
            return service.report(userId(body), Long.parseLong(reportId.toString()));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("报告编号格式错误");
        }
    }

    @PostMapping("/summary")
    public Map<String, Object> summary(@RequestBody Map<String, Object> body) {
        return service.summary(userId(body));
    }

    @PostMapping("/messages")
    public List<Map<String, Object>> messages(@RequestBody Map<String, Object> body) {
        return service.messages(userId(body));
    }

    private String userId(Map<String, Object> body) {
        Object value = body.get("userid");
        return value == null ? "" : value.toString().trim();
    }

    private long number(Map<String, Object> body, String key) {
        Object value = body.get(key);
        if (value == null) {
            throw new IllegalArgumentException(key + "不能为空");
        }
        try {
            return Long.parseLong(value.toString());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(key + "格式错误");
        }
    }
}
