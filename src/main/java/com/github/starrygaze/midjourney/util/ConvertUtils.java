package com.github.starrygaze.midjourney.util;

import cn.hutool.core.text.CharSequenceUtil;
import com.github.starrygaze.midjourney.entity.MessageData;
import com.github.starrygaze.midjourney.entity.UVData;
import com.github.starrygaze.midjourney.enums.Action;
import lombok.experimental.UtilityClass;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@UtilityClass
public class ConvertUtils {
	private static final String MJ_I_CONTENT_REGEX = "\\*\\*(.*?)\\*\\* - <@(\\d+)> \\((.*?)\\)";
	private static final String MJ_UV_CONTENT_REGEX = "\\*\\*(.*?)\\*\\* - (.*?) by <@(\\d+)> \\((.*?)\\)";
	private static final String MJ_U_CONTENT_REGEX = "\\*\\*(.*?)\\*\\* - Image #(\\d) <@(\\d+)>";

	/**
	 * 这个方法通过接收一个字符串finalPrompt，然后在其中找出两个"["和"]"之间的字符串，作为任务ID返回。
	 * @param finalPrompt
	 * @return
	 */
	public static String findTaskIdByFinalPrompt(String finalPrompt) {
		return CharSequenceUtil.subBetween(finalPrompt, "[", "]");
	}

	/**
	 *  这个方法用于匹配和解析某种格式的字符串，具体来说是看这个字符串是否符合MJ_I_CONTENT_REGEX这个正则表达式。
	 *  如果匹配成功，它会创建一个MessageData对象，并从字符串中提取出一些信息（如提示，状态）并设置到这个对象中，然后返回这个对象。
	 *  如果不匹配，就返回null。
	 * @param content
	 * @return
	 */
	public static MessageData matchImagineContent(String content) {
		Pattern pattern = Pattern.compile(MJ_I_CONTENT_REGEX);
		Matcher matcher = pattern.matcher(content);
		if (!matcher.find()) {
			return null;
		}
		MessageData data = new MessageData();
		data.setAction(Action.IMAGINE);
		data.setPrompt(matcher.group(1));
		data.setStatus(matcher.group(3));
		return data;
	}

	/**
	 * 这个方法用于匹配和解析另一种格式的字符串，具体来说是看这个字符串是否符合MJ_UV_CONTENT_REGEX这个正则表达式。
	 * 如果匹配成功，它会创建一个MessageData对象，并从字符串中提取出一些信息（如提示，状态）并设置到这个对象中，然后返回这个对象。
	 * 如果不匹配，就会尝试使用matchUContent(content)方法来处理这个字符串。
	 * @param content
	 * @return
	 */
	public static MessageData matchUVContent(String content) {
		Pattern pattern = Pattern.compile(MJ_UV_CONTENT_REGEX);
		Matcher matcher = pattern.matcher(content);
		if (!matcher.find()) {
			return matchUContent(content);
		}
		MessageData data = new MessageData();
		data.setPrompt(matcher.group(1));
		String matchAction = matcher.group(2);
		data.setAction(matchAction.startsWith("Variation") ? Action.VARIATION : Action.UPSCALE);
		data.setStatus(matcher.group(4));
		return data;
	}

	/**
	 * 这是一个私有方法，它用于匹配和解析另一种格式的字符串，具体来说是看这个字符串是否符合MJ_U_CONTENT_REGEX这个正则表达式。
	 * 如果匹配成功，它会创建一个MessageData对象，并从字符串中提取出一些信息（如提示，索引）并设置到这个对象中，然后返回这个对象。
	 * 如果不匹配，就返回null。
	 * @param content
	 * @return
	 */
	private static MessageData matchUContent(String content) {
		Pattern pattern = Pattern.compile(MJ_U_CONTENT_REGEX);
		Matcher matcher = pattern.matcher(content);
		if (!matcher.find()) {
			return null;
		}
		MessageData data = new MessageData();
		data.setAction(Action.UPSCALE);
		data.setPrompt(matcher.group(1));
		data.setStatus("complete");
		data.setIndex(Integer.parseInt(matcher.group(2)));
		return data;
	}

	/**
	 * 这个方法用于将一个字符串转换为一个UVData对象。这个方法会先将字符串按空格分割，然后根据分割后的字符串的具体内容来创建和设置UVData对象的属性。
	 * 如果字符串的格式不符合预期（如长度不对，字符不对等），这个方法会返回null。
	 * @param content
	 * @return
	 */
	public static UVData convertUVData(String content) {
		List<String> split = CharSequenceUtil.split(content, " ");
		if (split.size() != 2) {
			return null;
		}
		String action = split.get(1).toLowerCase();
		if (action.length() != 2) {
			return null;
		}
		UVData upData = new UVData();
		if (action.charAt(0) == 'u') {
			upData.setAction(Action.UPSCALE);
		} else if (action.charAt(0) == 'v') {
			upData.setAction(Action.VARIATION);
		} else {
			return null;
		}
		try {
			int index = Integer.parseInt(action.substring(1, 2));
			if (index < 1 || index > 4) {
				return null;
			}
			upData.setIndex(index);
		} catch (NumberFormatException e) {
			return null;
		}
		upData.setId(split.get(0));
		return upData;
	}

}
