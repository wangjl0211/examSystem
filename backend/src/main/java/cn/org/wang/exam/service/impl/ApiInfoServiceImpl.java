package cn.org.wang.exam.service.impl;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import cn.org.wang.exam.common.exception.ApiInfoException;
import cn.org.wang.exam.service.IApiInfoService;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 接口信息服务实现类
 * 用于获取项目的实际接口信息
 *
 * @author Wang
 * @since 2026-03-21
 */
@Service
public class ApiInfoServiceImpl implements IApiInfoService {

    // 控制器包路径
    private static final String CONTROLLER_PACKAGE = "cn.org.wang.exam.controller";
    // 常量定义
    private static final String PARAMS_KEY = "params";
    private static final String REQ_EXAMPLE_KEY = "reqExample";
    private static final String RES_EXAMPLE_KEY = "resExample";
    private static final String METHOD_KEY = "method";
    private static final String PATH_KEY = "path";
    private static final String DESC_KEY = "desc";
    private static final String STATUS_KEY = "status";
    private static final String CATEGORY_KEY = "category";
    private static final String ENABLED_STATUS = "已启用";
    private static final String DEFAULT_DESC = "无描述";

    @Override
    public List<Map<String, Object>> getAllApiInfo() {
        List<Map<String, Object>> apiInfoList = new ArrayList<>();

        try {
            // 扫描控制器包，获取所有控制器类
            List<Class<?>> controllerClasses = scanControllerClasses(CONTROLLER_PACKAGE);

            // 遍历控制器类
            for (Class<?> controllerClass : controllerClasses) {
                processControllerClass(controllerClass, apiInfoList);
            }
        } catch (ApiInfoException e) {
            // 如果获取失败，返回空列表
            return new ArrayList<>();
        } catch (Exception e) {
            // 其他异常转换为 ApiInfoException
            throw new ApiInfoException("获取接口信息失败", e);
        }

        return apiInfoList;
    }

    /**
     * 处理控制器类，提取接口信息
     * @param controllerClass 控制器类
     * @param apiInfoList 接口信息列表
     * @throws ApiInfoException 接口信息服务异常
     */
    private void processControllerClass(Class<?> controllerClass, List<Map<String, Object>> apiInfoList) throws ApiInfoException {
        // 获取控制器类上的@RequestMapping注解，获取基础路径
        String basePath = getControllerBasePath(controllerClass);

        // 遍历控制器类中的所有方法
        Method[] methods = controllerClass.getDeclaredMethods();
        for (Method method : methods) {
            processControllerMethod(controllerClass, method, basePath, apiInfoList);
        }
    }

    /**
     * 获取控制器基础路径
     * @param controllerClass 控制器类
     * @return 基础路径
     */
    private String getControllerBasePath(Class<?> controllerClass) {
        String basePath = "";
        if (controllerClass.isAnnotationPresent(RequestMapping.class)) {
            RequestMapping requestMapping = controllerClass.getAnnotation(RequestMapping.class);
            if (requestMapping.value().length > 0) {
                basePath = requestMapping.value()[0];
            }
        }
        return basePath;
    }

    /**
     * 处理控制器方法，提取接口信息
     * @param controllerClass 控制器类
     * @param method 方法
     * @param basePath 基础路径
     * @param apiInfoList 接口信息列表
     */
    private void processControllerMethod(Class<?> controllerClass, Method method, String basePath, List<Map<String, Object>> apiInfoList) {
        // 检查方法是否有HTTP方法注解（GET, POST, PUT, DELETE等）
        String httpMethod = getHttpMethod(method);
        if (httpMethod == null) {
            return; // 跳过没有HTTP方法注解的方法
        }

        // 获取方法上的路径注解，获取方法路径
        String methodPath = getMethodPath(method);
        if (methodPath == null) {
            return; // 跳过没有路径注解的方法
        }

        // 构建完整路径
        String fullPath = basePath + methodPath;

        // 获取方法上的@Operation注解，获取接口描述
        String desc = getMethodDescription(method);

        // 构建接口信息
        Map<String, Object> apiInfo = createApiInfo(controllerClass, httpMethod, fullPath, desc);

        // 处理请求参数
        processRequestParameters(method, apiInfo);

        // 处理请求体示例
        processRequestBodyExample(method, apiInfo);

        // 处理响应示例
        processResponseExample(apiInfo);

        apiInfoList.add(apiInfo);
    }

