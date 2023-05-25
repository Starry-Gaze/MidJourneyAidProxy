package com.github.starrygaze.midjourney.util;

import com.github.starrygaze.midjourney.enums.Action;
import lombok.Data;

@Data
public class MessageData {
	private Action action;
	private String prompt;
	private int index;
	private String status;
}
