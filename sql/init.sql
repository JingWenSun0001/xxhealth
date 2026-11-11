-- 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS health_check_system DEFAULT CHARSET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE health_check_system;

-- =============================================
-- 1. 医院信息表
-- =============================================
CREATE TABLE hospital (
                          hpId INT PRIMARY KEY AUTO_INCREMENT COMMENT '医院编号',
                          name VARCHAR(30) NOT NULL COMMENT '医院名称',
                          picture MEDIUMTEXT COMMENT '医院图片',
                          telephone VARCHAR(20) NOT NULL COMMENT '医院电话',
                          address VARCHAR(100) NOT NULL COMMENT '医院地址',
                          businessHours VARCHAR(100) NOT NULL COMMENT '营业时间',
                          deadline VARCHAR(30) NOT NULL COMMENT '采血截止时间',
                          rule VARCHAR(30) NOT NULL COMMENT '预约人数规则',
                          state INT NOT NULL COMMENT '医院状态 (1:正常; 2:其他)'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='医院信息表';

-- =============================================
-- 2. 体检套餐信息表
-- =============================================
CREATE TABLE setmeal (
                         smId INT PRIMARY KEY AUTO_INCREMENT COMMENT '体检套餐编号',
                         name VARCHAR(225) NOT NULL COMMENT '体检套餐名称',
                         type INT NOT NULL COMMENT '体检套餐类型 (1:男士套餐; 0:女士套餐)',
                         price INT NOT NULL COMMENT '体检套餐价格'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='体检套餐信息表';

-- =============================================
-- 4. 体检项信息表 (先创建，作为被引用表)
-- =============================================
CREATE TABLE checkitem (
                           ciId INT PRIMARY KEY AUTO_INCREMENT COMMENT '检查项编号',
                           ciName VARCHAR(30) NOT NULL COMMENT '检查项名称',
                           ciContent VARCHAR(200) NOT NULL COMMENT '检查项内容',
                           meaning VARCHAR(200) COMMENT '检查项意义',
                           remarks VARCHAR(50) COMMENT '备注'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='检查项信息表';

-- =============================================
-- 3. 体检套餐项目明细表 (依赖 setmeal 和 checkitem)
-- =============================================
CREATE TABLE setmealdetailed (
                                 sdId INT PRIMARY KEY AUTO_INCREMENT COMMENT '体检套餐项目明细编号',
                                 smId INT NOT NULL COMMENT '体检套餐编号',
                                 ciId INT NOT NULL COMMENT '体检检查项编号',
                                 CONSTRAINT fk_sd_setmeal FOREIGN KEY (smId) REFERENCES setmeal(smId),
                                 CONSTRAINT fk_sd_checkitem FOREIGN KEY (ciId) REFERENCES checkitem(ciId)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='体检套餐项目明细表';

-- =============================================
-- 5. 体检项明细表 (依赖 checkitem)
-- =============================================
CREATE TABLE checkitemdetailed (
                                   cdId INT PRIMARY KEY AUTO_INCREMENT COMMENT '检查项明细编号',
                                   name VARCHAR(40) NOT NULL COMMENT '检查项明细名称',
                                   unit VARCHAR(20) COMMENT '检查项明细单位',
                                   minrange DOUBLE COMMENT '检查项明细正常值范围中的最小值',
                                   maxrange DOUBLE COMMENT '检查项明细正常值范围中的最大值',
                                   normalValue VARCHAR(20) COMMENT '检查项明细正常值 (非数字型)',
                                   normalValueString VARCHAR(20) COMMENT '检查项验证范围说明文字',
                                   type INT NOT NULL COMMENT '检查项明细类型 (1:数值范围验证型; 2:数值相等验证型; 3:无需验证型; 4:描述型; 5:其它)',
                                   ciId INT NOT NULL COMMENT '所属检查项编号',
                                   remarks VARCHAR(100) COMMENT '备注',
                                   CONSTRAINT fk_cd_checkitem FOREIGN KEY (ciId) REFERENCES checkitem(ciId)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='检查项明细表';

-- =============================================
-- 11. 用户表
-- =============================================
CREATE TABLE users (
                       userId VARCHAR(11) PRIMARY KEY NOT NULL COMMENT '用户编号 (手机号码)',
                       password VARCHAR(20) NOT NULL COMMENT '密码',
                       realName VARCHAR(20) NOT NULL COMMENT '真实姓名',
                       sex INT NOT NULL COMMENT '用户性别 (1:男; 0:女)',
                       identityCard VARCHAR(18) NOT NULL COMMENT '身份证号',
                       birthday DATE NOT NULL COMMENT '出生日期',
                       userType INT NOT NULL COMMENT '用户类型 (1:普通用户; 2:东软内部员工; 3:其他)'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- =============================================
-- 6. 体检预约订单表 (依赖 hospital, setmeal, users)
-- =============================================
CREATE TABLE orders (
                        orderId INT PRIMARY KEY AUTO_INCREMENT COMMENT '订单编号',
                        orderDate DATE NOT NULL COMMENT '预约日期',
                        userId VARCHAR(11) NOT NULL COMMENT '客户编号',
                        hpId INT NOT NULL COMMENT '所属医院编号',
                        smId INT NOT NULL COMMENT '所属套餐编号',
                        state INT NOT NULL COMMENT '订单状态 (1:未归档; 2:已归档; 3:已取消)',
                        CONSTRAINT fk_orders_user FOREIGN KEY (userId) REFERENCES users(userId),
                        CONSTRAINT fk_orders_hospital FOREIGN KEY (hpId) REFERENCES hospital(hpId),
                        CONSTRAINT fk_orders_setmeal FOREIGN KEY (smId) REFERENCES setmeal(smId)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='体检预约订单表';

-- =============================================
-- 7. 总检结论信息表 (依赖 orders)
-- =============================================
CREATE TABLE overallresult (
                               orId INT PRIMARY KEY AUTO_INCREMENT COMMENT '总检结论项编号',
                               title VARCHAR(40) NOT NULL COMMENT '总检结论项标题',
                               content VARCHAR(400) COMMENT '总检结论项内容',
                               orderId INT NOT NULL COMMENT '所属预约编号',
                               CONSTRAINT fk_result_orders FOREIGN KEY (orderId) REFERENCES orders(orderId)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='总检结论信息表';

-- =============================================
-- 8. 体检报告检查项信息表 (依赖 checkitem, orders)
-- =============================================
CREATE TABLE cireport (
                          cirId INT PRIMARY KEY AUTO_INCREMENT COMMENT '检查项报告主键',
                          ciId INT NOT NULL COMMENT '检查项编号',
                          ciName VARCHAR(30) NOT NULL COMMENT '检查项名称',
                          orderId INT NOT NULL COMMENT '所属预约编号',
                          CONSTRAINT fk_cireport_checkitem FOREIGN KEY (ciId) REFERENCES checkitem(ciId),
                          CONSTRAINT fk_cireport_orders FOREIGN KEY (orderId) REFERENCES orders(orderId)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='体检报告检查项信息表';

-- =============================================
-- 9. 体检报告检查项明细表 (依赖 checkitem, orders)
-- =============================================
CREATE TABLE cidetailedreport (
                                  cidrId INT PRIMARY KEY AUTO_INCREMENT COMMENT '检查项明细报告编号',
                                  name VARCHAR(40) NOT NULL COMMENT '检查项明细名称',
                                  unit VARCHAR(20) COMMENT '检查项明细单位',
                                  minrange DOUBLE COMMENT '检查项明细正常值范围中的最小值',
                                  maxrange DOUBLE COMMENT '检查项明细正常值范围中的最大值',
                                  normalValue VARCHAR(20) COMMENT '检查项明细正常值 (非数字型)',
                                  normalValueString VARCHAR(20) COMMENT '检查项验证范围说明文字',
                                  type INT NOT NULL COMMENT '检查项明细类型 (1:数值范围验证型; 2:数值相等验证型; 3:无需验证型; 4:描述型; 5:其它)',
                                  value VARCHAR(100) COMMENT '检查项目明细值',
                                  isError INT NOT NULL COMMENT '此项是否异常 (0:无异常; 1:异常)',
                                  ciId INT NOT NULL COMMENT '所属检查项报告编号',
                                  orderId INT NOT NULL COMMENT '所属预约编号',
                                  CONSTRAINT fk_cidr_cireport FOREIGN KEY (ciId) REFERENCES cireport(cirId),
                                  CONSTRAINT fk_cidr_orders FOREIGN KEY (orderId) REFERENCES orders(orderId)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='体检报告检查项明细表';

-- =============================================
-- 10. 医生信息表
-- =============================================
CREATE TABLE doctor (
                        docId INT PRIMARY KEY AUTO_INCREMENT COMMENT '医生编号',
                        docCode VARCHAR(20) NOT NULL UNIQUE COMMENT '医生编码 (登录用)',
                        realName VARCHAR(20) NOT NULL COMMENT '真实姓名',
                        password VARCHAR(20) NOT NULL COMMENT '密码 (登录用)',
                        sex INT NOT NULL COMMENT '性别',
                        deptno INT NOT NULL COMMENT '所属科室 (1:检验科; 2:内科; 3:外科)'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='医生信息表';
