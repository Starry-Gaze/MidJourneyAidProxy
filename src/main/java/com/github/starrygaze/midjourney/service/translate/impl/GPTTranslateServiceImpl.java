package com.github.starrygaze.midjourney.service.translate.impl;


import cn.hutool.core.text.CharSequenceUtil;
import com.github.starrygaze.midjourney.ProxyProperties;
import com.github.starrygaze.midjourney.service.translate.TranslateService;
import com.theokanning.openai.completion.chat.ChatCompletionChoice;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.service.OpenAiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.support.BeanDefinitionValidationException;

import java.util.List;

/**
 * 这个类叫做 GPTTranslateServiceImpl，它实现了 TranslateService 接口。根据类名和代码内容，我们可以看出这个类是用来提供基于 OpenAI GPT (Generative Pre-training Transformer) 的翻译服务。
 * 这个类提供了一个基于 OpenAI GPT 的翻译服务，可以将中文文本翻译成英语文本。尽管 OpenAI GPT 不是专门的翻译模型，但是由于其强大的语言生成和理解能力，也可以用来做一些简单的翻译任务。
 */
@Slf4j
public class GPTTranslateServiceImpl implements TranslateService {

	/**
	 * 初始化（构造函数）：在构造函数中，接收一个类型为 ProxyProperties.OpenaiConfig 的参数 openaiConfig，并从这个参数中获取了 OpenAI GPT 所需要的 API 密钥。
	 * 如果这个密钥为空，将会抛出一个 BeanDefinitionValidationException 异常。然后，使用这个 API 密钥和超时时间创建一个 OpenAiService 实例。
	 */
	private final OpenAiService openAiService;
	private final ProxyProperties.OpenaiConfig openaiConfig;

	public GPTTranslateServiceImpl(ProxyProperties.OpenaiConfig openaiConfig) {
		if (CharSequenceUtil.isBlank(openaiConfig.getGptApiKey())) {
			throw new BeanDefinitionValidationException("mj-proxy.openai.gpt-api-key未配置");
		}
		this.openaiConfig = openaiConfig;
		this.openAiService = new OpenAiService(openaiConfig.getGptApiKey(), openaiConfig.getTimeout());
	}

	/**
	 * translateToEnglish(String prompt)：这个方法用来将输入的文本翻译成英语。首先，它会检查输入的文本是否包含中文，如果不包含，直接返回原来的文本，否则会进行翻译。
	 * 翻译的过程中，它会创建两个 ChatMessage 实例，一个是系统消息，告诉 GPT 需要将中文翻译成英文，另一个是用户消息，包含了需要翻译的中文。然后，使用这两个消息创建一个
	 * ChatCompletionRequest 实例。接下来，使用 OpenAiService 来执行这个请求，并获取返回的结果。从结果中提取出翻译后的英文并返回。如果在调用过程中出现任何异常，
	 * 都会被捕获并打印警告信息，然后返回原来的文本。
	 * @param prompt
	 * @return
	 */
	@Override
	public String translateToEnglish(String prompt) {
		if (!containsChinese(prompt)) {
			return prompt;
		}
		ChatMessage m1 = new ChatMessage("system", "把中文翻译成英文");
		ChatMessage m2 = new ChatMessage("user", prompt);
		ChatCompletionRequest request = ChatCompletionRequest.builder()
				.model(this.openaiConfig.getModel())
				.temperature(this.openaiConfig.getTemperature())
				.maxTokens(this.openaiConfig.getMaxTokens())
				.messages(List.of(m1, m2))
				.build();
		try {
			List<ChatCompletionChoice> choices = this.openAiService.createChatCompletion(request).getChoices();
			if (!choices.isEmpty()) {
				return choices.get(0).getMessage().getContent();
			}
		} catch (Exception e) {
			log.warn("调用chat-gpt接口翻译中文失败: {}", e.getMessage());
		}
		return prompt;
	}
}