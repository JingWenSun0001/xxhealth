package com.it.xxhealth.doctor;

import com.it.xxhealth.common.Result;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/doctor/workspace")
public class DoctorOperationsController {
    private final DoctorOperationsService service;
    private final MobileDoctorSyncService mobileSync;
    public DoctorOperationsController(DoctorOperationsService service,MobileDoctorSyncService mobileSync){this.service=service;this.mobileSync=mobileSync;}
    @GetMapping("/packages") public Result<?> packages(@RequestParam Map<String,String>q){return Result.success(service.packages(q));}
    @GetMapping("/packages/{id}") public Result<?> packageDetail(@PathVariable long id){return Result.success(service.packageDetail(id));}
    @PostMapping("/packages") public Result<?> createPackage(@RequestBody Map<String,Object>b){return Result.success(service.savePackage(null,b));}
    @PutMapping("/packages/{id}") public Result<?> savePackage(@PathVariable long id,@RequestBody Map<String,Object>b){return Result.success(service.savePackage(id,b));}
    @PutMapping("/packages/{id}/status") public Result<?> packageStatus(@PathVariable long id,@RequestBody Map<String,Object>b){return Result.success(service.packageStatus(id,b));}
    @GetMapping("/schedule") public Result<?> schedule(@RequestParam Map<String,String>q){return Result.success(service.schedule(q));}
    @PutMapping("/schedule/batch") public Result<?> batch(@RequestBody Map<String,Object>b){return Result.success(service.batchSchedule(b));}
    @PutMapping("/schedule/{id}") public Result<?> scheduleUpdate(@PathVariable long id,@RequestBody Map<String,Object>b){return Result.success(service.updateSchedule(id,b));}
    @GetMapping("/notifications") public Result<?> notifications(@RequestParam Map<String,String>q){return Result.success(service.notifications(q));}
    @GetMapping("/notifications/summary") public Result<?> summary(){return Result.success(service.notificationSummary());}
    @GetMapping("/notifications/{id}") public Result<?> notification(@PathVariable long id){return Result.success(service.notification(id));}
    @PutMapping("/notifications/read-all") public Result<?> readAll(){return Result.success(service.readAll());}
    @PutMapping("/notifications/{id}/read") public Result<?> read(@PathVariable long id,@RequestBody Map<String,Object>b){service.readNotification(id,b);return Result.success();}
    @GetMapping("/settings") public Result<?> settings(){return Result.success(service.settings());}
    @PutMapping("/settings/{section}") public Result<?> saveSettings(@PathVariable String section,@RequestBody Map<String,Object>b){return Result.success(service.saveSettings(section,b));}
    @GetMapping("/operation-logs") public Result<?> logs(@RequestParam Map<String,String>q){return Result.success(service.auditLogs(q));}
    @PostMapping("/mobile-sync") public Result<?> mobileSync(){return Result.success(mobileSync.syncAllOrders());}
}
