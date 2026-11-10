USE health_check_system;

-- 医生端最小演示数据。所有语句均为 INSERT IGNORE，可重复执行。
INSERT IGNORE INTO hospital
    (hpId, name, picture, telephone, address, businessHours, deadline, rule, state)
VALUES
    (1, '熙心健康体检中心', NULL, '024-88888888', '沈阳市浑南区健康路1号', '08:00-17:00', '10:00前完成采血', '每日100人', 1);

INSERT IGNORE INTO setmeal (smId, name, type, price)
VALUES (1, '基础健康体检套餐', 1, 399);

INSERT IGNORE INTO checkitem (ciId, ciName, ciContent, meaning, remarks)
VALUES
    (1, '一般检查', '身高、体重及血压检查', '评估基础身体指标', NULL),
    (2, '血常规', '常规血液指标检查', '辅助发现贫血、感染等情况', NULL);

INSERT IGNORE INTO setmealdetailed (sdId, smId, ciId)
VALUES
    (1, 1, 1),
    (2, 1, 2);

INSERT IGNORE INTO checkitemdetailed
    (cdId, name, unit, minrange, maxrange, normalValue, normalValueString, type, ciId, remarks)
VALUES
    (1, '身高', 'cm', NULL, NULL, NULL, '如实填写', 3, 1, NULL),
    (2, '体重', 'kg', NULL, NULL, NULL, '如实填写', 3, 1, NULL),
    (3, '收缩压', 'mmHg', 90, 139, NULL, '90～139 mmHg', 1, 1, NULL),
    (4, '舒张压', 'mmHg', 60, 89, NULL, '60～89 mmHg', 1, 1, NULL),
    (5, '白细胞计数', '10^9/L', 3.5, 9.5, NULL, '3.5～9.5 × 10^9/L', 1, 2, NULL),
    (6, '血小板计数', '10^9/L', 125, 350, NULL, '125～350 × 10^9/L', 1, 2, NULL);

INSERT IGNORE INTO users
    (userId, password, realName, sex, identityCard, birthday, userType)
VALUES
    ('13800138000', '123456', '演示用户', 1, '210102199001010011', '1990-01-01', 1);

INSERT IGNORE INTO doctor (docId, docCode, realName, password, sex, deptno)
VALUES (1, 'doctor01', '张医生', '123456', 1, 1);

INSERT IGNORE INTO orders (orderId, orderDate, userId, hpId, smId, state)
VALUES (1, '2026-11-05', '13800138000', 1, 1, 1);

-- INSERT IGNORE 不会覆盖已有的演示记录；以下更新确保重复执行时也能修复中文内容。
UPDATE hospital
SET name = '熙心健康体检中心',
    address = '沈阳市浑南区健康路1号',
    businessHours = '08:00-17:00',
    deadline = '10:00前完成采血',
    rule = '每日100人'
WHERE hpId = 1;

UPDATE setmeal
SET name = '基础健康体检套餐'
WHERE smId = 1;

UPDATE checkitem
SET ciName = CASE ciId
        WHEN 1 THEN '一般检查'
        WHEN 2 THEN '血常规'
    END,
    ciContent = CASE ciId
        WHEN 1 THEN '身高、体重及血压检查'
        WHEN 2 THEN '常规血液指标检查'
    END,
    meaning = CASE ciId
        WHEN 1 THEN '评估基础身体指标'
        WHEN 2 THEN '辅助发现贫血、感染等情况'
    END
WHERE ciId IN (1, 2);

UPDATE checkitemdetailed
SET name = CASE cdId
        WHEN 1 THEN '身高'
        WHEN 2 THEN '体重'
        WHEN 3 THEN '收缩压'
        WHEN 4 THEN '舒张压'
        WHEN 5 THEN '白细胞计数'
        WHEN 6 THEN '血小板计数'
    END,
    normalValueString = CASE cdId
        WHEN 1 THEN '如实填写'
        WHEN 2 THEN '如实填写'
        WHEN 3 THEN '90～139 mmHg'
        WHEN 4 THEN '60～89 mmHg'
        WHEN 5 THEN '3.5～9.5 × 10^9/L'
        WHEN 6 THEN '125～350 × 10^9/L'
    END
WHERE cdId IN (1, 2, 3, 4, 5, 6);

UPDATE users SET realName = '演示用户' WHERE userId = '13800138000';
UPDATE doctor SET realName = '张医生' WHERE docId = 1;

-- 修复已经为演示订单生成的报告快照，不覆盖医生录入的 value/isError。
UPDATE cireport cr
INNER JOIN checkitem ci ON ci.ciId = cr.ciId
SET cr.ciName = ci.ciName
WHERE cr.orderId = 1;

UPDATE cidetailedreport
SET name = CASE cidrId
        WHEN 1 THEN '身高'
        WHEN 2 THEN '体重'
        WHEN 3 THEN '收缩压'
        WHEN 4 THEN '舒张压'
        WHEN 5 THEN '白细胞计数'
        WHEN 6 THEN '血小板计数'
    END,
    normalValueString = CASE cidrId
        WHEN 1 THEN '如实填写'
        WHEN 2 THEN '如实填写'
        WHEN 3 THEN '90～139 mmHg'
        WHEN 4 THEN '60～89 mmHg'
        WHEN 5 THEN '3.5～9.5 × 10^9/L'
        WHEN 6 THEN '125～350 × 10^9/L'
    END
WHERE orderId = 1 AND cidrId IN (1, 2, 3, 4, 5, 6);
