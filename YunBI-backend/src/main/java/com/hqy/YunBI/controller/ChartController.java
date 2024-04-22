package com.hqy.YunBI.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hqy.YunBI.annotation.AuthCheck;
import com.hqy.YunBI.bizmq.BiMessageProducer;
import com.hqy.YunBI.common.BaseResponse;
import com.hqy.YunBI.common.DeleteRequest;
import com.hqy.YunBI.common.ErrorCode;
import com.hqy.YunBI.common.ResultUtils;
import com.hqy.YunBI.constant.CommonConstant;
import com.hqy.YunBI.constant.UserConstant;
import com.hqy.YunBI.exception.BusinessException;
import com.hqy.YunBI.exception.ThrowUtils;
import com.hqy.YunBI.manager.AiManager;
import com.hqy.YunBI.manager.RedisLimiterManager;
import com.hqy.YunBI.model.dto.chart.*;
import com.hqy.YunBI.model.entity.Chart;
import com.hqy.YunBI.model.entity.User;
import com.hqy.YunBI.model.vo.BiResponse;
import com.hqy.YunBI.service.ChartService;
import com.hqy.YunBI.service.UserService;
import com.hqy.YunBI.utils.ExcelUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;


/**
 * 帖子接口
 *
 * @author hqy
 * 
 */
@RestController
@RequestMapping("/chart")
@Slf4j
public class ChartController {

    @Resource
    private ChartService chartService;

    @Resource
    private UserService userService;

    @Resource
    private AiManager aiManager;

    @Resource
    private RedisLimiterManager redisLimiterManager;
    // region 增删改查
    @Resource
    private ThreadPoolExecutor threadPoolExecutor;
    @Resource
    private BiMessageProducer biMessageProducer;

    /**
     * 创建
     *
     * @param chartAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<Long> addChart(@RequestBody ChartAddRequest chartAddRequest, HttpServletRequest request) {
        if (chartAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Chart chart = new Chart();
        BeanUtils.copyProperties(chartAddRequest, chart);

        User loginUser = userService.getLoginUser(request);
        chart.setUserId(loginUser.getId());

        boolean result = chartService.save(chart);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        long newChartId = chart.getId();
        return ResultUtils.success(newChartId);
    }

    /**
     * 删除
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteChart(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getLoginUser(request);
        long id = deleteRequest.getId();
        // 判断是否存在
        Chart oldChart = chartService.getById(id);
        ThrowUtils.throwIf(oldChart == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可删除
        if (!oldChart.getUserId().equals(user.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean b = chartService.removeById(id);
        return ResultUtils.success(b);
    }

    /**
     * 更新（仅管理员）
     *
     * @param chartUpdateRequest
     * @return
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateChart(@RequestBody ChartUpdateRequest chartUpdateRequest) {
        if (chartUpdateRequest == null || chartUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Chart chart = new Chart();
        BeanUtils.copyProperties(chartUpdateRequest, chart);

        // 参数校验
//        chartService.validChart(chart, false);
        long id = chartUpdateRequest.getId();
        // 判断是否存在
        Chart oldChart = chartService.getById(id);
        ThrowUtils.throwIf(oldChart == null, ErrorCode.NOT_FOUND_ERROR);
        boolean result = chartService.updateById(chart);
        return ResultUtils.success(result);
    }

    /**
     * 根据 id 获取
     *
     * @param id
     * @return
     */
    @GetMapping("/get/{id}")
    public BaseResponse<Chart> getChartById(@PathVariable long id, HttpServletRequest request) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Chart chart = chartService.getById(id);
        if (chart == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        return ResultUtils.success(chart);
    }

/*
    */
/**
     * 分页获取列表（仅管理员）
     *
     * @param chartQueryRequest
     * @return
     *//*

    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<Chart>> listChartByPage(@RequestBody ChartQueryRequest chartQueryRequest) {
        long current = chartQueryRequest.getCurrent();
        long size = chartQueryRequest.getPageSize();
        Page<Chart> chartPage = chartService.page(new Page<>(current, size),
                chartService.getQueryWrapper(chartQueryRequest));
        return ResultUtils.success(chartPage);
    }
*/

