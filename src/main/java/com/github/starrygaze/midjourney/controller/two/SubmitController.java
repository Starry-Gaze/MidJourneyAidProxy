package com.github.starrygaze.midjourney.controller.two;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.RandomUtil;
import com.github.starrygaze.midjourney.ProxyProperties;
import com.github.starrygaze.midjourney.dto.two.SubmitChangeDTO;
import com.github.starrygaze.midjourney.dto.two.SubmitDescribeDTO;
import com.github.starrygaze.midjourney.dto.two.SubmitImagineDTO;
import com.github.starrygaze.midjourney.dto.two.SubmitSimpleChangeDTO;
import com.github.starrygaze.midjourney.dto.base.BaseSubmitDTO;
import com.github.starrygaze.midjourney.entity.Task;
import com.github.starrygaze.midjourney.entity.two.TaskChangeParams;
import com.github.starrygaze.midjourney.enums.two.TaskAction;
import com.github.starrygaze.midjourney.enums.TaskStatus;
import com.github.starrygaze.midjourney.find.ReturnCode;
import com.github.starrygaze.midjourney.service.store.TaskStoreService;
import com.github.starrygaze.midjourney.service.task.TaskService;
import com.github.starrygaze.midjourney.service.translate.TranslateService;
import com.github.starrygaze.midjourney.support.TaskCondition;
import com.github.starrygaze.midjourney.util.two.BannedPromptUtils;
import com.github.starrygaze.midjourney.util.ConvertUtils;
import com.github.starrygaze.midjourney.util.MimeTypeUtils;
import com.github.starrygaze.midjourney.vo.SubmitResultVO;
import eu.maxschuster.dataurl.DataUrl;
import eu.maxschuster.dataurl.DataUrlSerializer;
import eu.maxschuster.dataurl.IDataUrlSerializer;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.MalformedURLException;
import java.util.Set;

@Api(tags = "任务提交")
@RestController
@RequestMapping("/submit")
@RequiredArgsConstructor
public class SubmitController {
	private final TranslateService translateService;
	private final TaskStoreService taskStoreService;
	private final ProxyProperties properties;
	private final TaskService taskService;

	@ApiOperation(value = "提交Imagine任务")
	@PostMapping("/imagine")
	public SubmitResultVO imagine(@RequestBody SubmitImagineDTO imagineDTO) {
		String prompt = imagineDTO.getPrompt();
		if (CharSequenceUtil.isBlank(prompt)) {
			return SubmitResultVO.fail(ReturnCode.VALIDATION_ERROR, "prompt不能为空");
		}
		Task task = newTask(imagineDTO);
		task.setAction2(TaskAction.IMAGINE);
		task.setPrompt(prompt);
		String promptEn;
		int paramStart = prompt.indexOf(" --");
		if (paramStart > 0) {
			promptEn = this.translateService.translateToEnglish(prompt.substring(0, paramStart)).trim() + prompt.substring(paramStart);
		} else {
			promptEn = this.translateService.translateToEnglish(prompt).trim();
		}
		if (BannedPromptUtils.isBanned(promptEn)) {
			return SubmitResultVO.fail(ReturnCode.BANNED_PROMPT, "可能包含敏感词");
		}
		task.setPromptEn(promptEn);
		task.setFinalPrompt("[" + task.getId() + "] " + promptEn);
		task.setDescription("/imagine " + imagineDTO.getPrompt());
		return this.taskService.submitImagine2(task);
	}

	@ApiOperation(value = "绘图变化-simple")
	@PostMapping("/simple-change")
	public SubmitResultVO simpleChange(@RequestBody SubmitSimpleChangeDTO simpleChangeDTO) {
		TaskChangeParams changeParams = ConvertUtils.convertChangeParams(simpleChangeDTO.getContent());
		if (changeParams == null) {
			return SubmitResultVO.fail(ReturnCode.VALIDATION_ERROR, "content参数错误");
		}
		SubmitChangeDTO changeDTO = new SubmitChangeDTO();
		changeDTO.setAction(changeParams.getAction());
		changeDTO.setTaskId(changeParams.getId());
		changeDTO.setIndex(changeParams.getIndex());
		changeDTO.setState(simpleChangeDTO.getState());
		changeDTO.setNotifyHook(simpleChangeDTO.getNotifyHook());
		return change(changeDTO);
	}

