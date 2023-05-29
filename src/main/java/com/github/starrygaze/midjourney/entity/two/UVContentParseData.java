package com.github.starrygaze.midjourney.entity.two;

import lombok.Data;

@Data
public class UVContentParseData extends ContentParseData{
	protected String taskId;
	protected String prompt;
	protected String status;
	protected Integer index;
}
