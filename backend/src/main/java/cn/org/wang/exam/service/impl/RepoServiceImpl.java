package cn.org.wang.exam.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import cn.org.wang.exam.common.exception.ServiceRuntimeException;
import cn.org.wang.exam.common.result.Result;
import cn.org.wang.exam.mapper.QuestionMapper;
import cn.org.wang.exam.mapper.RepoMapper;
import cn.org.wang.exam.mapper.UserMapper;
import cn.org.wang.exam.mapper.UserSubjectMapper;
import cn.org.wang.exam.model.entity.*;
import cn.org.wang.exam.model.vo.exercise.ExerciseRepoVO;
import cn.org.wang.exam.model.vo.repo.RepoListVO;
import cn.org.wang.exam.model.vo.repo.RepoVO;
import cn.org.wang.exam.service.ICategoryService;
import cn.org.wang.exam.service.IRepoService;
import cn.org.wang.exam.service.ISubjectExerciseService;
import cn.org.wang.exam.service.ISubjectService;
import cn.org.wang.exam.utils.SecurityUtil;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 题库管理服务实现类
 *
 * @author Wang
 * @since 2026-03-21
 */
@Service
public class RepoServiceImpl extends ServiceImpl<RepoMapper, Repo> implements IRepoService {
    // 常量定义
    private static final String GET_REPO_LIST_SUCCESS = "分页获取可刷题库列表成功";
    private static final String UPDATE_REPO_COURSES_SUCCESS = "更新题库课程关联成功";
    private static final String GET_REPO_COURSES_SUCCESS = "获取题库课程关联成功";
    
    @Resource
    private RepoMapper repoMapper;
    @Resource
    private QuestionMapper questionMapper;
    @Resource
    private UserSubjectMapper userSubjectMapper;
    @Resource
    private UserMapper userMapper;
    @Resource
    private ICategoryService categoryService;
    @Resource
    private ISubjectService subjectService;
    @Resource
    private ISubjectExerciseService subjectExerciseService;

    @Override
    public Result<String> addRepo(Repo repo) {
        // 检查分类ID是否存在
        if (repo.getCategoryId() != null) {
            Category category = categoryService.getById(repo.getCategoryId());
            if (category == null) {
                return Result.failed("分类不存在");
            }
        }
        
        int row = repoMapper.insert(repo);
        if (row > 0) {
            return Result.success("新增题库成功");
        }
        throw new ServiceRuntimeException("添加题库条数<1");
    }

