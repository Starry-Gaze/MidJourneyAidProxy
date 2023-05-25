package com.github.starrygaze.midjourney.support.handle;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.text.CharSequenceUtil;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * 这个类 BannedPromptHelper 主要的作用是提供检测某个输入是否包含被禁止的词汇的功能。
 * 这个类首先在其构造函数中加载被禁止的词汇列表。它首先检查是否存在特定路径（BANNED_WORDS_FILE_PATH）下的文件，如果存在则从该文件中读取被禁止的词汇。
 * 如果不存在，那么从类路径下的 /banned-words.txt 文件中读取。读取到的被禁止的词汇会被存储到 bannedWords 列表中。
 *
 */
@Component
public class BannedPromptHelper {
	private static final String BANNED_WORDS_FILE_PATH = "/home/spring/config/banned-words.txt";
	private final List<String> bannedWords;

	/**
	 * BannedPromptHelper 类的主要作用是提供一个方法用于检测输入是否包含被禁止的词汇。
	 */
	public BannedPromptHelper() {
		List<String> lines;
		File file = new File(BANNED_WORDS_FILE_PATH);
		if (file.exists()) {
			lines = FileUtil.readLines(file, StandardCharsets.UTF_8);
		} else {
			var resource = BannedPromptHelper.class.getResource("/banned-words.txt");
			lines = FileUtil.readLines(resource, StandardCharsets.UTF_8);
		}
		this.bannedWords = lines.stream().filter(CharSequenceUtil::isNotBlank).toList();
	}

	/**
	 * 这个类有一个方法 isBanned，它接收一个字符串 promptEn 作为输入，将这个字符串转换为小写并检查它是否包含 bannedWords 列表中的任何一个词汇。
	 * 这个检查是通过创建正则表达式并匹配输入字符串完成的。如果输入字符串包含被禁止的词汇，那么这个方法将返回 true，否则返回 false。
	 * @param promptEn
	 * @return
	 */
	public boolean isBanned(String promptEn) {
		String finalPromptEn = promptEn.toLowerCase(Locale.ENGLISH);
		return this.bannedWords.stream().anyMatch(bannedWord -> Pattern.compile("\\b" + bannedWord + "\\b").matcher(finalPromptEn).find());
	}

}
