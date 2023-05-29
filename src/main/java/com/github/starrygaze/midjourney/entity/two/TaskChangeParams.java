package com.github.starrygaze.midjourney.entity.two;

import com.github.starrygaze.midjourney.enums.two.TaskAction;
import lombok.Data;

@Data
public class TaskChangeParams {
	private String id;
	private TaskAction action;
	private Integer index;
}
