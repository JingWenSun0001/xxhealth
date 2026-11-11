package com.it.xxhealth.doctor;

import com.it.xxhealth.common.Result;
import org.springframework.web.bind.annotation.*;
import java.util.*;

/** Doctor workspace API. Existing patient-side controllers remain independent. */
@RestController
@RequestMapping("/doctor/workspace")
public class DoctorWorkspaceController {
    private final DoctorWorkspaceService service;
    public DoctorWorkspaceController(DoctorWorkspaceService service) { this.service = service; }
    @GetMapping("/dashboard") public Result<?> dashboard() { return Result.success(service.dashboard()); }
    @GetMapping("/patients") public Result<?> patients(@RequestParam Map<String,String> q) { return Result.success(service.patients(q)); }
    @GetMapping("/patients/{id}") public Result<?> patient(@PathVariable Long id) { return Result.success(service.patient(id)); }
    @PostMapping("/patients") public Result<?> createPatient(@RequestBody Map<String,Object> body) { return Result.success(service.savePatient(null, body)); }
    @PutMapping("/patients/{id}") public Result<?> updatePatient(@PathVariable Long id,@RequestBody Map<String,Object> body) { return Result.success(service.savePatient(id, body)); }
    @DeleteMapping("/patients/{id}") public Result<?> deletePatient(@PathVariable Long id) { service.deletePatient(id); return Result.success(); }
    @PostMapping("/patients/import") public Result<?> importPatients(@RequestBody List<Map<String,Object>> body) { return Result.success(service.importPatients(body)); }
    @GetMapping("/appointments") public Result<?> appointments(@RequestParam Map<String,String> q) { return Result.success(service.appointments(q)); }
    @GetMapping("/appointments/{id}") public Result<?> appointment(@PathVariable Long id) { return Result.success(service.appointment(id)); }
    @PostMapping("/appointments") public Result<?> createAppointment(@RequestBody Map<String,Object> body) { return Result.success(service.createAppointment(body)); }
    @PutMapping("/appointments/{id}/status") public Result<?> appointmentStatus(@PathVariable Long id,@RequestBody Map<String,Object> body) { return Result.success(service.appointmentStatus(id, body)); }
    @GetMapping("/calendar") public Result<?> calendar(@RequestParam String month) { return Result.success(service.calendar(month)); }
    @GetMapping("/reports") public Result<?> reports(@RequestParam Map<String,String> q) { return Result.success(service.reports(q)); }
    @GetMapping("/reports/{id}") public Result<?> report(@PathVariable Long id) { return Result.success(service.report(id)); }
    @PostMapping("/reports") public Result<?> createReport(@RequestBody Map<String,Object> body) { return Result.success(service.createReport(body)); }
    @PutMapping("/reports/{id}") public Result<?> saveReport(@PathVariable Long id,@RequestBody Map<String,Object> body) { return Result.success(service.saveReport(id, body)); }
    @PutMapping("/reports/{id}/status") public Result<?> reportStatus(@PathVariable Long id,@RequestBody Map<String,Object> body) { return Result.success(service.reportStatus(id, body)); }
    @GetMapping("/templates") public Result<?> templates() { return Result.success(service.templates()); }
    @PostMapping("/templates") public Result<?> createTemplate(@RequestBody Map<String,Object> body) { return Result.success(service.createTemplate(body)); }
    @GetMapping("/alerts") public Result<?> alerts(@RequestParam Map<String,String> q) { return Result.success(service.alerts(q)); }
    @PutMapping("/alerts/{id}") public Result<?> resolveAlert(@PathVariable Long id) { service.resolveAlert(id); return Result.success(); }
}
