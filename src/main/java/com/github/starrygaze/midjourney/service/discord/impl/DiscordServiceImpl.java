package com.github.starrygaze.midjourney.service.discord.impl;

import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.github.starrygaze.midjourney.ProxyProperties;
import com.github.starrygaze.midjourney.result.Message;
import com.github.starrygaze.midjourney.service.discord.DiscordService;
import eu.maxschuster.dataurl.DataUrl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import javax.annotation.PostConstruct;

/**
 * 这个类名为 DiscordServiceImpl 是一个实现 DiscordService 接口的服务类。它主要负责和 Discord API 进行交互。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DiscordServiceImpl implements DiscordService {

	private final ProxyProperties properties;

	private static final String DISCORD_API_URL = "https://discord.com/api/v9/interactions";

	private String userAgent;

	private String discordUploadUrl;

	private String imagineParamsJson;

	private String upscaleParamsJson;

	private String variationParamsJson;

	private String resetParamsJson;

	private String describeParamsJson;

	private String discordUserToken;

	private String discordGuildId;

	private String discordChannelId;

	/**
	 * @PostConstruct init(): 这个是一个初始化方法，它在类实例化之后自动被调用。该方法从配置类中读取必要的 Discord 属性和参数，并读取预定的 API 参数模板。
	 */
	@PostConstruct
	void init() {
		this.discordUserToken = this.properties.getDiscord().getUserToken();
		this.discordGuildId = this.properties.getDiscord().getGuildId();
		this.discordChannelId = this.properties.getDiscord().getChannelId();
		this.discordUploadUrl = "https://discord.com/api/v9/channels/" + this.discordChannelId + "/attachments";
		this.userAgent = this.properties.getDiscord().getUserAgent();
		this.imagineParamsJson = ResourceUtil.readUtf8Str("api-params/imagine.json");
		this.upscaleParamsJson = ResourceUtil.readUtf8Str("api-params/upscale.json");
		this.variationParamsJson = ResourceUtil.readUtf8Str("api-params/variation.json");
		this.resetParamsJson = ResourceUtil.readUtf8Str("api-params/reset.json");
		this.describeParamsJson = ResourceUtil.readUtf8Str("api-params/describe.json");
	}

	/**
	 * imagine(String prompt): 这个方法接受一个文本提示 (prompt) 并用该提示替换 API 请求模板中的对应部分。然后，通过 Discord API 发送请求，可能是执行一些操作或者获取一些信息。
	 * @param prompt
	 * @return
	 */
	@Override
	public Message<Void> imagine(String prompt) {
		String paramsStr = this.imagineParamsJson.replace("$guild_id", this.discordGuildId)
				.replace("$channel_id", this.discordChannelId);
		JSONObject params = new JSONObject(paramsStr);
		params.getJSONObject("data").getJSONArray("options").getJSONObject(0)
				.put("value", prompt);
		return postJsonAndCheckStatus(params.toString());
	}

	/**
	 * upscale(String messageId, int index, String messageHash): 这个方法接收一个消息ID、索引和消息哈希值，然后用这些值替换 API 请求模板中的对应部分。
	 * 发送这个请求可能是为了执行一些操作，如提升图片的分辨率。
	 * @param messageId
	 * @param index
	 * @param messageHash
	 * @return
	 */
	@Override
	public Message<Void> upscale(String messageId, int index, String messageHash) {
		String paramsStr = this.upscaleParamsJson.replace("$guild_id", this.discordGuildId)
				.replace("$channel_id", this.discordChannelId)
				.replace("$message_id", messageId)
				.replace("$index", String.valueOf(index))
				.replace("$message_hash", messageHash);
		return postJsonAndCheckStatus(paramsStr);
	}

	/**
	 * variation(String messageId, int index, String messageHash): 这个方法与 upscale 方法类似，但执行的操作可能不同。
	 * @param messageId
	 * @param index
	 * @param messageHash
	 * @return
	 */
	@Override
	public Message<Void> variation(String messageId, int index, String messageHash) {
		String paramsStr = this.variationParamsJson.replace("$guild_id", this.discordGuildId)
				.replace("$channel_id", this.discordChannelId)
				.replace("$message_id", messageId)
				.replace("$index", String.valueOf(index))
				.replace("$message_hash", messageHash);
		return postJsonAndCheckStatus(paramsStr);
	}

	/**
	 * reset(String messageId, String messageHash): 这个方法接收一个消息ID和消息哈希值，并用这些值替换 API 请求模板中的对应部分。发送这个请求可能是为了执行一些重置操作。
	 * @param messageId
	 * @param messageHash
	 * @return
	 */
	@Override
	public Message<Void> reset(String messageId, String messageHash) {
		String paramsStr = this.resetParamsJson.replace("$guild_id", this.discordGuildId)
				.replace("$channel_id", this.discordChannelId)
				.replace("$message_id", messageId)
				.replace("$message_hash", messageHash);
		return postJsonAndCheckStatus(paramsStr);
	}

	/**
	 * upload(String fileName, DataUrl dataUrl): 这个方法接收一个文件名和一个包含文件数据的 DataUrl。然后，它将文件上传到 Discord。
	 * 如果上传成功，它会返回一个包含上传文件名的成功消息，否则它将返回一个错误消息。
	 * @param fileName
	 * @param dataUrl
	 * @return
	 */
	@Override
	public Message<String> upload(String fileName, DataUrl dataUrl) {
		try {
			JSONObject fileObj = new JSONObject();
			fileObj.put("filename", fileName);
			fileObj.put("file_size", dataUrl.getData().length);
			fileObj.put("id", "0");
			JSONObject params = new JSONObject()
					.put("files", new JSONArray().put(fileObj));
			ResponseEntity<String> responseEntity = postJson(this.discordUploadUrl, params.toString());
			if (responseEntity.getStatusCode() != HttpStatus.OK) {
				log.error("上传图片到discord失败, status: {}, msg: {}", responseEntity.getStatusCodeValue(), responseEntity.getBody());
				return Message.of(Message.VALIDATION_ERROR_CODE, "上传图片到discord失败");
			}
			JSONArray array = new JSONObject(responseEntity.getBody()).getJSONArray("attachments");
			if (array.length() == 0) {
				return Message.of(Message.VALIDATION_ERROR_CODE, "上传图片到discord失败");
			}
			String uploadUrl = array.getJSONObject(0).getString("upload_url");
			String uploadFilename = array.getJSONObject(0).getString("upload_filename");
			putFile(uploadUrl, dataUrl);
			return Message.success(uploadFilename);
		} catch (Exception e) {
			log.error("上传图片到discord失败", e);
			return Message.of(Message.FAILURE_CODE, "上传图片到discord失败");
		}
	}

	/**
	 * describe(String finalFileName): 这个方法接收一个最终文件名，并用这个文件名替换 API 请求模板中的对应部分。发送这个请求可能是为了执行一些描述或标记操作。
	 * @param finalFileName
	 * @return
	 */
	@Override
	public Message<Void> describe(String finalFileName) {
		String fileName = CharSequenceUtil.subAfter(finalFileName, "/", true);
		String paramsStr = this.describeParamsJson.replace("$guild_id", this.discordGuildId)
				.replace("$channel_id", this.discordChannelId)
				.replace("$file_name", fileName)
				.replace("$final_file_name", finalFileName);
		return postJsonAndCheckStatus(paramsStr);
	}

	/**
	 * putFile(String uploadUrl, DataUrl dataUrl): 这个私有方法接收一个上传 URL 和一个包含文件数据的 DataUrl，然后使用 RESTful API 将文件数据上传到给定的 URL。
	 * @param uploadUrl
	 * @param dataUrl
	 */
	private void putFile(String uploadUrl, DataUrl dataUrl) {
		HttpHeaders headers = new HttpHeaders();
		headers.add("User-Agent", this.userAgent);
		headers.setContentType(MediaType.valueOf(dataUrl.getMimeType()));
		headers.setContentLength(dataUrl.getData().length);
		HttpEntity<byte[]> requestEntity = new HttpEntity<>(dataUrl.getData(), headers);
		new RestTemplate().put(uploadUrl, requestEntity);
	}

	/*
	
	 */
	private ResponseEntity<String> postJson(String paramsStr) {
		return postJson(DISCORD_API_URL, paramsStr);
	}

	private ResponseEntity<String> postJson(String url, String paramsStr) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("Authorization", this.discordUserToken);
		headers.add("User-Agent", this.userAgent);
		HttpEntity<String> httpEntity = new HttpEntity<>(paramsStr, headers);
		return new RestTemplate().postForEntity(url, httpEntity, String.class);
	}

	private Message<Void> postJsonAndCheckStatus(String paramsStr) {
		try {
			ResponseEntity<String> responseEntity = postJson(paramsStr);
			if (responseEntity.getStatusCode() == HttpStatus.NO_CONTENT) {
				return Message.success();
			}
			return Message.of(responseEntity.getStatusCodeValue(), CharSequenceUtil.sub(responseEntity.getBody(), 0, 100));
		} catch (HttpClientErrorException e) {
			try {
				JSONObject error = new JSONObject(e.getResponseBodyAsString());
				return Message.of(error.optInt("code", e.getRawStatusCode()), error.optString("message"));
			} catch (Exception je) {
				return Message.of(e.getRawStatusCode(), CharSequenceUtil.sub(e.getMessage(), 0, 100));
			}
		}
	}
}
