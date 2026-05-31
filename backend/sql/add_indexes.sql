-- =====================================================
-- 在线考试系统 - 数据库索引优化脚本
-- =====================================================

-- t_option 表：题目ID高频查询
ALTER TABLE t_option ADD INDEX idx_qu_id(qu_id);

-- t_question 表：题库ID高频查询
ALTER TABLE t_question ADD INDEX idx_repo_id(repo_id);

-- t_exam_question 表：考试ID查询
ALTER TABLE t_exam_question ADD INDEX idx_exam_id(exam_id);

-- t_exam_qu_answer 表：用户+考试联合查询
ALTER TABLE t_exam_qu_answer ADD INDEX idx_user_exam(user_id, exam_id);

-- t_user_exams_score 表：用户成绩查询
ALTER TABLE t_user_exams_score ADD INDEX idx_user_id(user_id);

-- t_discussion 表：课程讨论查询
ALTER TABLE t_discussion ADD INDEX idx_subject_id(subject_id);

-- t_notice 表：公告查询（按时间排序）
ALTER TABLE t_notice ADD INDEX idx_create_time(create_time);

-- t_log 表：日志查询
ALTER TABLE t_log ADD INDEX idx_user_id(user_id);
ALTER TABLE t_log ADD INDEX idx_create_time(create_time);

-- t_like 表：点赞查询
ALTER TABLE t_like ADD INDEX idx_discussion_id(discussion_id);

-- t_reply 表：回复查询
ALTER TABLE t_reply ADD INDEX idx_discussion_id(discussion_id);

-- t_exercise_record 表：练习记录查询
ALTER TABLE t_exercise_record ADD INDEX idx_user_repo(user_id, repo_id);

-- t_user_daily_login_duration 表：用户登录时长查询
ALTER TABLE t_user_daily_login_duration ADD INDEX idx_user_date(user_id, login_date);

-- t_manual_score 表：手动评分查询
ALTER TABLE t_manual_score ADD INDEX idx_exam_qu_answer_id(exam_qu_answer_id);

-- t_subject_exercise 表：科目练习查询
ALTER TABLE t_subject_exercise ADD INDEX idx_subject_id(subject_id);

-- t_user_subject 表：用户科目关联查询
ALTER TABLE t_user_subject ADD INDEX idx_user_id(user_id);