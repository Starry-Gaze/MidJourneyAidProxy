package com.github.starrygaze.midjourney.util;

import com.github.starrygaze.midjourney.enums.Action;
import lombok.Data;

@Data
public class UVData {
	private String id;
	private Action action;
	private int index;
}
