-- 医生端后四个模块增量脚本（MySQL 8）。先执行 doctor_workspace.sql，再执行本文件。
USE health_check_system;
SET NAMES utf8mb4;
CREATE TABLE IF NOT EXISTS doctor_package (
 id BIGINT PRIMARY KEY AUTO_INCREMENT, name VARCHAR(80) NOT NULL UNIQUE, category VARCHAR(30) NOT NULL,
 audience VARCHAR(100) NOT NULL, gender VARCHAR(4) NOT NULL DEFAULT '全部',
 price DECIMAL(10,2) NOT NULL, original_price DECIMAL(10,2) NOT NULL,
 features JSON NOT NULL, items JSON NOT NULL, picture VARCHAR(40) NOT NULL DEFAULT 'basic',
 enabled TINYINT NOT NULL DEFAULT 1, sort_order INT NOT NULL DEFAULT 0, version INT NOT NULL DEFAULT 0,
 created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
 INDEX idx_package_category(category,enabled)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE IF NOT EXISTS doctor_schedule_day (
 schedule_date DATE PRIMARY KEY
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE IF NOT EXISTS doctor_schedule (
 id BIGINT PRIMARY KEY AUTO_INCREMENT, appointment_id BIGINT NOT NULL UNIQUE,
 schedule_date DATE NOT NULL, start_time TIME NOT NULL, end_time TIME NOT NULL,
 status VARCHAR(15) NOT NULL DEFAULT '已预约', room VARCHAR(60) NOT NULL DEFAULT '体检一室',
 notes VARCHAR(1000) NOT NULL DEFAULT '', version INT NOT NULL DEFAULT 0,
 FOREIGN KEY(appointment_id) REFERENCES doctor_appointment(id),
 INDEX idx_schedule_day(schedule_date,start_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE IF NOT EXISTS doctor_notification (
 id BIGINT PRIMARY KEY AUTO_INCREMENT, category VARCHAR(20) NOT NULL, title VARCHAR(100) NOT NULL,
 content TEXT NOT NULL, is_read TINYINT NOT NULL DEFAULT 0,
 related_type VARCHAR(20) NOT NULL DEFAULT '', related_id BIGINT NULL,
 created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, INDEX idx_notification_read(is_read,created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE IF NOT EXISTS doctor_settings (
 id INT PRIMARY KEY, profile JSON NOT NULL, preferences JSON NOT NULL,
 notifications JSON NOT NULL, rules JSON NOT NULL, version INT NOT NULL DEFAULT 0,
 updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE IF NOT EXISTS doctor_operation_log (
 id BIGINT PRIMARY KEY AUTO_INCREMENT, actor VARCHAR(60) NOT NULL, module VARCHAR(30) NOT NULL,
 action VARCHAR(255) NOT NULL, created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
INSERT IGNORE INTO doctor_settings(id,profile,preferences,notifications,rules) VALUES(1,
 '{"doctorName":"张医生","department":"健康管理科","title":"主治医师","hospital":"熙心健康体检中心","phone":"13800000000","email":"","bio":"用专业守护每一份健康。"}',
 '{"landingPage":"dashboard","pageSize":10,"calendarView":"day","defaultRoom":"体检一室"}',
 '{"appointment":true,"report":true,"alert":true,"system":true,"other":true}',
 '{"dailyCapacity":40,"slotMinutes":30,"morningStart":"08:00","morningEnd":"12:00","afternoonStart":"13:30","afternoonEnd":"17:00","workdays":[1,2,3,4,5,6,7],"bookingNotes":"请空腹到院，携带身份证及既往检查资料。"}');

-- 套餐数据对应参考图，历史预约保持原有套餐名称快照。
INSERT IGNORE INTO doctor_package(id,name,category,audience,gender,price,original_price,features,items,picture,sort_order) VALUES
 (1,'基础体检套餐','基础体检','适合18–40岁人群','全部',399,599,'["热门","推荐"]','["常规检查12项","血常规、尿常规","肝功能、心电图"]','basic',1),
 (2,'中青年体检套餐','中青年体检','适合40–60岁人群','全部',699,999,'["畅销","全面"]','["常规检查18项","肿瘤筛查、心脑血管","彩超、骨密度"]','middle',2),
 (3,'老年体检套餐','老年体检','适合60岁以上人群','全部',999,1299,'["专业","深度"]','["全面检查24项","心脑血管专项","肿瘤标志物筛查"]','senior',3),
 (4,'女性健康套餐','女性专项','适合女性全生命周期','女',599,799,'["呵护","全面"]','["妇科检查、乳腺彩超","HPV筛查、激素检测","甲状腺功能"]','women',4),
 (5,'男性健康套餐','男性专项','适合男性各年龄段','男',599,799,'["精准","高效"]','["前列腺检查、肝功能","心脑血管、肿瘤筛查","代谢评估"]','men',5),
 (6,'高端尊享套餐','高端体检','高端人群专属定制','全部',2999,3999,'["尊享","全面"]','["全面深度检查40项","高端影像、基因检测","专家解读报告"]','premium',6),
 (7,'入职体检套餐','职场体检','适合新入职人员','全部',299,399,'["高效","推荐"]','["一般检查、血常规","肝功能、胸部X线","心电图"]','middle',7),
 (8,'学生体检套餐','基础体检','适合在校学生','全部',199,299,'["基础","高效"]','["一般检查、视力检查","血常规、尿常规","心肺检查"]','basic',8),
 (9,'高端体检套餐','高端体检','适合全面健康筛查人群','全部',1299,1599,'["深度","全面"]','["全身健康评估","心脑血管专项","腹部影像检查"]','premium',9);

-- 为原有预约补齐日程，无损保留预约状态。
INSERT IGNORE INTO doctor_schedule(appointment_id,schedule_date,start_time,end_time,status,room)
 SELECT id,exam_date,REPLACE(SUBSTRING_INDEX(time_slot,'–',1),'010:','10:'),SUBSTRING_INDEX(time_slot,'–',-1),
 CASE status WHEN '已完成' THEN '已完成' WHEN '已取消' THEN '已取消' ELSE '已预约' END,
 CONCAT('体检',CASE WHEN MOD(id,2)=0 THEN '二' ELSE '一' END,'室') FROM doctor_appointment;

-- 当前日期的五条排班演示，重复执行不会重复创建。
INSERT IGNORE INTO doctor_appointment(patient_id,appointment_no,exam_date,time_slot,package_name,source,status) VALUES
 (1,CONCAT('DEMO',DATE_FORMAT(CURRENT_DATE,'%Y%m%d'),'01'),CURRENT_DATE,'08:00–08:30','中青年体检套餐','微信小程序','已完成'),
 (2,CONCAT('DEMO',DATE_FORMAT(CURRENT_DATE,'%Y%m%d'),'02'),CURRENT_DATE,'09:00–09:30','高端体检套餐','微信公众号','已确认'),
 (3,CONCAT('DEMO',DATE_FORMAT(CURRENT_DATE,'%Y%m%d'),'03'),CURRENT_DATE,'10:00–10:30','女性健康套餐','医生工作台','已确认'),
 (6,CONCAT('DEMO',DATE_FORMAT(CURRENT_DATE,'%Y%m%d'),'04'),CURRENT_DATE,'11:00–11:30','老年体检套餐','微信小程序','已确认'),
 (5,CONCAT('DEMO',DATE_FORMAT(CURRENT_DATE,'%Y%m%d'),'05'),CURRENT_DATE,'14:00–14:30','女性健康套餐','微信小程序','已确认');
INSERT IGNORE INTO doctor_schedule(appointment_id,schedule_date,start_time,end_time,status,room)
 SELECT id,exam_date,SUBSTRING_INDEX(time_slot,'–',1),SUBSTRING_INDEX(time_slot,'–',-1),
 CASE RIGHT(appointment_no,2) WHEN '01' THEN '已完成' WHEN '02' THEN '进行中' WHEN '03' THEN '待检' ELSE '已预约' END,'体检一室'
 FROM doctor_appointment WHERE appointment_no LIKE 'DEMO%';

INSERT IGNORE INTO doctor_notification(id,category,title,content,is_read,related_type,related_id,created_at) VALUES
 (1,'预约提醒','预约提醒','您有一位新预约：李*萱，请提前查看体检安排并做好准备。',0,'appointment',1,CURRENT_TIMESTAMP),
 (2,'报告通知','报告通知','李*萱的体检报告已生成，请查看检查结果与健康建议。',0,'report',1,DATE_SUB(CURRENT_TIMESTAMP,INTERVAL 33 MINUTE)),
 (3,'异常提醒','异常提醒','张*敏的体检结果存在异常指标，请关注并进一步检查。',0,'alert',3,DATE_SUB(CURRENT_TIMESTAMP,INTERVAL 2 HOUR)),
 (4,'系统通知','系统通知','欢迎使用熙心健康医生端，您可在系统设置中调整工作偏好。',1,'',NULL,DATE_SUB(CURRENT_TIMESTAMP,INTERVAL 1 DAY)),
 (5,'预约提醒','预约提醒','王*强已完成预约确认，请及时安排体检。',1,'appointment',2,DATE_SUB(CURRENT_TIMESTAMP,INTERVAL 1 DAY)),
 (6,'报告通知','报告通知','历史体检报告已归集，可在体检报告中查询。',1,'report',2,DATE_SUB(CURRENT_TIMESTAMP,INTERVAL 1 DAY));

-- 补充分类消息，用于分页和已读状态展示。
INSERT IGNORE INTO doctor_notification(id,category,title,content,is_read,related_type,related_id,created_at) VALUES
(7,'预约提醒','预约提醒','预约安排已更新，请前往我的预约核对患者体检时间。',1,'',NULL,DATE_SUB(CURRENT_TIMESTAMP,INTERVAL 2 DAY)),
(8,'预约提醒','预约提醒','预约安排已更新，请前往我的预约核对患者体检时间。',1,'',NULL,DATE_SUB(CURRENT_TIMESTAMP,INTERVAL 3 DAY)),
(9,'预约提醒','预约提醒','预约安排已更新，请前往我的预约核对患者体检时间。',1,'',NULL,DATE_SUB(CURRENT_TIMESTAMP,INTERVAL 4 DAY)),
(10,'预约提醒','预约提醒','预约安排已更新，请前往我的预约核对患者体检时间。',1,'',NULL,DATE_SUB(CURRENT_TIMESTAMP,INTERVAL 5 DAY)),
(11,'报告通知','报告通知','体检报告已完成归档，请在报告列表查看完整结果。',1,'',NULL,DATE_SUB(CURRENT_TIMESTAMP,INTERVAL 6 DAY)),
(12,'报告通知','报告通知','体检报告已完成归档，请在报告列表查看完整结果。',1,'',NULL,DATE_SUB(CURRENT_TIMESTAMP,INTERVAL 7 DAY)),
(13,'报告通知','报告通知','体检报告已完成归档，请在报告列表查看完整结果。',1,'',NULL,DATE_SUB(CURRENT_TIMESTAMP,INTERVAL 8 DAY)),
(14,'报告通知','报告通知','体检报告已完成归档，请在报告列表查看完整结果。',1,'',NULL,DATE_SUB(CURRENT_TIMESTAMP,INTERVAL 9 DAY)),
(15,'报告通知','报告通知','体检报告已完成归档，请在报告列表查看完整结果。',1,'',NULL,DATE_SUB(CURRENT_TIMESTAMP,INTERVAL 10 DAY)),
(16,'报告通知','报告通知','体检报告已完成归档，请在报告列表查看完整结果。',1,'',NULL,DATE_SUB(CURRENT_TIMESTAMP,INTERVAL 11 DAY)),
(17,'异常提醒','异常提醒','体检存在异常指标，请在异常提醒中查看并及时处理。',1,'',NULL,DATE_SUB(CURRENT_TIMESTAMP,INTERVAL 12 DAY)),
(18,'异常提醒','异常提醒','体检存在异常指标，请在异常提醒中查看并及时处理。',1,'',NULL,DATE_SUB(CURRENT_TIMESTAMP,INTERVAL 13 DAY)),
(19,'系统通知','系统通知','请定期检查工作偏好和排班规则，保证预约安排准确。',1,'',NULL,DATE_SUB(CURRENT_TIMESTAMP,INTERVAL 14 DAY)),
(20,'系统通知','系统通知','请定期检查工作偏好和排班规则，保证预约安排准确。',1,'',NULL,DATE_SUB(CURRENT_TIMESTAMP,INTERVAL 15 DAY)),
(21,'系统通知','系统通知','请定期检查工作偏好和排班规则，保证预约安排准确。',1,'',NULL,DATE_SUB(CURRENT_TIMESTAMP,INTERVAL 16 DAY)),
(22,'系统通知','系统通知','请定期检查工作偏好和排班规则，保证预约安排准确。',1,'',NULL,DATE_SUB(CURRENT_TIMESTAMP,INTERVAL 17 DAY)),
(23,'其他消息','其他消息','健康管理工作提示：请及时更新患者档案和随访记录。',1,'',NULL,DATE_SUB(CURRENT_TIMESTAMP,INTERVAL 18 DAY)),
(24,'其他消息','其他消息','健康管理工作提示：请及时更新患者档案和随访记录。',1,'',NULL,DATE_SUB(CURRENT_TIMESTAMP,INTERVAL 19 DAY));
