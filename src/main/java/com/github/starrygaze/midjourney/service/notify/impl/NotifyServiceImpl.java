package com.github.starrygaze.midjourney.service.notify.impl;

import cn.hutool.core.text.CharSequenceUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.starrygaze.midjourney.entity.Task;
import com.github.starrygaze.midjourney.service.notify.NotifyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * 这个类名为 NotifyServiceImpl 是一个实现 NotifyService 接口的服务类。它主要负责对任务状态的变更进行通知。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotifyServiceImpl implements NotifyService {
	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	/**
	 * notifyTaskChange(Task task): 这个方法接收一个任务 (Task) 对象作为参数。首先，检查任务对象的 notifyHook 字段，如果为空，则直接返回。
	 * 如果不为空，那么将任务对象序列化为 JSON 格式的字符串，并打印一条调试信息。然后，调用 postJson 方法将任务对象的 JSON 字符串通过 HTTP POST 方法发送到 notifyHook 指定的 URL。
	 * 如果在这个过程中发生异常，将会打印一条警告信息。
	 * @param task
	 */
	@Override
	public void notifyTaskChange(Task task) {
		String notifyHook = task.getNotifyHook();
		if (CharSequenceUtil.isBlank(notifyHook)) {
			return;
		}
		try {
			String paramsStr = OBJECT_MAPPER.writeValueAsString(task);
			log.debug("任务变更, 触发推送, task: {}", paramsStr);
			postJson(notifyHook, paramsStr);
		} catch (Exception e) {
			log.warn("回调通知接口失败: {}", e.getMessage());
		}
	}

	/**
	 * postJson(String notifyHook, String paramsJson): 这个方法接收一个通知 URL (notifyHook) 和一个 JSON 格式的字符串 (paramsJson) 作为参数。
	 * 然后，设置 HTTP 头部的内容类型为 JSON，并将 paramsJson 作为 HTTP 请求的主体。最后，通过 HTTP POST 方法发送这个请求到 notifyHook 指定的 URL。
	 * 如果响应的状态码为 OK，则直接返回。否则，打印一条警告信息，包括响应的状态码和响应主体。
	 * @param notifyHook
	 * @param paramsJson
	 */
	private void postJson(String notifyHook, String paramsJson) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<String> httpEntity = new HttpEntity<>(paramsJson, headers);
		ResponseEntity<String> responseEntity = new RestTemplate().postForEntity(notifyHook, httpEntity, String.class);
		if (responseEntity.getStatusCode() == HttpStatus.OK) {
			return;
		}
		log.warn("回调通知接口失败, code: {}, msg: {}", responseEntity.getStatusCodeValue(), responseEntity.getBody());
	}

}