    @Override
    public Result<String> updateRepo(Repo repo, Integer id) {
        // 检查题库是否属于当前用户
        Integer userId = SecurityUtil.getUserId();
        Repo existingRepo = repoMapper.selectById(id);
        if (existingRepo == null) {
            return Result.failed("题库不存在");
        }
        if (!existingRepo.getUserId().equals(userId)) {
            return Result.failed("无权修改此题库");
        }
        
        // 检查分类ID是否存在
        if (repo.getCategoryId() != null) {
            Category category = categoryService.getById(repo.getCategoryId());
            if (category == null) {
                return Result.failed("分类不存在");
            }
        }
        
        // 修改题库
        LambdaUpdateWrapper<Repo> updateWrapper = new LambdaUpdateWrapper<Repo>()
                .eq(Repo::getId, id)
                .set(Repo::getTitle, repo.getTitle())
                .set(Repo::getIsExercise, repo.getIsExercise())
                .set(repo.getCategoryId() != null, Repo::getCategoryId, repo.getCategoryId());
        int row = repoMapper.update(updateWrapper);
        if (row > 0) {
            return Result.success("修改题库成功");
        }
        throw new ServiceRuntimeException("修改题库条数<1");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> deleteRepoById(Integer id) {
        // 检查题库是否属于当前用户
        Integer userId = SecurityUtil.getUserId();
        Repo existingRepo = repoMapper.selectById(id);
        if (existingRepo == null) {
            return Result.failed("题库不存在");
        }
        if (!existingRepo.getUserId().equals(userId)) {
            return Result.failed("无权删除此题库");
        }
        
        // 题库内试题清空所属题库id
        LambdaUpdateWrapper<Question> wrapper = new LambdaUpdateWrapper<Question>()
                .eq(Question::getRepoId, id)
                .set(Question::getRepoId, null);
        questionMapper.update(wrapper);
        // 删除题库
        boolean result = this.removeById(id);
        if (result) {
            return Result.success("删除题库成功");
        }
        throw new ServiceRuntimeException("删除题库条数<1");
    }

    @Override
    public Result<List<RepoListVO>> getRepoList(String repoTitle) {
        List<RepoListVO> list;
        Integer roleCode = SecurityUtil.getRoleCode();
        Integer userId = SecurityUtil.getUserId();
        if (roleCode == 1 || roleCode == 2) {
            // 教师和学生只能查看自己的题库
            list = repoMapper.selectRepoList(repoTitle, userId);
        } else {
            // 管理员可以查看所有题库
            list = repoMapper.selectRepoList(repoTitle, 0);
        }
        return Result.success("根据用户id获取自己的题库获取成功", list);
    }

    @Override
    public Result<IPage<RepoVO>> pagingRepo(Integer pageNum, Integer pageSize, String title, Integer categoryId) {
        IPage<RepoVO> page = new Page<>(pageNum, pageSize);
        Integer roleCode = SecurityUtil.getRoleCode();
        Integer userId = SecurityUtil.getUserId();
        if (roleCode == 1 || roleCode == 2) {
            // 教师和学生只能查看自己的题库
            page = repoMapper.pagingRepo(page, title, userId, categoryId);
        } else {
            // 管理员可以查看所有题库
            page = repoMapper.pagingRepo(page, title, 0, categoryId);
        }
        
        // 为每个题库设置分类信息和题目数量
        List<RepoVO> records = page.getRecords();
        for (RepoVO vo : records) {
            // 设置分类信息
            if (vo.getCategoryId() != null) {
                // 查询分类信息
                Category category = categoryService.getById(vo.getCategoryId());
                if (category != null) {
                    vo.setCategoryName(category.getCategoryName());
                }
            }
            
            // 查询题库中的题目数量
            LambdaQueryWrapper<Question> questionWrapper = new LambdaQueryWrapper<>();
            questionWrapper.eq(Question::getRepoId, vo.getId());
            int count = questionMapper.selectCount(questionWrapper).intValue();
            vo.setQuestionCount(count);
        }
        
        return Result.success("题库分页查询成功", page);
    }

    @Override
    public Result<IPage<ExerciseRepoVO>> getRepo(Integer pageNum, Integer pageSize, String title, Integer categoryId, Integer subjectId) {
        IPage<ExerciseRepoVO> page = new Page<>(pageNum, pageSize);
        // 获取当前用户ID和角色
        Integer userId = SecurityUtil.getUserId();
        Integer roleCode = SecurityUtil.getRoleCode();
        
        // 根据用户角色查询题库
        if (roleCode == 0 || roleCode == 1) {
            // 管理员或教师，查询可刷题库
            queryAdminTeacherRepos(page, title, categoryId, userId);
        } else {
            // 学生用户，只能查看与自己课程关联的题库
            if (!queryStudentRepos(page, title, categoryId, subjectId, userId)) {
                return Result.success(GET_REPO_LIST_SUCCESS, page);
            }
        }
        
        // 为每个题库设置分类信息
        setCategoryInfoForRepos(page.getRecords());
        
        return Result.success(GET_REPO_LIST_SUCCESS, page);
    }

    /**
     * 查询管理员和教师的可刷题库
     * @param page 分页对象
     * @param title 标题
     * @param categoryId 分类ID
     * @param userId 用户ID
     */
    private void queryAdminTeacherRepos(IPage<ExerciseRepoVO> page, String title, Integer categoryId, Integer userId) {
        // 获取当前用户角色
        Integer roleCode = SecurityUtil.getRoleCode();
        
        if (roleCode == 0) {
            // 管理员可以查看所有题库
            repoMapper.selectRepo(page, title, null, categoryId);
        } else if (roleCode == 1) {
            // 教师只能查看自己创建的题库
            List<Integer> userList = new ArrayList<>();
            userList.add(userId);
            repoMapper.selectRepo(page, title, userList, categoryId);
        }
    }

    /**
     * 查询学生的可刷题库
     * @param page 分页对象
     * @param title 标题
     * @param categoryId 分类ID
     * @param subjectId 课程ID
     * @param userId 用户ID
     * @return 是否查询到题库
     */
    private boolean queryStudentRepos(IPage<ExerciseRepoVO> page, String title, Integer categoryId, Integer subjectId, Integer userId) {
        // 获取当前学生所在课程ID列表
        List<UserSubject> userSubjects = getUserSubjects(userId, subjectId);
        
        if (userSubjects.isEmpty()) {
            // 如果学生没有加入任何课程或指定的课程不存在，返回空列表
            return false;
        }
        
        // 获取学生所在的所有课程ID
        List<Integer> subjectIds = userSubjects.stream()
                .map(UserSubject::getGId)
                .toList();
        
        // 获取这些课程关联的题库ID
        List<SubjectExercise> subjectExercises = getSubjectExercises(subjectIds);
        
        if (subjectExercises.isEmpty()) {
            // 如果没有关联的题库，返回空列表
            return false;
        }
        
        // 获取关联的题库ID列表
        List<Integer> repoIds = subjectExercises.stream()
                .map(SubjectExercise::getRepoId)
                .distinct()
                .toList();
        
        // 学生用户，只需要根据课程关联的题库ID来查询
        // 不需要过滤题库的创建者，因为我们已经通过SubjectExercise表过滤了与课程关联的题库
        // 查询可以刷的题库，条件是没有删除的公开的且在关联列表中
        repoMapper.selectRepoByRepoIds(page, title, null, categoryId, repoIds);
        return true;
    }

    /**
     * 获取用户课程关联
     * @param userId 用户ID
     * @param subjectId 课程ID
     * @return 用户课程关联列表
     */
    private List<UserSubject> getUserSubjects(Integer userId, Integer subjectId) {
        LambdaQueryWrapper<UserSubject> userSubjectWrapper = new LambdaQueryWrapper<>();
        userSubjectWrapper.eq(UserSubject::getUId, userId)
                        .eq(UserSubject::getIsDeleted, 0);
        
        // 如果指定了课程ID，只查询该课程
        if (subjectId != null) {
            userSubjectWrapper.eq(UserSubject::getGId, subjectId);
        }
        
        return userSubjectMapper.selectList(userSubjectWrapper);
    }

    /**
     * 获取课程题库关联
     * @param subjectIds 课程ID列表
     * @return 课程题库关联列表
     */
    private List<SubjectExercise> getSubjectExercises(List<Integer> subjectIds) {
        LambdaQueryWrapper<SubjectExercise> seWrapper = new LambdaQueryWrapper<>();
        seWrapper.in(SubjectExercise::getSubjectId, subjectIds);
        return subjectExerciseService.list(seWrapper);
    }

    /**
     * 为题库设置分类信息
     * @param records 题库记录列表
     */
    private void setCategoryInfoForRepos(List<ExerciseRepoVO> records) {
        for (ExerciseRepoVO vo : records) {
            setCategoryInfoForRepo(vo);
        }
    }
    
    /**
     * 为单个题库设置分类信息
     * @param vo 题库VO对象
     */
    private void setCategoryInfoForRepo(ExerciseRepoVO vo) {
        if (vo.getCategoryId() == null) {
            return;
        }
        
        Category category = categoryService.getById(vo.getCategoryId());
        if (category == null) {
            return;
        }
        
        vo.setCategoryName(category.getCategoryName());
        setParentCategoryInfo(vo, category);
    }
    
    /**
     * 设置父分类信息
     * @param vo 题库VO对象
     * @param category 分类对象
     */
    private void setParentCategoryInfo(ExerciseRepoVO vo, Category category) {
        if (category.getParentId() == null || category.getParentId() <= 0) {
            return;
        }
        
        vo.setParentCategoryId(category.getParentId());
        Category parentCategory = categoryService.getById(category.getParentId());
        if (parentCategory != null) {
            vo.setParentCategoryName(parentCategory.getCategoryName());
        }
    }
    
    @Override
    public Result<IPage<RepoVO>> getReposByCategory(Integer categoryId, Integer pageNum, Integer pageSize) {
        // 查询该分类下的所有子分类ID
        List<Integer> categoryIds = new ArrayList<>();
        categoryIds.add(categoryId);
        
        // 如果是一级分类，还需要查询其下的所有二级分类
        LambdaQueryWrapper<Category> categoryWrapper = new LambdaQueryWrapper<>();
        categoryWrapper.eq(Category::getParentId, categoryId);
        List<Category> childCategories = categoryService.list(categoryWrapper);
        if (!childCategories.isEmpty()) {
            List<Integer> childIds = childCategories.stream()
                    .map(Category::getId)
                    .toList();
            categoryIds.addAll(childIds);
        }
        
        // 查询题库
        Page<Repo> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Repo> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(Repo::getCategoryId, categoryIds)
               .orderByDesc(Repo::getCreateTime);
        
        // 如果是教师或学生，只能查看自己创建的题库
        Integer userId = SecurityUtil.getUserId();
        Integer roleCode = SecurityUtil.getRoleCode();
        if (roleCode == 1 || roleCode == 2) {
            wrapper.eq(Repo::getUserId, userId);
        }
        
        IPage<Repo> repoPage = page(page, wrapper);
        
        // 转换为VO
        IPage<RepoVO> result = repoPage.convert(repo -> {
            RepoVO vo = new RepoVO();
            BeanUtils.copyProperties(repo, vo);
            
            // 设置分类名称
            if (repo.getCategoryId() != null) {
                Category category = categoryService.getById(repo.getCategoryId());
                if (category != null) {
                    vo.setCategoryName(category.getCategoryName());
                }
            }
            
            return vo;
        });
        
        return Result.success("根据分类查询题库成功", result);
    }
    
    @Override
    public Result<List<Subject>> getTeacherCourses() {
        Integer userId = SecurityUtil.getUserId();
        // 查询教师创建的所有课程
        LambdaQueryWrapper<Subject> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Subject::getUserId, userId)
               .eq(Subject::getIsDeleted, 0)
               .orderByDesc(Subject::getCreateTime);
        List<Subject> courses = subjectService.list(wrapper);
        return Result.success("获取教师课程列表成功", courses);
    }
    
