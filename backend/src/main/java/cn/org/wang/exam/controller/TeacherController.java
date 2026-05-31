package cn.org.wang.exam.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;

import cn.org.wang.exam.common.result.Result;
import cn.org.wang.exam.mapper.UserMapper;
import cn.org.wang.exam.model.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 教师管理接口
 *
 * @author Wang
 * @Version
 * @Date 2026/5/20 10:00 AM
 */
@Tag(name = "教师管理接口")
@RestController
@RequestMapping("/api/teacher")
public class TeacherController {

    @Resource
    private UserMapper userMapper;

    /**
     * 验证教师证件编号唯一性
     *
     * @param teacherCertNo 教师资格证件编号
     * @return 验证结果
     */
    @Operation(summary = "验证教师证件编号唯一性")
    @GetMapping("/check-cert")
    public Result<String> checkTeacherCert(@RequestParam("teacherCertNo") String teacherCertNo) {
        if (StringUtils.isBlank(teacherCertNo)) {
            return Result.failed("教师资格证编号不能为空");
        }

        // 检查证件编号是否已存在
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getTeacherCertNo, teacherCertNo);
        User existingUser = userMapper.selectOne(wrapper);
        if (existingUser != null) {
            return Result.failed("教师资格证编号已存在");
        }

        return Result.success("教师资格证编号可用");
    }

}
