-- ============================================================
-- 礼单送还系统 数据库初始化脚本
-- 数据库：demo（MySQL 8.0）
-- 执行：mysql -h127.0.0.1 -uroot -p --default-character-set=utf8mb4 < init.sql
-- 说明：重建所有表，插入基础数据（角色/菜单/关联）与示例数据
-- ============================================================

SET NAMES utf8mb4;

CREATE DATABASE IF NOT EXISTS `demo` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE `demo`;

-- ---------- 删除旧表（按依赖逆序） ----------
DROP TABLE IF EXISTS `tb_role_menu`;
DROP TABLE IF EXISTS `tb_user_role`;
DROP TABLE IF EXISTS `tb_menu`;
DROP TABLE IF EXISTS `tb_role`;
DROP TABLE IF EXISTS `tb_gift_record`;
DROP TABLE IF EXISTS `tb_user`;

-- ---------- 用户表 ----------
CREATE TABLE `tb_user` (
  `id` bigint NOT NULL COMMENT '主键',
  `username` varchar(50) NOT NULL COMMENT '用户名',
  `nickname` varchar(50) DEFAULT NULL COMMENT '昵称',
  `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
  `password` varchar(100) DEFAULT NULL COMMENT 'BCrypt密码哈希',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态 1启用 0禁用',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_yn` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除 0否 1是',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户表';

-- ---------- 礼单记录表 ----------
CREATE TABLE `tb_gift_record` (
  `id` bigint NOT NULL COMMENT '主键',
  `user_id` bigint NOT NULL COMMENT '归属用户ID',
  `name` varchar(50) NOT NULL COMMENT '对方姓名',
  `amount` decimal(10,2) NOT NULL COMMENT '金额',
  `direction` tinyint NOT NULL DEFAULT '1' COMMENT '方向 1送出 2收到(还礼)',
  `gift_date` date NOT NULL COMMENT '礼金日期',
  `occasion` varchar(50) NOT NULL COMMENT '事由',
  `relation` varchar(50) DEFAULT NULL COMMENT '关系',
  `remark` varchar(200) DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_yn` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除 0否 1是',
  PRIMARY KEY (`id`),
  KEY `idx_user_date` (`user_id`,`gift_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='礼单记录表';

-- ---------- 角色表 ----------
CREATE TABLE `tb_role` (
  `id` bigint NOT NULL COMMENT '主键',
  `role_code` varchar(50) NOT NULL COMMENT '角色编码',
  `role_name` varchar(50) NOT NULL COMMENT '角色名称',
  `remark` varchar(200) DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_yn` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除 0否 1是',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_code` (`role_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='角色表';

-- ---------- 菜单表 ----------
CREATE TABLE `tb_menu` (
  `id` bigint NOT NULL COMMENT '主键',
  `parent_id` bigint NOT NULL DEFAULT '0' COMMENT '父菜单ID 0=顶级',
  `menu_name` varchar(50) NOT NULL COMMENT '菜单名称',
  `menu_type` tinyint NOT NULL DEFAULT '1' COMMENT '类型 1目录 2菜单 3按钮',
  `route_path` varchar(100) DEFAULT NULL COMMENT '路由地址',
  `component` varchar(200) DEFAULT NULL COMMENT '前端组件名',
  `icon` varchar(50) DEFAULT NULL COMMENT '图标',
  `perms` varchar(100) DEFAULT NULL COMMENT '权限标识(按钮)',
  `sort` int NOT NULL DEFAULT '0' COMMENT '排序',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_yn` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除 0否 1是',
  PRIMARY KEY (`id`),
  KEY `idx_parent` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='菜单表';

-- ---------- 用户角色关联表 ----------
CREATE TABLE `tb_user_role` (
  `id` bigint NOT NULL COMMENT '主键',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `role_id` bigint NOT NULL COMMENT '角色ID',
  PRIMARY KEY (`id`),
  KEY `idx_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户角色关联表';

-- ---------- 角色菜单关联表 ----------
CREATE TABLE `tb_role_menu` (
  `id` bigint NOT NULL COMMENT '主键',
  `role_id` bigint NOT NULL COMMENT '角色ID',
  `menu_id` bigint NOT NULL COMMENT '菜单ID',
  PRIMARY KEY (`id`),
  KEY `idx_role` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='角色菜单关联表';

-- ============================================================
-- 基础数据
-- ============================================================

-- 角色
INSERT INTO `tb_role` (`id`, `role_code`, `role_name`, `remark`) VALUES
(1, 'admin', '管理员', '系统管理员，拥有全部权限'),
(2, 'tenant', '租户', '普通租户，仅礼单与统计');

-- 菜单（目录/菜单/按钮）
INSERT INTO `tb_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `route_path`, `component`, `icon`, `perms`, `sort`) VALUES
(1, 0, '礼单管理', 2, '/gifts', 'GiftList', '📋', NULL, 1),
(2, 0, '统计分析', 2, '/stats', 'Statistics', '📊', NULL, 2),
(3, 0, '系统管理', 1, '/system', NULL, '⚙️', NULL, 3),
(4, 3, '用户管理', 2, '/users', 'UserList', '👥', NULL, 1),
(5, 4, '用户新增', 3, NULL, NULL, NULL, 'user:add', 1),
(6, 4, '用户编辑', 3, NULL, NULL, NULL, 'user:edit', 2),
(7, 4, '用户删除', 3, NULL, NULL, NULL, 'user:delete', 3),
(8, 3, '角色管理', 2, '/roles', 'RoleManage', '🎭', NULL, 2),
(9, 3, '菜单管理', 2, '/menus', 'MenuManage', '📁', NULL, 3),
(10, 1, '礼单新增', 3, NULL, NULL, NULL, 'gift:add', 1),
(11, 1, '礼单编辑', 3, NULL, NULL, NULL, 'gift:edit', 2),
(12, 1, '礼单删除', 3, NULL, NULL, NULL, 'gift:delete', 3),
(13, 1, '礼单导入', 3, NULL, NULL, NULL, 'gift:import', 4);

-- 角色菜单：admin 全量
INSERT INTO `tb_role_menu` (`id`, `role_id`, `menu_id`) VALUES
(1,1,1),(2,1,2),(3,1,3),(4,1,4),(5,1,5),(6,1,6),(7,1,7),(8,1,8),(9,1,9),(10,1,10),(11,1,11),(12,1,12),(13,1,13);

-- 角色菜单：tenant 礼单 + 统计 + 礼单按钮
INSERT INTO `tb_role_menu` (`id`, `role_id`, `menu_id`) VALUES
(14,2,1),(15,2,2),(16,2,10),(17,2,11),(18,2,12),(19,2,13);

-- ============================================================
-- 示例用户（密码均为 123456，BCrypt 哈希）
-- ============================================================
INSERT INTO `tb_user` (`id`, `username`, `nickname`, `email`, `password`, `status`) VALUES
(1, 'admin', 'admin', 'zhangsan@example.com', '$2a$10$5ZS0rCY1AxELYZC4jvLtt.adI1ATj/SguThRizwrqWtAucDfvlyX2', 1),
(2, 'lisi', '李四', 'lisi@example.com', '$2a$10$5rVg.95/qKQt1DmmVbi1T.J9.X7GA1kHAdnZyftl/VNcNd.UD3/HW', 1),
(2096770124083302401, 'wangwu', '王五', 'wangwu@example.com', '$2a$10$VGmVYYT1vqgtPL7CuLAhG.hWEnOIzJlzut99cGx3GYgrEUy3SB766', 1);

-- 用户角色：admin=管理员，lisi/wangwu=租户
INSERT INTO `tb_user_role` (`id`, `user_id`, `role_id`) VALUES
(1, 1, 1),
(2, 2, 2),
(3, 2096770124083302401, 2);

-- ============================================================
-- 示例礼单数据（归属 admin，用于演示归还/统计）
-- ============================================================
INSERT INTO `tb_gift_record` (`id`, `user_id`, `name`, `amount`, `direction`, `gift_date`, `occasion`, `relation`, `remark`) VALUES
(1001, 1, '王小明', 600.00, 1, '2025-01-18', '结婚', '朋友', NULL),
(1002, 1, '李建国', 500.00, 1, '2025-02-10', '满月', '同事', NULL),
(1003, 1, '赵丽', 800.00, 1, '2025-03-06', '结婚', '同学', NULL),
(1004, 1, '刘洋', 300.00, 1, '2025-05-01', '乔迁', '朋友', NULL),
(1005, 1, '陈静', 1000.00, 1, '2025-06-20', '结婚', '亲戚', '表妹'),
(1006, 1, '周涛', 400.00, 1, '2025-08-15', '生日', '朋友', NULL),
(1007, 1, '吴芳', 600.00, 1, '2025-09-28', '结婚', '同事', NULL),
(1008, 1, '郑强', 200.00, 1, '2025-11-11', '满月', '朋友', NULL),
(1009, 1, '孙悦', 900.00, 1, '2025-12-03', '结婚', '同学', NULL),
(1010, 1, '冯磊', 500.00, 1, '2026-01-25', '乔迁', '同事', NULL),
(1020, 1, '孙悦', 1000.00, 1, '2026-03-12', '满月', '同学', NULL),
(1011, 1, '王小明', 600.00, 2, '2026-09-05', '结婚', '朋友', '还礼'),
(1012, 1, '李建国', 500.00, 2, '2026-09-06', '结婚', '同事', '还礼'),
(1013, 1, '赵丽', 800.00, 2, '2026-09-07', '结婚', '同学', '还礼'),
(1014, 1, '陈静', 1000.00, 2, '2026-09-08', '结婚', '亲戚', '还礼'),
(1015, 1, '周涛', 500.00, 2, '2026-09-09', '结婚', '朋友', '还礼'),
(1016, 1, '吴芳', 700.00, 2, '2026-09-10', '结婚', '同事', '还礼'),
(1017, 1, '郑强', 400.00, 2, '2026-09-11', '结婚', '朋友', '还礼'),
(1018, 1, '孙悦', 1000.00, 2, '2026-09-12', '结婚', '同学', '还礼'),
(1019, 1, '冯磊', 600.00, 2, '2026-09-13', '结婚', '同事', '还礼');
