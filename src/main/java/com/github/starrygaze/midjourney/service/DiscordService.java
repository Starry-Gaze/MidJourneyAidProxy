package com.github.starrygaze.midjourney.service;

import com.github.starrygaze.midjourney.result.Message;
import eu.maxschuster.dataurl.DataUrl;

public interface DiscordService {

	Message<Void> imagine(String prompt);

	Message<Void> upscale(String messageId, int index, String messageHash);

	Message<Void> variation(String messageId, int index, String messageHash);

	Message<Void> reset(String messageId, String messageHash);

	Message<String> upload(String fileName, DataUrl dataUrl);

	Message<Void> describe(String finalFileName);

}