    /**
     * 获取方法描述
     * @param method 方法
     * @return 方法描述
     */
    private String getMethodDescription(Method method) {
        String desc = DEFAULT_DESC;
        if (method.isAnnotationPresent(Operation.class)) {
            Operation operation = method.getAnnotation(Operation.class);
            desc = operation.summary() != null && !operation.summary().isEmpty() ? operation.summary() : desc;
        }
        return desc;
    }

    /**
     * 创建接口信息
     * @param controllerClass 控制器类
     * @param httpMethod HTTP方法
     * @param fullPath 完整路径
     * @param desc 描述
     * @return 接口信息
     */
    private Map<String, Object> createApiInfo(Class<?> controllerClass, String httpMethod, String fullPath, String desc) {
        Map<String, Object> apiInfo = new HashMap<>();
        apiInfo.put(METHOD_KEY, httpMethod);
        apiInfo.put(PATH_KEY, fullPath);
        apiInfo.put(DESC_KEY, desc);
        apiInfo.put(STATUS_KEY, ENABLED_STATUS); // 默认接口状态为启用
        // 添加分类信息（基于控制器类名）
        String category = getControllerCategory(controllerClass);
        apiInfo.put(CATEGORY_KEY, category);
        return apiInfo;
    }

    /**
     * 处理请求参数
     * @param method 方法
     * @param apiInfo 接口信息
     */
    private void processRequestParameters(Method method, Map<String, Object> apiInfo) {
        List<Map<String, Object>> params = new ArrayList<>();
        // 获取方法参数
        java.lang.reflect.Parameter[] methodParams = method.getParameters();
        for (java.lang.reflect.Parameter param : methodParams) {
            Map<String, Object> paramInfo = new HashMap<>();
            paramInfo.put("name", param.getName());
            paramInfo.put("pos", getParameterPosition(param));
            paramInfo.put("type", param.getType().getSimpleName());
            paramInfo.put("required", isParameterRequired(param));
            paramInfo.put("desc", DEFAULT_DESC); // 反射无法直接获取参数描述
            params.add(paramInfo);
        }
        apiInfo.put(PARAMS_KEY, params);
    }

    /**
     * 处理请求体示例
     * @param method 方法
     * @param apiInfo 接口信息
     */
    private void processRequestBodyExample(Method method, Map<String, Object> apiInfo) {
        Map<String, Object> reqExample = new HashMap<>();
        // 简单生成请求体示例
        for (java.lang.reflect.Parameter param : method.getParameters()) {
            if (param.isAnnotationPresent(RequestBody.class)) {
                reqExample.put("示例参数", "示例值");
                break;
            }
        }
        apiInfo.put(REQ_EXAMPLE_KEY, reqExample);
    }

    /**
     * 处理响应示例
     * @param apiInfo 接口信息
     */
    private void processResponseExample(Map<String, Object> apiInfo) {
        Map<String, Object> resExample = new HashMap<>();
        resExample.put("code", 200);
        resExample.put("msg", "成功");
        resExample.put("data", new HashMap<>());
        apiInfo.put(RES_EXAMPLE_KEY, resExample);
    }

