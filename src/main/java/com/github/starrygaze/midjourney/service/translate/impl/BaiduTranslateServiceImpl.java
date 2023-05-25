package com.github.starrygaze.midjourney.service.translate.impl;


import cn.hutool.core.exceptions.ValidateException;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.crypto.digest.MD5;
import com.github.starrygaze.midjourney.ProxyProperties;
import com.github.starrygaze.midjourney.service.translate.TranslateService;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.support.BeanDefinitionValidationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

/**
 * 这个类叫做 BaiduTranslateServiceImpl，它实现了 TranslateService 接口。从名字中我们可以看出，这个类的作用是提供基于百度翻译的服务。
 * 这个类的作用在于提供一个基于百度翻译的翻译服务，可以用于将中文文本翻译成英语文本。其中，调用百度翻译API的过程中，使用了 RestTemplate 这个Spring提供的HTTP客户端工具，使得调用过程更为方便。
 * 同时，这个类对百度翻译API的返回结果做了详细的检查，确保了服务的稳定性和准确性。
 */
@Slf4j
public class BaiduTranslateServiceImpl implements TranslateService {

	/**
	 *
	 */
	private static final String TRANSLATE_API = "https://fanyi-api.baidu.com/api/trans/vip/translate";

	private final String appid;
	private final String appSecret;

	/**
	 * 初始化（构造函数）：在构造函数中，接收一个类型为 ProxyProperties.BaiduTranslateConfig 的参数 translateConfig，并从这个参数中获取了百度翻译所需要的appid和appSecret。如果这两个字段任何一个为空，将会抛出一个 BeanDefinitionValidationException 异常。
	 * @param translateConfig
	 */
	public BaiduTranslateServiceImpl(ProxyProperties.BaiduTranslateConfig translateConfig) {
		this.appid = translateConfig.getAppid();
		this.appSecret = translateConfig.getAppSecret();
		if (!CharSequenceUtil.isAllNotBlank(this.appid, this.appSecret)) {
			throw new BeanDefinitionValidationException("mj-proxy.baidu-translate.appid或mj-proxy.baidu-translate.app-secret未配置");
		}
	}

	/**
	 * translateToEnglish(String prompt)：这个方法用来将输入的文本翻译成英语。方法首先检查输入文本是否包含中文，如果不包含，直接返回原文本，否则会进行翻译。翻译的过程中会生成一个随机的salt，以及一个签名sign。然后构造一个用于调用百度翻译API的URL。
	 * 通过 RestTemplate 调用这个URL，得到百度翻译API的响应结果，并从这个结果中提取出翻译后的文本。如果在调用过程中出现任何异常，都会被捕获并打印错误信息，然后直接返回原文本。
	 * @param prompt
	 * @return
	 */
	@Override
	public String translateToEnglish(String prompt) {
		if (!containsChinese(prompt)) {
			return prompt;
		}
		String salt = RandomUtil.randomNumbers(5);
		String sign = MD5.create().digestHex(this.appid + prompt + salt + this.appSecret);
		String url = TRANSLATE_API + "?from=zh&to=en&appid=" + this.appid + "&salt=" + salt + "&q=" + prompt + "&sign=" + sign;
		try {
			ResponseEntity<String> responseEntity = new RestTemplate().getForEntity(url, String.class);
			if (responseEntity.getStatusCode() != HttpStatus.OK || CharSequenceUtil.isBlank(responseEntity.getBody())) {
				throw new ValidateException(responseEntity.getStatusCodeValue() + " - " + responseEntity.getBody());
			}
			JSONObject result = new JSONObject(responseEntity.getBody());
			if (result.has("error_code")) {
				throw new ValidateException(result.getString("error_code") + " - " + result.getString("error_msg"));
			}
			return result.getJSONArray("trans_result").getJSONObject(0).getString("dst");
		} catch (Exception e) {
			log.warn("调用百度翻译失败: {}", e.getMessage());
		}
		return prompt;
	}

}