    /**
     * 分页获取列表（封装类）
     *
     * @param chartQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page")
    public BaseResponse<Page<Chart>> listChartByPage(@RequestBody ChartQueryRequest chartQueryRequest,
                                                       HttpServletRequest request) {
        long current = chartQueryRequest.getCurrent();
        long size = chartQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<Chart> chartPage = chartService.page(new Page<>(current, size),
                chartService.getQueryWrapper(chartQueryRequest));
        return ResultUtils.success(chartPage);
    }

    /**
     * 分页获取当前用户创建的资源列表
     *
     * @param chartQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/my/list/page")
    public BaseResponse<Page<Chart>> listMyChartByPage(@RequestBody ChartQueryRequest chartQueryRequest,
                                                         HttpServletRequest request) {
        if (chartQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        chartQueryRequest.setUserId(loginUser.getId());
        long current = chartQueryRequest.getCurrent();
        long size = chartQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<Chart> chartPage = chartService.page(new Page<>(current, size),
                chartService.getQueryWrapper(chartQueryRequest));
        return ResultUtils.success(chartPage);
    }

    // endregion


    /**
     * 编辑（图表）
     *
     * @param chartEditRequest
     * @param request
     * @return
     */
    @PostMapping("/edit")
    public BaseResponse<Boolean> editChart(@RequestBody ChartEditRequest chartEditRequest, HttpServletRequest request) {
        if (chartEditRequest == null || chartEditRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Chart chart = new Chart();
        BeanUtils.copyProperties(chartEditRequest, chart);

        User loginUser = userService.getLoginUser(request);
        long id = chartEditRequest.getId();
        // 判断是否存在
        Chart oldChart = chartService.getById(id);
        ThrowUtils.throwIf(oldChart == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可编辑
        if (!oldChart.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean result = chartService.updateById(chart);
        return ResultUtils.success(result);
    }
    /**
     * 智能分析文件上传
     *
     * @param multipartFile
     * @param fileRequest
     * @param request
     * @return
     */

    private static final long biModel = CommonConstant.BI_MODEL_ID;
    @PostMapping("/gen")
    public BaseResponse<BiResponse> genChartByAi(@RequestPart("file") MultipartFile multipartFile,
                                                 GenChartByAiFileRequest genChartByAiFileRequest, HttpServletRequest request) {
        String name = genChartByAiFileRequest.getName();
        String goal = genChartByAiFileRequest.getGoal();
        String chartType = genChartByAiFileRequest.getChartType();

        //校验参数
        ThrowUtils.throwIf(StringUtils.isNotBlank(name)&&name.length()>100,ErrorCode.PARAMS_ERROR,"名称过长");
        ThrowUtils.throwIf(StringUtils.isBlank(goal),ErrorCode.PARAMS_ERROR,"分析目标为空");

        // 用户输入
        StringBuilder  userInput = new StringBuilder();
        userInput.append("你是一个数据分析师和前端专家,接下来我会按照固定格式给你提供内容：" +
                "分析需求：" +
                "{数据分析的需求或者目标" +
                "原始数据：" +
                "【【【【【" +
                "{前端Echarts V5的option配置对象JSON代码，并严格使用双引号包裹JSON中的键,合理地将数据进行可视化,不要生成任何多余的内容比如注释}" +
                "【【【【【" +
                "{明确的数据分析结论、越详细越好，不要生成多余注释}").append("\n");
        String userGoal =goal;
        if(StringUtils.isNotBlank(chartType)){
            userGoal+= ",请使用"+chartType;
        }

        User loginUser =userService.getLoginUser(request);
        // 对每个用户的请求做限流
        redisLimiterManager.doRateLimit("genChartByAi_"+loginUser.getId());
        userInput.append("分析需求:").append(userGoal).append("\n");
        //压缩数据
        //读取用户上传Excel进行处理
        String chartData = ExcelUtils.excelToCsv(multipartFile);
        userInput.append("原始数据:").append(chartData).append("\n");

        String res = aiManager.doChat(biModel,userInput.toString());
        //对分析结果进行括号拆分
        String[] split = res.split("【【【【【");

        if(split.length<3){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"AI生成参数错误");
        }
        // 数据持久化
        String genData = split[1].trim();
        //AI生成数据提取Json字符串
        String genChart = genData.substring(genData.indexOf("{"),genData.lastIndexOf("}")+1).replace("'","\"");
        String genResult = split[2].trim();
        Chart chart = new Chart();
        chart.setGoal(goal);
        chart.setName(name);
        chart.setChartData(chartData);
        chart.setChartType(chartType);
        chart.setGenChart(genChart);
        chart.setGenResult(genResult);
        chart.setUserId(loginUser.getId());

        boolean saveResult = chartService.save(chart);
        ThrowUtils.throwIf(!saveResult,ErrorCode.SYSTEM_ERROR,"图表保存失败");

        //
        BiResponse biResponse = new BiResponse();
        biResponse.setGenChart(genChart);
        biResponse.setGenResult(genResult);
        biResponse.setChartId(chart.getId());

        return ResultUtils.success(biResponse);
    }

    /**
     * 智能分析图表(异步)
     * @param multipartFile
     * @param genChartByAiFileRequest
     * @param request
     * @return
     */
    @PostMapping("/gen/async")
    public BaseResponse<BiResponse> genChartByAiAsync(@RequestPart("file") MultipartFile multipartFile,
                                                 GenChartByAiFileRequest genChartByAiFileRequest, HttpServletRequest request) {
        String name = genChartByAiFileRequest.getName();
        String goal = genChartByAiFileRequest.getGoal();
        String chartType = genChartByAiFileRequest.getChartType();
        //校验参数
        ThrowUtils.throwIf(StringUtils.isNotBlank(name)&&name.length()>100,ErrorCode.PARAMS_ERROR,"名称过长");
        ThrowUtils.throwIf(StringUtils.isBlank(goal),ErrorCode.PARAMS_ERROR,"分析目标为空");

        //校验文件大小
        //获取文件大小单位bytes
        long size = multipartFile.getSize();
        final long ONE_MB = 1024*1024L;
        ThrowUtils.throwIf(size>ONE_MB,ErrorCode.PARAMS_ERROR);

        //校验文件后缀非法
        String originalFilename = multipartFile.getOriginalFilename();
        final List<String> validSuffixList = Arrays.asList("xlsx","xls");

        try {
            String suffix = originalFilename.substring(originalFilename.lastIndexOf(".")+1,originalFilename.length());
            if(!validSuffixList.contains(suffix)){
                throw new BusinessException(ErrorCode.PARAMS_ERROR,"文件类型非法");
            }
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,ErrorCode.PARAMS_ERROR.getMessage());
        }


        // 用户输入
        StringBuilder  userInput = new StringBuilder();
        userInput.append("你是一个数据分析师和前端专家,接下来我会按照固定格式给你提供内容：" +
                "分析需求：" +
                "{数据分析的需求或者目标" +
                "原始数据：" +
                "【【【【【" +
                "{前端Echarts V5的option配置对象JSON代码，并严格使用双引号包裹JSON中的键,合理地将数据进行可视化,不要生成任何多余的内容比如注释}" +
                "【【【【【" +
                "{明确的数据分析结论、越详细越好，不要生成多余注释}").append("\n");
        String userGoal =goal;
        if(StringUtils.isNotBlank(chartType)){
            userGoal+= ",请使用"+chartType;
        }

        User loginUser =userService.getLoginUser(request);
        // 对每个用户的请求做限流
        redisLimiterManager.doRateLimit("genChartByAi_"+loginUser.getId());
        userInput.append("分析需求:").append(userGoal).append("\n");
        //压缩数据
        //读取用户上传Excel进行处理
        String chartData = ExcelUtils.excelToCsv(multipartFile);
        userInput.append("原始数据:").append(chartData).append("\n");

        //先把请求数据存储到数据库
        Chart chart = new Chart();
        chart.setGoal(goal);
        chart.setName(name);
        chart.setChartData(chartData);
        chart.setChartType(chartType);
        chart.setUserId(loginUser.getId());
        chart.setStatus("wait");
        boolean saveResult = chartService.save(chart);
        ThrowUtils.throwIf(!saveResult,ErrorCode.SYSTEM_ERROR,"图表保存失败");

        //在最终返回前提交一个异步任务
        CompletableFuture.runAsync(new Runnable() {
            @Override
            public void run() {
                Chart updateChar = new Chart();
                updateChar.setId(chart.getId());
                //调用AI分析
                String res = aiManager.doChat(biModel,userInput.toString());
                //对分析结果进行括号拆分
                String[] split = res.split("【【【【【");

                if(split.length<3){
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR,"AI生成参数错误");
                }
                // 数据持久化
                String genData = split[1].trim();
                //AI生成数据提取Json字符串
                String genChart = genData.substring(genData.indexOf("{"),genData.lastIndexOf("}")+1).replace("'","\"");
                String genResult = split[2].trim();
                updateChar.setGenChart(genChart);
                updateChar.setGenResult(genResult);
                updateChar.setStatus("succeed");
                boolean updateResult = chartService.updateById(updateChar);
                ThrowUtils.throwIf(!updateResult,ErrorCode.SYSTEM_ERROR,"图表保存失败");
            }
        },threadPoolExecutor);
        //
        BiResponse biResponse = new BiResponse();
//        biResponse.setGenChart(genChart);
//        biResponse.setGenResult(genResult);
        biResponse.setChartId(chart.getId());

        return ResultUtils.success(biResponse);
    }
    /**
     * 消息队列 智能分析图表(异步)
     * @param multipartFile
     * @param genChartByAiFileRequest
     * @param request
     * @return
     */
    @PostMapping("/gen/async/mq")
    public BaseResponse<BiResponse> genChartByAiAsyncMq(@RequestPart("file") MultipartFile multipartFile,
                                                      GenChartByAiFileRequest genChartByAiFileRequest, HttpServletRequest request) {
        String name = genChartByAiFileRequest.getName();
        String goal = genChartByAiFileRequest.getGoal();
        String chartType = genChartByAiFileRequest.getChartType();
        //校验参数
        ThrowUtils.throwIf(StringUtils.isNotBlank(name)&&name.length()>100,ErrorCode.PARAMS_ERROR,"名称过长");
        ThrowUtils.throwIf(StringUtils.isBlank(goal),ErrorCode.PARAMS_ERROR,"分析目标为空");

        //校验文件大小
        //获取文件大小单位bytes
        long size = multipartFile.getSize();
        final long ONE_MB = 1024*1024L;
        ThrowUtils.throwIf(size>ONE_MB,ErrorCode.PARAMS_ERROR);

        //校验文件后缀非法
        String originalFilename = multipartFile.getOriginalFilename();
        final List<String> validSuffixList = Arrays.asList("xlsx","xls");

        try {
            String suffix = originalFilename.substring(originalFilename.lastIndexOf(".")+1,originalFilename.length());
            if(!validSuffixList.contains(suffix)){
                throw new BusinessException(ErrorCode.PARAMS_ERROR,"文件类型非法");
            }
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,ErrorCode.PARAMS_ERROR.getMessage());
        }

        Chart chart = new Chart();
        //发送消息处理分析业务逻辑
        //先把请求数据存储到数据库
        chart.setGoal(goal);
        chart.setName(name);
        String chartData = ExcelUtils.excelToCsv(multipartFile);
        chart.setChartData(chartData);
        chart.setChartType(chartType);
        User loginUser =userService.getLoginUser(request);
        chart.setUserId(loginUser.getId());
        chart.setStatus("wait");
        boolean saveResult = chartService.save(chart);
        ThrowUtils.throwIf(!saveResult,ErrorCode.SYSTEM_ERROR,"图表保存失败");

        //发送消息
        long chartId = chart.getId();
        biMessageProducer.sentMessage(String.valueOf(chartId));

        // 对每个用户的请求做限流
        redisLimiterManager.doRateLimit("genChartByAi_"+loginUser.getId());


        //
        BiResponse biResponse = new BiResponse();
        biResponse.setChartId(chartId);
        return ResultUtils.success(biResponse);
    }


}