    @Override
    public Result<String> updateRepoCourses(Integer repoId, List<Integer> courseIds) {
        // 检查题库是否存在
        Repo repo = getById(repoId);
        if (repo == null) {
            return Result.failed("题库不存在");
        }
        
        // 检查题库是否属于当前教师
        Integer userId = SecurityUtil.getUserId();
        if (!repo.getUserId().equals(userId)) {
            return Result.failed("无权操作此题库");
        }
        
        // 删除原有的课程关联
        LambdaQueryWrapper<SubjectExercise> deleteWrapper = new LambdaQueryWrapper<>();
        deleteWrapper.eq(SubjectExercise::getRepoId, repoId);
        subjectExerciseService.remove(deleteWrapper);
        
        // 添加新的课程关联
        if (courseIds != null && !courseIds.isEmpty()) {
            List<SubjectExercise> subjectExercises = new ArrayList<>();
            for (Integer subjectId : courseIds) {
                // 检查课程是否存在
                Subject subject = subjectService.getById(subjectId);
                if (subject != null && subject.getUserId().equals(userId)) {
                    SubjectExercise se = new SubjectExercise();
                    se.setRepoId(repoId);
                    se.setSubjectId(subjectId);
                    se.setUserId(userId);
                    subjectExercises.add(se);
                }
            }
            if (!subjectExercises.isEmpty()) {
                subjectExerciseService.saveBatch(subjectExercises);
            }
        }
        
        return Result.success(UPDATE_REPO_COURSES_SUCCESS);
    }
    
    @Override
    public Result<List<Subject>> getRepoCourses(Integer repoId) {
        // 检查题库是否存在
        Repo repo = getById(repoId);
        if (repo == null) {
            return Result.failed("题库不存在");
        }
        
        // 检查题库是否属于当前教师
        Integer userId = SecurityUtil.getUserId();
        if (!repo.getUserId().equals(userId)) {
            return Result.failed("无权操作此题库");
        }
        
        // 获取题库关联的课程ID
        LambdaQueryWrapper<SubjectExercise> seWrapper = new LambdaQueryWrapper<>();
        seWrapper.eq(SubjectExercise::getRepoId, repoId);
        List<SubjectExercise> subjectExercises = subjectExerciseService.list(seWrapper);
        
        if (subjectExercises.isEmpty()) {
            return Result.success(GET_REPO_COURSES_SUCCESS, new ArrayList<>());
        }
        
        // 获取课程信息
        List<Integer> subjectIds = subjectExercises.stream()
                .map(SubjectExercise::getSubjectId)
                .toList();
        
        List<Subject> subjects = subjectService.listByIds(subjectIds);
        
        return Result.success(GET_REPO_COURSES_SUCCESS, subjects);
    }
}