	@ApiOperation(value = "绘图变化")
	@PostMapping("/change")
	public SubmitResultVO change(@RequestBody SubmitChangeDTO changeDTO) {
		if (CharSequenceUtil.isBlank(changeDTO.getTaskId())) {
			return SubmitResultVO.fail(ReturnCode.VALIDATION_ERROR, "taskId不能为空");
		}
		if (!Set.of(TaskAction.UPSCALE, TaskAction.VARIATION, TaskAction.REROLL).contains(changeDTO.getAction())) {
			return SubmitResultVO.fail(ReturnCode.VALIDATION_ERROR, "action参数错误");
		}
		String description = "/up " + changeDTO.getTaskId();
		if (TaskAction.REROLL.equals(changeDTO.getAction())) {
			description += " R";
		} else {
			description += " " + changeDTO.getAction().name().charAt(0) + changeDTO.getIndex();
		}
		TaskCondition condition = new TaskCondition().setDescription(description);
		Task existTask = this.taskStoreService.findOne(condition);
		if (existTask != null) {
			return SubmitResultVO.of(ReturnCode.EXISTED, "任务已存在", existTask.getId())
					.setProperty("status", existTask.getStatus())
					.setProperty("imageUrl", existTask.getImageUrl());
		}
		Task targetTask = this.taskStoreService.get(changeDTO.getTaskId());
		if (targetTask == null) {
			return SubmitResultVO.fail(ReturnCode.NOT_FOUND, "任务不存在或已失效");
		}
		if (!TaskStatus.SUCCESS.equals(targetTask.getStatus())) {
			return SubmitResultVO.fail(ReturnCode.VALIDATION_ERROR, "关联任务状态错误");
		}
		Task task = newTask(changeDTO);
		task.setAction2(changeDTO.getAction());
		task.setPrompt(targetTask.getPrompt());
		task.setPromptEn(targetTask.getPromptEn());
		task.setFinalPrompt(targetTask.getFinalPrompt());
		task.setRelatedTaskId(ConvertUtils.findTaskIdByFinalPrompt(targetTask.getFinalPrompt()));
		task.setDescription(description);
		if (TaskAction.UPSCALE.equals(changeDTO.getAction())) {
			return this.taskService.submitUpscale2(task, targetTask.getMessageId(), targetTask.getMessageHash(), changeDTO.getIndex());
		} else if (TaskAction.VARIATION.equals(changeDTO.getAction())) {
			return this.taskService.submitVariation2(task, targetTask.getMessageId(), targetTask.getMessageHash(), changeDTO.getIndex());
		} else {
			return SubmitResultVO.fail(ReturnCode.VALIDATION_ERROR, "不支持的操作: " + changeDTO.getAction());
		}
	}

	@ApiOperation(value = "提交Describe任务")
	@PostMapping("/describe")
	public SubmitResultVO describe(@RequestBody SubmitDescribeDTO describeDTO) {
		if (CharSequenceUtil.isBlank(describeDTO.getBase64())) {
			return SubmitResultVO.fail(ReturnCode.VALIDATION_ERROR, "base64不能为空");
		}
		IDataUrlSerializer serializer = new DataUrlSerializer();
		DataUrl dataUrl;
		try {
			dataUrl = serializer.unserialize(describeDTO.getBase64());
		} catch (MalformedURLException e) {
			return SubmitResultVO.fail(ReturnCode.VALIDATION_ERROR, "base64格式错误");
		}
		Task task = newTask(describeDTO);
		task.setAction2(TaskAction.DESCRIBE);
		String taskFileName = task.getId() + "." + MimeTypeUtils.guessFileSuffix(dataUrl.getMimeType());
		task.setDescription("/describe " + taskFileName);
		return this.taskService.submitDescribe2(task, dataUrl);
	}

	private Task newTask(BaseSubmitDTO base) {
		Task task = new Task();
		task.setId(RandomUtil.randomNumbers(16));
		task.setSubmitTime(System.currentTimeMillis());
		task.setState(base.getState());
		task.setNotifyHook(CharSequenceUtil.isBlank(base.getNotifyHook()) ? this.properties.getNotifyHook() : base.getNotifyHook());
		return task;
	}
}
