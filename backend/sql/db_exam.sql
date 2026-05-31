/*
 Navicat Premium Dump SQL

 Source Server         : localhost
 Source Server Type    : MySQL
 Source Server Version : 80035 (8.0.35)
 Source Host           : localhost:3306
 Source Schema         : db_exam

 Target Server Type    : MySQL
 Target Server Version : 80035 (8.0.35)
 File Encoding         : 65001

 Date: 24/02/2026 18:28:26
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ================================
-- 管理员表
-- 功能：存储系统管理员账户信息，用于后台系统登录认证
-- 主要内容：管理员登录名、邮箱、密码（加密存储）、状态、角色、头像
-- 业务说明：系统超级管理员账户，具有最高权限，可管理所有功能模块
-- ================================
DROP TABLE IF EXISTS `t_admin`;
CREATE TABLE `t_admin`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '管理员ID，主键自增',
  `admin_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '管理员登录用户名，唯一标识，用于系统登录认证',
  `mail` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '管理员邮箱，用于接收系统通知和找回密码',
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '登录密码，使用BCrypt加密存储，保障安全性',
  `status` int NULL DEFAULT 1 COMMENT '账户状态：1-正常（可登录），0-禁用（禁止登录）',
  `role_id` int NULL DEFAULT 0 COMMENT '角色ID，关联t_role表，标识管理员所属角色权限',
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT 'http://118.178.88.163:9000/examsystem/avatar/admin.jpg' COMMENT '管理员头像URL路径，存储头像图片的OSS地址',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `admin_name`(`admin_name` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 0 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of t_admin
-- ----------------------------
INSERT INTO `t_admin` VALUES (0, 'admin', '1878679942@qq.com', '$2a$10$8mExrAoTNp7Opwoe6VTqSeqSYKyrFrr5.l7HGMW7aWRp5BiEPmyUm', 1, 0, 'http://118.178.88.163:9000/examsystem/avatar/admin.jpg');

-- ================================
-- 分类表
-- 功能：存储课程和题库的分类层级结构
-- 主要内容：分类名称、父分类ID、排序序号
-- 业务说明：支持多级分类，parent_id=0表示一级分类，通过树形结构组织课程和题库
-- ================================
DROP TABLE IF EXISTS `t_category`;
CREATE TABLE `t_category`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '分类ID，主键自增',
  `category_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '分类名称，用于展示和选择，如"计算机基础"、"高等数学"等',
  `parent_id` int NULL DEFAULT 0 COMMENT '父分类ID，0表示顶级分类（非0值关联t_category.id），支持二级或多级分类体系',
  `sort` int NULL DEFAULT 0 COMMENT '排序序号，数值越小越靠前，用于控制同级分类的显示顺序',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间，记录分类的创建时刻，格式：YYYY-MM-DD HH:MM:SS',
  `is_deleted` int NOT NULL DEFAULT 0 COMMENT '逻辑删除标记：0-未删除（正常显示），1-已删除（不显示但保留数据）',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of t_category
-- ----------------------------

-- ================================
-- 讨论表
-- 功能：存储用户在课程下的讨论帖子
-- 主要内容：发布用户、所属课程、标题、内容、发布时间
-- 业务说明：支持师生在课程下发起讨论、提问和交流，关联t_user表和t_subject表
-- ================================
DROP TABLE IF EXISTS `t_discussion`;
CREATE TABLE `t_discussion`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '讨论帖ID，主键自增',
  `user_id` int NOT NULL COMMENT '发布用户ID，关联t_user表(id)，标识帖子作者身份',
  `subject_id` int NOT NULL COMMENT '所属课程ID，关联t_subject表(id)，标识帖子所属课程范围',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '讨论内容，支持富文本格式，存储帖子的正文内容',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '帖子标题，用于帖子列表展示和搜索',
  `create_time` datetime NOT NULL COMMENT '发布时间，记录帖子的创建时刻，格式：YYYY-MM-DD HH:MM:SS',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of t_discussion
-- ----------------------------

-- ================================
-- 考试表
-- 功能：存储考试的基本配置信息
-- 主要内容：考试名称、时长、分数、切屏限制、题目配置、时间设置
-- 业务说明：记录一场考试的所有配置参数，包括考试时长、及格线、题目数量和分值、考试时间范围
-- 注意：分数字段数据库存储值为实际分值*100（如60分存储为6000），前端展示时除以100
-- ================================
DROP TABLE IF EXISTS `t_exam`;
CREATE TABLE `t_exam`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '考试ID，主键自增',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '考试名称，用于在考试列表和试卷中心展示',
  `exam_duration` int NOT NULL COMMENT '考试时长，单位为分钟，定义考生完成考试的最大时间限制',
  `passed_score` int NOT NULL COMMENT '及格分数，定义考试的及格分数线，考生得分需达到此分数才算通过',
  `gross_score` int NOT NULL COMMENT '试卷总分，统计本次考试所有题目的总分值',
  `max_count` int NULL DEFAULT NULL COMMENT '最大切屏次数，允许考生切换页面的最大次数，超过则强制交卷',
  `user_id` int NULL DEFAULT NULL COMMENT '创建人ID，关联t_user表(id)，标识这场考试的创建者（教师）',
  `radio_count` int NULL DEFAULT NULL COMMENT '单选题数量，本次考试包含的单选题题目数量',
  `radio_score` int NULL DEFAULT NULL COMMENT '单选题每题分值，数据库存储为实际分值*100（如2分存储为200）',
  `multi_count` int NULL DEFAULT NULL COMMENT '多选题数量，本次考试包含的多选题题目数量',
  `multi_score` int NULL DEFAULT NULL COMMENT '多选题每题分值，数据库存储为实际分值*100（如3分存储为300）',
  `judge_count` int NULL DEFAULT NULL COMMENT '判断题数量，本次考试包含的判断题题目数量',
  `judge_score` int NULL DEFAULT NULL COMMENT '判断题每题分值，数据库存储为实际分值*100（如1分存储为100）',
  `saq_count` int NULL DEFAULT NULL COMMENT '简答题数量，本次考试包含的简答题（主观题）题目数量',
  `saq_score` int NULL DEFAULT NULL COMMENT '简答题每题分值，数据库存储为实际分值*100（如10分存储为1000）',
  `start_time` datetime NULL DEFAULT NULL COMMENT '考试开始时间，定义考试的开放时间点，格式：YYYY-MM-DD HH:MM:SS',
  `end_time` datetime NULL DEFAULT NULL COMMENT '考试结束时间，定义考试的截止时间点，格式：YYYY-MM-DD HH:MM:SS',
  `create_time` datetime NULL DEFAULT NULL COMMENT '考试创建时间，记录考试配置的创建时刻，格式：YYYY-MM-DD HH:MM:SS',
  `is_deleted` int NOT NULL DEFAULT 0 COMMENT '逻辑删除标记：0-未删除（正常），1-已删除（不显示）',
  PRIMARY KEY (`id`, `passed_score`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of t_exam
-- ----------------------------

-- ================================
-- 考试答题记录表
-- 功能：存储考生在考试中对每道题目的作答记录
-- 主要内容：用户ID、考试ID、题目ID、答案内容、标记状态、正确性判定
-- 业务说明：记录考生每次考试的完整答题过程，支持客观题自动评分和主观题人工评分
-- ================================
DROP TABLE IF EXISTS `t_exam_qu_answer`;
CREATE TABLE `t_exam_qu_answer`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '答题记录ID，主键自增',
  `user_id` int NOT NULL COMMENT '用户ID，关联t_user表(id)，标识作答考生身份',
  `exam_id` int NOT NULL COMMENT '考试ID，关联t_exam表(id)，标识属于哪场考试',
  `question_id` int NOT NULL COMMENT '题目ID，关联t_question表(id)，标识作答的具体题目',
  `question_type` int NULL DEFAULT NULL COMMENT '题目类型：1-单选题，2-多选题，3-判断题，4-简答题',
  `answer_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '选项答案ID，客观题使用，存储选择的选项ID，多选题用逗号分隔',
  `answer_content` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL COMMENT '主观题答案内容，简答题使用，存储考生输入的文本答案',
  `checkout` int NULL DEFAULT NULL COMMENT '是否选中标记：0-未选中，1-选中，用于标记当前题目是否已完成作答',
  `is_sign` int NULL DEFAULT NULL COMMENT '标记状态：0-未标记，1-已标记，考生可标记有疑问的题目稍后回顾',
  `is_right` int NULL DEFAULT NULL COMMENT '客观题正确性判定：0-错误，1-正确（仅客观题自动判定，主观题为NULL待人工评分）',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `user_id`(`user_id` ASC, `exam_id` ASC, `question_id` ASC) USING BTREE COMMENT '联合唯一索引，确保每个考生每场考试每道题只有一条答题记录'
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of t_exam_qu_answer
-- ----------------------------

-- ================================
-- 试卷题目关联表
-- 功能：存储考试与题目的关联关系及题目在试卷中的位置
-- 主要内容：考试ID、题目ID、题目分值、题目排序
-- 业务说明：定义一场考试包含哪些题目，以及每道题的分值和出现顺序
-- ================================
DROP TABLE IF EXISTS `t_exam_question`;
CREATE TABLE `t_exam_question`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '试卷题目ID，主键自增',
  `exam_id` int NOT NULL COMMENT '考试ID，关联t_exam表(id)，标识所属考试',
  `question_id` int NOT NULL COMMENT '题目ID，关联t_question表(id)，标识试卷中的具体题目',
  `score` int NOT NULL COMMENT '题目分值，这道题在本次考试中的分值',
  `sort` int NULL DEFAULT NULL COMMENT '题目排序号，定义题目在试卷中的显示顺序',
  `type` int NULL DEFAULT NULL COMMENT '题目类型：1-单选题，2-多选题，3-判断题，4-简答题',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of t_exam_question
-- ----------------------------

-- ================================
-- 考试题库关联表
-- 功能：存储考试与题库的关联关系
-- 主要内容：考试ID、题库ID
-- 业务说明：定义一场考试的题目来源从哪些题库抽取，支持多题库组卷
-- ================================
DROP TABLE IF EXISTS `t_exam_repo`;
CREATE TABLE `t_exam_repo`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '考试题库关联ID，主键自增',
  `exam_id` int NOT NULL COMMENT '考试ID，关联t_exam表(id)，标识所属考试（唯一）',
  `repo_id` int NULL DEFAULT NULL COMMENT '题库ID，关联t_repo表(id)，标识题库来源',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of t_exam_repo
-- ----------------------------

-- ================================
-- 考试课程关联表
-- 功能：存储考试与课程的关联关系
-- 主要内容：考试ID、课程ID
-- 业务说明：定义考试发布到哪些课程范围，学生需要订阅相应课程才能参加考试
-- ================================
DROP TABLE IF EXISTS `t_exam_subject`;
CREATE TABLE `t_exam_subject`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '考试课程关联ID，主键自增',
  `exam_id` int NULL DEFAULT NULL COMMENT '考试ID，关联t_exam表(id)，标识所属考试（唯一）',
  `subject_id` int NULL DEFAULT NULL COMMENT '课程ID，关联t_subject表(id)，标识发布到的课程',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of t_exam_subject
-- ----------------------------

-- ================================
-- 刷题记录表
-- 功能：存储用户在题库中的刷题作答记录
-- 主要内容：题库ID、题目ID、用户ID、答案内容、正确性
-- 业务说明：记录用户在题库练习时的答题情况，支持客观题自动判定正误
-- ================================
DROP TABLE IF EXISTS `t_exercise_record`;
CREATE TABLE `t_exercise_record`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '刷题记录ID，主键自增',
  `repo_id` int NOT NULL COMMENT '题库ID，关联t_repo表(id)，标识所属题库',
  `question_id` int NOT NULL COMMENT '题目ID，关联t_question表(id)，标识作答的题目',
  `user_id` int NOT NULL COMMENT '用户ID，关联t_user表(id)，标识作答用户',
  `answer` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '主观题答案，简答题用户输入的文本内容',
  `question_type` int NOT NULL COMMENT '题目类型：1-单选题，2-多选题，3-判断题，4-简答题',
  `options` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '客观题答案选项，多选题用逗号分隔各选项ID',
  `is_right` int NULL DEFAULT NULL COMMENT '客观题正确性判定：0-错误，1-正确（主观题为NULL）',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `repo_id`(`repo_id` ASC, `question_id` ASC, `user_id` ASC) USING BTREE COMMENT '联合唯一索引，确保每个用户每题每题库只有一条记录'
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of t_exercise_record
-- ----------------------------

-- ================================
-- 点赞表
-- 功能：存储用户对讨论帖或回复的点赞记录
-- 主要内容：讨论ID、回复ID、用户ID、点赞时间
-- 业务说明：支持对讨论帖和回复进行点赞，支持取消点赞
-- ================================
DROP TABLE IF EXISTS `t_like`;
CREATE TABLE `t_like`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '点赞记录ID，主键自增',
  `discussion_id` int NULL DEFAULT NULL COMMENT '讨论帖ID，关联t_discussion表(id)，标识被点赞的讨论帖',
  `reply_id` int NULL DEFAULT NULL COMMENT '回复ID，关联t_reply表(id)，标识被点赞的回复',
  `user_id` int NULL DEFAULT NULL COMMENT '用户ID，关联t_user表(id)，标识点赞用户',
  `create_time` datetime NULL DEFAULT NULL COMMENT '点赞时间，记录用户点赞的时刻',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `discussion_id`(`discussion_id` ASC, `reply_id` ASC, `user_id` ASC) USING BTREE COMMENT '联合唯一索引，防止用户重复点赞同一内容'
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of t_like
-- ----------------------------

-- ================================
-- 操作日志表
-- 功能：记录用户的重要操作日志
-- 主要内容：操作地点、操作行为、设备信息、操作用户、操作时间
-- 业务说明：用于审计追踪，记录用户的登录地点和关键操作行为
-- ================================
DROP TABLE IF EXISTS `t_log`;
CREATE TABLE `t_log`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '日志ID，主键自增',
  `place` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '登录地点，记录用户登录时的IP地址归属地信息',
  `behavior` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '操作行为，描述用户执行的具体操作类型',
  `device` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '登录设备，记录用户使用的设备类型和浏览器信息',
  `user_id` int NOT NULL COMMENT '操作用户ID，关联t_user表(id)或t_admin表(id)，标识执行操作的用户',
  `create_time` datetime NOT NULL COMMENT '操作时间，记录用户执行操作的时刻，格式：YYYY-MM-DD HH:MM:SS',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of t_log
-- ----------------------------

-- ================================
-- 人工评分表
-- 功能：存储教师对主观题的人工阅卷评分记录
-- 主要内容：答题记录ID、批改人ID、得分、批改时间
-- 业务说明：用于教师对简答题等主观题进行人工评分，记录阅卷过程
-- ================================
DROP TABLE IF EXISTS `t_manual_score`;
CREATE TABLE `t_manual_score`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '人工评分记录ID，主键自增',
  `exam_qu_answer_id` int NULL DEFAULT NULL COMMENT '考试答题记录ID，关联t_exam_qu_answer表(id)，标识被评分的答题记录',
  `user_id` int NULL DEFAULT NULL COMMENT '批改人ID，关联t_user表(id)（教师用户），标识执行批改的教师',
  `score` int NULL DEFAULT NULL COMMENT '得分，批改给予的分数值',
  `create_time` datetime NULL DEFAULT NULL COMMENT '批改时间，记录教师完成批改的时刻',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of t_manual_score
-- ----------------------------

-- ================================
-- 公告表
-- 功能：存储系统公告信息
-- 主要内容：公告标题、图片、内容、发布者、发布时间、可见范围
-- 业务说明：管理员或教师发布系统公告或课程公告，通知相关用户群体
-- ================================
DROP TABLE IF EXISTS `t_notice`;
CREATE TABLE `t_notice`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '公告ID，主键自增',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '公告标题，用于公告列表展示',
  `image` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '公告封面图片URL，可选的配图增强展示效果',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '公告正文内容，支持富文本格式',
  `user_id` int NULL DEFAULT NULL COMMENT '发布者用户ID，关联t_user表(id)或t_admin表(id)，标识公告发布者',
  `create_time` datetime NULL DEFAULT NULL COMMENT '发布时间，记录公告的创建时刻',
  `is_deleted` int NOT NULL DEFAULT 0 COMMENT '逻辑删除标记：0-未删除（正常显示），1-已删除',
  `is_public` int NULL DEFAULT NULL COMMENT '是否公开：1-公开给所有用户（管理员发布），0-仅限关联课程（教师发布）',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of t_notice
-- ----------------------------

-- ================================
-- 公告课程关联表
-- 功能：存储公告与课程的关联关系
-- 主要内容：公告ID、课程ID
-- 业务说明：定义公告的可见课程范围，非公开公告只在关联课程的学生中展示
-- ================================
DROP TABLE IF EXISTS `t_notice_subject`;
CREATE TABLE `t_notice_subject`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '公告课程关联ID，主键自增',
  `notice_id` int NULL DEFAULT NULL COMMENT '公告ID，关联t_notice表(id)，标识所属公告',
  `subject_id` int NULL DEFAULT NULL COMMENT '课程ID，关联t_subject表(id)，标识公告发布的课程',
  `is_deleted` int NULL DEFAULT 0 COMMENT '逻辑删除标记：0-未删除，1-已删除',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of t_notice_subject
-- ----------------------------

-- ================================
-- 选项答案表
-- 功能：存储题目的所有选项及其正确性
-- 主要内容：题目ID、选项内容、是否正确、排序、图片
-- 业务说明：记录每道题的所有选项定义，用于客观题（单选、多选、判断）的选项展示和答案判定
-- ================================
DROP TABLE IF EXISTS `t_option`;
CREATE TABLE `t_option`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '选项ID，主键自增',
  `qu_id` int NOT NULL COMMENT '题目ID，关联t_question表(id)，标识所属题目',
  `is_right` int NULL DEFAULT NULL COMMENT '是否正确答案：0-错误选项，1-正确选项（判断题只有1个正确，单选1个，多选多个）',
  `image` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '选项图片URL，可选的图片选项增强题目展示',
  `content` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '选项内容文字，定义选项的具体描述',
  `sort` int NULL DEFAULT NULL COMMENT '选项排序号，定义选项在题目中的显示顺序（如A、B、C、D）',
  `is_deleted` int NOT NULL DEFAULT 0 COMMENT '逻辑删除标记：0-未删除（正常使用），1-已删除',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of t_option
-- ----------------------------

-- ================================
-- 试题表
-- 功能：存储题目的基本信息和题干内容
-- 主要内容：题目类型、内容、图片、解析、所属题库、创建者
-- 业务说明：定义考试和刷题的核心资源，支持多种题型，包含题目解析供学习参考
-- ================================
DROP TABLE IF EXISTS `t_question`;
CREATE TABLE `t_question`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '题目ID，主键自增',
  `qu_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '题目类型：radio-单选题，multi-多选题，judge-判断题，saq-简答题',
  `image` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '题目配图URL，可选的图片增强题目描述',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '题干内容，题目的问题描述文字',
  `create_time` datetime NOT NULL COMMENT '创建时间，记录题目的创建时刻',
  `analysis` text CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL COMMENT '题目解析，对题目的解题思路或答案进行说明',
  `repo_id` int NULL DEFAULT NULL COMMENT '所属题库ID，关联t_repo表(id)，标识题目归属的题库',
  `user_id` int NULL DEFAULT NULL COMMENT '创建者ID，关联t_user表(id)（教师用户），标识题目的创建教师',
  `is_deleted` int NOT NULL DEFAULT 0 COMMENT '逻辑删除标记：0-未删除（正常使用），1-已删除',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of t_question
-- ----------------------------

-- ================================
-- 回复表
-- 功能：存储用户对讨论帖的回复内容
-- 主要内容：讨论ID、用户ID、父回复ID、回复内容、回复时间
-- 业务说明：支持讨论帖的多级回复，形成讨论线程，支持嵌套回复结构
-- ================================
DROP TABLE IF EXISTS `t_reply`;
CREATE TABLE `t_reply`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '回复ID，主键自增',
  `discussion_id` int NOT NULL COMMENT '讨论帖ID，关联t_discussion表(id)，标识所属讨论帖',
  `user_id` int NOT NULL COMMENT '回复用户ID，关联t_user表(id)，标识回复作者',
  `parent_id` int NULL DEFAULT NULL COMMENT '父回复ID，关联t_reply表(id)，NULL表示一级回复，非NULL表示回复的回复（嵌套评论）',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '回复内容，用户的回复正文',
  `create_time` datetime NOT NULL COMMENT '回复时间，记录回复的创建时刻',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of t_reply
-- ----------------------------

-- ================================
-- 题库表
-- 功能：存储题库的基本信息
-- 主要内容：题库标题、创建者、所属分类、是否开启刷题模式
-- 业务说明：用于组织和管理试题，支持分类管理，可设置为练习模式供学生刷题
-- ================================
DROP TABLE IF EXISTS `t_repo`;
CREATE TABLE `t_repo`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '题库ID，主键自增',
  `user_id` int NOT NULL COMMENT '创建者ID，关联t_user表(id)（教师用户），标识题库创建者',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '题库标题，用于题库列表展示和选择',
  `category_id` int NULL DEFAULT NULL COMMENT '分类ID，关联t_category表(id)，标识题库所属分类',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间，记录题库的创建时刻',
  `is_deleted` int NOT NULL DEFAULT 0 COMMENT '逻辑删除标记：0-未删除（正常使用），1-已删除',
  `is_exercise` int NOT NULL DEFAULT 0 COMMENT '是否开启刷题模式：0-关闭，1-开启（开启后学生可进入题库练习）',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of t_repo
-- ----------------------------

-- ================================
-- 角色表
-- 功能：存储系统角色定义
-- 主要内容：角色ID、角色名称、角色编码
-- 业务说明：定义系统的用户角色体系，如管理员、教师、学生，每种角色有不同权限
-- ================================
DROP TABLE IF EXISTS `t_role`;
CREATE TABLE `t_role`  (
  `id` int NOT NULL COMMENT '角色ID，主键',
  `role_name` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '角色名称，用于展示，如"管理员"、"教师"、"学生"',
  `code` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '角色编码，系统内部标识，如"admin"、"teacher"、"student"',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of t_role
-- ----------------------------
INSERT INTO `t_role` VALUES (0, '管理员', 'admin');
INSERT INTO `t_role` VALUES (1, '教师', 'teacher');
INSERT INTO `t_role` VALUES (2, '学生', 'student');

-- ================================
-- 角色权限关联表
-- 功能：存储角色与权限的对应关系
-- 主要内容：角色ID、权限标识
-- 业务说明：定义每个角色拥有的具体操作权限，支持RBAC权限管理模型
-- ================================
DROP TABLE IF EXISTS `t_role_permission`;
CREATE TABLE `t_role_permission`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '角色权限ID，主键自增',
  `role_id` int NOT NULL COMMENT '角色ID，关联t_role表(id)，标识所属角色',
  `permission` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '权限标识，如"exam:create"、"question:add"，定义具体操作权限',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_role_id`(`role_id` ASC) USING BTREE COMMENT '角色ID索引，加速按角色查询权限'
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of t_role_permission
-- ----------------------------

-- ================================
-- 课程表
-- 功能：存储课程的基本信息
-- 主要内容：课程名称、创建者、课程口令
-- 业务说明：教师创建的课程，学生通过课程口令加入，实现课程范围内的教学活动
-- ================================
DROP TABLE IF EXISTS `t_subject`;
CREATE TABLE `t_subject`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '课程ID，主键自增',
  `subject_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '课程名称，用于课程列表展示',
  `user_id` int NULL DEFAULT NULL COMMENT '创建者ID，关联t_user表(id)（教师用户），标识课程创建教师',
  `code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '课程口令，学生加入课程的密钥，唯一标识',
  `is_deleted` int NOT NULL DEFAULT 0 COMMENT '逻辑删除标记：0-未删除（正常使用），1-已删除',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间，记录课程的创建时刻',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `code`(`code` ASC) USING BTREE COMMENT '课程口令唯一索引，确保口令不重复',
  UNIQUE INDEX `idx_user_subject_name`(`user_id` ASC, `subject_name` ASC, `is_deleted` ASC) USING BTREE COMMENT '教师范围内课程名唯一，防止同一教师创建同名课程'
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of t_subject
-- ----------------------------

-- ================================
-- 课程题库关联表
-- 功能：存储课程与题库的关联关系
-- 主要内容：题库ID、课程ID、创建者ID、创建时间
-- 业务说明：定义课程可以使用哪些题库的题目，教师可关联多个题库到课程
-- ================================
DROP TABLE IF EXISTS `t_subject_exercise`;
CREATE TABLE `t_subject_exercise`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '课程题库关联ID，主键自增',
  `repo_id` int NULL DEFAULT NULL COMMENT '题库ID，关联t_repo表(id)，标识可用的题库',
  `subject_id` int NULL DEFAULT NULL COMMENT '课程ID，关联t_subject表(id)，标识所属课程',
  `user_id` int NULL DEFAULT NULL COMMENT '创建者ID，关联t_user表(id)，标识关联操作的执行者',
  `create_time` datetime NULL DEFAULT NULL COMMENT '关联时间，记录题库与课程关联的时刻',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of t_subject_exercise
-- ----------------------------

-- ================================
-- 用户表
-- 功能：存储普通用户（教师和学生）的账户信息
-- 主要内容：用户编号、姓名、邮箱、密码、角色、教师资格证号、头像
-- 业务说明：系统的主要用户表，区分教师（role_id=1）和学生（role_id=2），支持注册和登录
-- ================================
DROP TABLE IF EXISTS `t_user`;
CREATE TABLE `t_user`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '用户ID，主键自增',
  `user_no` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '用户编号（学号/工号），唯一标识用户身份，用于登录认证',
  `real_name` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '真实姓名，用户的真实姓名用于展示',
  `mail` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '邮箱地址，用于接收通知和找回密码',
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '登录密码，使用BCrypt加密存储',
  `role_id` int NULL DEFAULT 2 COMMENT '角色ID，关联t_role表(id)：0-管理员，1-教师，2-学生',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间，记录用户注册时刻',
  `status` int NULL DEFAULT 1 COMMENT '账户状态：1-正常（可登录），0-禁用（禁止登录）',
  `is_deleted` int NOT NULL DEFAULT 0 COMMENT '逻辑删除标记：0-未删除，1-已注销',
  `teacher_cert_no` varchar(17) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '教师资格证编号，仅教师角色需填写，用于教师身份验证',
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT 'http://118.178.88.163:9000/examsystem/avatar/student.jpg' COMMENT '用户头像URL，存储头像图片地址',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `user_no`(`user_no` ASC) USING BTREE COMMENT '用户编号唯一索引，确保学号/工号不重复'
) ENGINE = InnoDB AUTO_INCREMENT = 1001 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of t_user
-- ----------------------------
INSERT INTO `t_user` VALUES (1001, 'T260001', '教师测试账号', '1878679942@qq.com', '$2a$10$iKq8GBiFdG0iEoRQuITwQOX/PZzNQ5qN43CpVflqfVXO5UBD5l08S', 1, '2026-02-24 11:48:07', 1, 0, '34010252601234567', 'http://118.178.88.163:9000/examsystem/avatar/teacher.jpg');
INSERT INTO `t_user` VALUES (1002, 'S26000001', '学生测试账号', '1878679942@qq.com', '$2a$10$WSSOLBkkgQ10CjBtjgM3qOVFQt.wiNVQpedArBD/d2JBxB/860wc.', 2, '2026-02-24 11:48:48', 1, 0, '', 'http://118.178.88.163:9000/examsystem/avatar/student.jpg');

-- ================================
-- 用户每日登录时长表
-- 功能：统计用户每日在平台的累计在线时长
-- 主要内容：用户ID、登录日期、累计在线秒数
-- 业务说明：记录用户的登录时长数据，用于统计分析用户活跃度和学习时长
-- ================================
DROP TABLE IF EXISTS `t_user_daily_login_duration`;
CREATE TABLE `t_user_daily_login_duration`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '记录ID，主键自增',
  `user_id` int NULL DEFAULT NULL COMMENT '用户ID，关联t_user表(id)，标识统计的用户',
  `login_date` date NULL DEFAULT NULL COMMENT '登录日期，统计哪一天的在线时长，格式：YYYY-MM-DD',
  `total_seconds` int NULL DEFAULT NULL COMMENT '累计在线秒数，当日用户累计在线的总时长，单位：秒',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of t_user_daily_login_duration
-- ----------------------------

-- ================================
-- 用户考试成绩表
-- 功能：存储用户参加考试的成绩和状态记录
-- 主要内容：用户ID、考试ID、总时长、用户得分、交卷时间、切屏次数、考试状态
-- 业务说明：记录用户每次参加考试的结果，包含进行中（state=0）和已完成（state=1）两种状态
-- ================================
DROP TABLE IF EXISTS `t_user_exams_score`;
CREATE TABLE `t_user_exams_score`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '考试记录ID，主键自增',
  `user_id` int NULL DEFAULT NULL COMMENT '用户ID，关联t_user表(id)，标识参加考试的用户',
  `exam_id` int NULL DEFAULT NULL COMMENT '考试ID，关联t_exam表(id)，标识参加的考试',
  `total_time` bigint NULL DEFAULT NULL COMMENT '考试总时长，单位为毫秒，记录考试的完整时间限制',
  `user_time` bigint NULL DEFAULT NULL COMMENT '用户实际用时，单位为毫秒，记录用户实际花费的考试时间',
  `user_score` int UNSIGNED NULL DEFAULT 0 COMMENT '用户得分，记录用户的最终考试成绩分数',
  `limit_time` datetime NULL DEFAULT NULL COMMENT '交卷截止时间，定义用户必须交卷的最晚时间点',
  `count` int NULL DEFAULT 0 COMMENT '切屏次数，记录考生在考试过程中切换页面的次数',
  `state` int NULL DEFAULT NULL COMMENT '考试状态：0-正在进行（未交卷），1-已完成（已交卷）',
  `create_time` datetime NULL DEFAULT NULL COMMENT '开始考试时间，记录用户进入考试的时刻',
  `whether_mark` int NULL DEFAULT NULL COMMENT '阅卷状态：-1-无简答题无需阅卷，0-待阅卷（含简答未批改），1-已完成阅卷',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `user_id`(`user_id` ASC, `exam_id` ASC) USING BTREE COMMENT '联合唯一索引，确保每个用户每场考试只有一条记录'
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of t_user_exams_score
-- ----------------------------

-- ================================
-- 用户刷题记录表
-- 功能：存储用户在各题库的刷题进度统计
-- 主要内容：用户ID、题库ID、总题数、已刷题数、刷题时间
-- 业务说明：记录用户在题库的刷题进度，用于展示刷题完成情况
-- ================================
DROP TABLE IF EXISTS `t_user_exercise_record`;
CREATE TABLE `t_user_exercise_record`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '刷题记录ID，主键自增',
  `user_id` int NULL DEFAULT NULL COMMENT '用户ID，关联t_user表(id)，标识刷题用户',
  `repo_id` int NULL DEFAULT NULL COMMENT '题库ID，关联t_repo表(id)，标识刷题的题库',
  `total_count` int NULL DEFAULT NULL COMMENT '总题数，该题库的题目总数量',
  `exercise_count` int NULL DEFAULT NULL COMMENT '已刷题数，用户已完成作答的题目数量',
  `create_time` datetime NULL DEFAULT NULL COMMENT '首次刷题时间，记录用户开始刷题的时刻',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of t_user_exercise_record
-- ----------------------------

-- ================================
-- 用户课程关联表
-- 功能：存储用户（学生）与课程的订阅关系
-- 主要内容：用户ID、课程ID、加入时间
-- 业务说明：学生通过输入课程口令加入课程，建立用户与课程的关联，可查看课程公告和参加课程考试
-- ================================
DROP TABLE IF EXISTS `t_user_subject`;
CREATE TABLE `t_user_subject`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '用户课程关联ID，主键自增',
  `user_id` int NULL DEFAULT NULL COMMENT '用户ID，关联t_user表(id)，标识加入课程的用户',
  `subject_id` int NULL DEFAULT NULL COMMENT '课程ID，关联t_subject表(id)，标识加入的课程',
  `is_deleted` int NULL DEFAULT 0 COMMENT '逻辑删除标记：0-未退出（正常），1-已退出（退课后标记）',
  `join_time` datetime NULL DEFAULT NULL COMMENT '加入时间，记录用户加入课程的时刻',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `user_subject`(`user_id` ASC, `subject_id` ASC) USING BTREE COMMENT '联合唯一索引，确保用户不会重复加入同一课程'
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of t_user_subject
-- ----------------------------

-- ================================
-- IP白名单表
-- 功能：存储允许访问管理员接口的IP地址列表
-- 主要内容：IP地址、IP类型、描述、状态、创建时间
-- 业务说明：用于管理员登录IP限制，支持单个IP、网段、CIDR三种格式，增强管理员账户安全性
-- ================================
DROP TABLE IF EXISTS `t_ip_whitelist`;
CREATE TABLE `t_ip_whitelist`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '白名单规则ID，主键自增',
  `ip_address` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT 'IP地址/网段/CIDR，支持格式：单个IP（192.168.1.1）、网段（192.168.1.0/24）、CIDR（10.0.0.0/8）',
  `ip_type` int NULL DEFAULT 1 COMMENT 'IP地址类型：1-单个IP，2-网段，3-CIDR格式',
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '规则描述，说明此IP规则的用途或来源',
  `status` int NULL DEFAULT 1 COMMENT '规则状态：1-启用（生效），0-禁用（不生效）',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间，记录规则的创建时刻',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间，记录规则的最后修改时刻',
  `is_deleted` int NOT NULL DEFAULT 0 COMMENT '逻辑删除标记：0-未删除（正常使用），1-已删除',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of t_ip_whitelist
-- ----------------------------
INSERT INTO `t_ip_whitelist` VALUES (1, '127.0.0.1', 1, '本机地址，允许本地开发调试', 1, '2026-01-01 00:00:00', NULL, 0);
INSERT INTO `t_ip_whitelist` VALUES (2, '192.168.0.0/16', 3, '内网网段，允许B类内网地址段', 1, '2026-01-01 00:00:00', NULL, 0);
INSERT INTO `t_ip_whitelist` VALUES (3, '10.0.0.0/8', 3, '内网A类地址，允许A类内网地址段', 1, '2026-01-01 00:00:00', NULL, 0);

SET FOREIGN_KEY_CHECKS = 1;