    @Override
    public Map<String, Object> getApiStats() {
        Map<String, Object> stats = new HashMap<>();

        try {
            // 获取所有接口信息
            List<Map<String, Object>> apiInfoList = getAllApiInfo();

            // 统计总接口数
            stats.put("totalApiCount", apiInfoList.size());

            // 按HTTP方法统计
            Map<String, Integer> methodStats = new HashMap<>();
            for (Map<String, Object> apiInfo : apiInfoList) {
                String method = (String) apiInfo.get(METHOD_KEY);
                methodStats.put(method, methodStats.getOrDefault(method, 0) + 1);
            }
            stats.put("methodStats", methodStats);

            // 统计启用的接口数
            long enabledCount = apiInfoList.stream()
                    .filter(apiInfo -> ENABLED_STATUS.equals(apiInfo.get(STATUS_KEY)))
                    .count();
            stats.put("enabledApiCount", enabledCount);

            // 统计平均参数数
            double avgParamCount = apiInfoList.stream()
                    .mapToInt(apiInfo -> {
                        int paramCount = 0;
                        if (apiInfo.get(PARAMS_KEY) instanceof List) {
                            List<?> paramsList = (List<?>) apiInfo.get(PARAMS_KEY);
                            paramCount = paramsList.size();
                        }
                        return paramCount;
                    })
                    .average()
                    .orElse(0);
            stats.put("avgParamCount", Math.round(avgParamCount * 100) / 100.0);

        } catch (ApiInfoException e) {
            // 如果获取失败，返回默认统计信息
            stats.put("totalApiCount", 0);
            stats.put("methodStats", new HashMap<>());
            stats.put("enabledApiCount", 0);
            stats.put("avgParamCount", 0);
        } catch (Exception e) {
            // 其他异常转换为 ApiInfoException
            throw new ApiInfoException("获取接口统计信息失败", e);
        }

        return stats;
    }

    /**
     * 扫描指定包下的所有控制器类
     * @param packageName 包名
     * @return 控制器类列表
     * @throws ApiInfoException 接口信息服务异常
     */
    private List<Class<?>> scanControllerClasses(String packageName) throws ApiInfoException {
        List<Class<?>> controllerClasses = new ArrayList<>();

        // 使用Spring的ClassPathScanningCandidateComponentProvider进行包扫描
        org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider scanner = 
            new org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider(false);

        // 添加过滤器，只扫描带有@RestController或@Controller注解的类
        scanner.addIncludeFilter(new org.springframework.core.type.filter.AnnotationTypeFilter(
            org.springframework.web.bind.annotation.RestController.class));
        scanner.addIncludeFilter(new org.springframework.core.type.filter.AnnotationTypeFilter(
            org.springframework.stereotype.Controller.class));

        // 扫描指定包
        java.util.Set<org.springframework.beans.factory.config.BeanDefinition> beanDefinitions = 
            scanner.findCandidateComponents(packageName);

        // 遍历扫描结果，获取类对象
        for (org.springframework.beans.factory.config.BeanDefinition beanDefinition : beanDefinitions) {
            try {
                String className = beanDefinition.getBeanClassName();
                if (className != null) {
                    Class<?> controllerClass = Class.forName(className);
                    controllerClasses.add(controllerClass);
                }
            } catch (ClassNotFoundException e) {
                // 忽略找不到的类
            }
        }

        return controllerClasses;
    }

    /**
     * 获取方法的HTTP方法
     * @param method 方法
     * @return HTTP方法
     */
    private String getHttpMethod(Method method) {
        if (method.isAnnotationPresent(GetMapping.class)) {
            return "GET";
        } else if (method.isAnnotationPresent(PostMapping.class)) {
            return "POST";
        } else if (method.isAnnotationPresent(PutMapping.class)) {
            return "PUT";
        } else if (method.isAnnotationPresent(DeleteMapping.class)) {
            return "DELETE";
        } else if (method.isAnnotationPresent(PatchMapping.class)) {
            return "PATCH";
        }
        return null;
    }

