package com.github.starrygaze.midjourney.util;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.text.CharSequenceUtil;
import lombok.experimental.UtilityClass;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@UtilityClass
public class MimeTypeUtils {
	private final Map<String, List<String>> MIME_TYPE_MAP;

	/**
	 * 在此类加载时，会读取资源文件 "mime.types" 的内容，然后将每一行分割成两个部分：前半部分为MIME类型，后半部分为可能的文件后缀，它们被加入到MIME_TYPE_MAP中。
	 * 这样，你就可以使用这个映射来根据MIME类型找到对应的文件后缀。
	 */
	static {
		MIME_TYPE_MAP = new HashMap<>();
		var resource = MimeTypeUtils.class.getResource("/mime.types");
		var lines = FileUtil.readLines(resource, StandardCharsets.UTF_8);
		for (var line : lines) {
			if (CharSequenceUtil.isBlank(line)) {
				continue;
			}
			var arr = line.split(":");
			MIME_TYPE_MAP.put(arr[0], CharSequenceUtil.split(arr[1], ' '));
		}
	}

	/**
	 * 这个方法接收一个MIME类型的字符串作为参数，然后尝试在MIME_TYPE_MAP中找到相应的文件后缀。
	 * 如果找不到，它会尝试找一个MIME类型，其前半部分与给定的MIME类型字符串匹配的键，并返回该键对应的文件后缀。
	 * 如果仍然找不到，就返回null。
	 * @param mimeType
	 * @return
	 */
	public static String guessFileSuffix(String mimeType) {
		if (CharSequenceUtil.isBlank(mimeType)) {
			return null;
		}
		String key = mimeType;
		if (!MIME_TYPE_MAP.containsKey(key)) {
			key = MIME_TYPE_MAP.keySet().stream().filter(k -> CharSequenceUtil.startWithIgnoreCase(mimeType, k))
					.findFirst().orElse(null);
		}
		var suffixList = MIME_TYPE_MAP.get(key);
		if (suffixList == null || suffixList.isEmpty()) {
			return null;
		}
		return suffixList.iterator().next();
	}

}