    /**
     * 获取方法的路径
     * @param method 方法
     * @return 方法路径
     */
    private String getMethodPath(Method method) {
        if (method.isAnnotationPresent(GetMapping.class)) {
            GetMapping getMapping = method.getAnnotation(GetMapping.class);
            return getMapping.value().length > 0 ? getMapping.value()[0] : "";
        } else if (method.isAnnotationPresent(PostMapping.class)) {
            PostMapping postMapping = method.getAnnotation(PostMapping.class);
            return postMapping.value().length > 0 ? postMapping.value()[0] : "";
        } else if (method.isAnnotationPresent(PutMapping.class)) {
            PutMapping putMapping = method.getAnnotation(PutMapping.class);
            return putMapping.value().length > 0 ? putMapping.value()[0] : "";
        } else if (method.isAnnotationPresent(DeleteMapping.class)) {
            DeleteMapping deleteMapping = method.getAnnotation(DeleteMapping.class);
            return deleteMapping.value().length > 0 ? deleteMapping.value()[0] : "";
        } else if (method.isAnnotationPresent(PatchMapping.class)) {
            PatchMapping patchMapping = method.getAnnotation(PatchMapping.class);
            return patchMapping.value().length > 0 ? patchMapping.value()[0] : "";
        }
        return null;
    }

    /**
     * 获取参数的位置
     * @param param 参数
     * @return 参数位置
     */
    private String getParameterPosition(java.lang.reflect.Parameter param) {
        if (param.isAnnotationPresent(PathVariable.class)) {
            return "path";
        } else if (param.isAnnotationPresent(RequestParam.class)) {
            return "query";
        } else if (param.isAnnotationPresent(RequestBody.class)) {
            return "body";
        } else if (param.isAnnotationPresent(RequestHeader.class)) {
            return "header";
        }
        return "unknown";
    }

    /**
     * 判断参数是否必填
     * @param param 参数
     * @return 是否必填
     */
    private boolean isParameterRequired(java.lang.reflect.Parameter param) {
        if (param.isAnnotationPresent(RequestParam.class)) {
            RequestParam requestParam = param.getAnnotation(RequestParam.class);
            return requestParam.required();
        } else if (param.isAnnotationPresent(PathVariable.class)) {
            PathVariable pathVariable = param.getAnnotation(PathVariable.class);
            return pathVariable.required();
        } else if (param.isAnnotationPresent(RequestBody.class)) {
            return true; // RequestBody默认是必填的
        }
        return false;
    }

    /**
     * 根据控制器类名获取分类信息
     * @param controllerClass 控制器类
     * @return 分类名称
     */
    private String getControllerCategory(Class<?> controllerClass) {
        String className = controllerClass.getSimpleName();
        // 移除Controller后缀
        if (className.endsWith("Controller")) {
            className = className.substring(0, className.length() - "Controller".length());
        }
        // 转换为中文分类名称
        Map<String, String> categoryMap = new HashMap<>();
        categoryMap.put("Auth", "认证相关");
        categoryMap.put("User", "用户管理");
        categoryMap.put("Exam", "考试管理");
        categoryMap.put("Question", "题目管理");
        categoryMap.put("Subject", "课程管理");
        categoryMap.put("Score", "成绩管理");
        categoryMap.put("Record", "记录管理");
        categoryMap.put("Stat", "统计分析");
        categoryMap.put("ApiInfo", "接口信息");
        categoryMap.put("File", "文件管理");
        categoryMap.put("Notice", "通知管理");
        categoryMap.put("Discussion", "讨论管理");
        categoryMap.put("Exercise", "练习管理");
        categoryMap.put("Repo", "题库管理");
        categoryMap.put("Category", "分类管理");
        categoryMap.put("Like", "点赞管理");
        categoryMap.put("Reply", "回复管理");
        categoryMap.put("Teacher", "教师管理");
        categoryMap.put("UserBook", "用户书架");
        categoryMap.put("Answer", "答案管理");
        
        // 如果有映射，返回中文名称，否则返回原类名
        return categoryMap.getOrDefault(className, className);
    }

}
