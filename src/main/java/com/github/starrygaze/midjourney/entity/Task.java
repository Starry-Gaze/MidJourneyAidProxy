package com.github.starrygaze.midjourney.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.starrygaze.midjourney.enums.Action;
import com.github.starrygaze.midjourney.enums.TaskStatus;
import com.github.starrygaze.midjourney.enums.two.TaskAction;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 总的来说，这个Task类用于表示一个任务的各种信息，并提供了线程同步控制的方法
 */
@Data
@ApiModel("任务")
public class Task implements Serializable {
	@Serial
	private static final long serialVersionUID = -674915748204390789L;

	/**
	 * 任务的动作类型
	 */
	private Action action;

	/**
	 * 任务的动作类型 v2
	 */
	private TaskAction action2;

	/**
	 * 任务的ID
	 */
	@ApiModelProperty("任务ID")
	private String id;

	/**
	 * 提示词
	 */
	@ApiModelProperty("提示词")
	private String prompt;

	/**
	 * 提示词
	 */
	@ApiModelProperty("提示词-英文")
	private String promptEn;

	/**
	 * 任务的描述
	 */
	@ApiModelProperty("任务描述")
	private String description;

	/**
	 * 自定义参数
	 */
	@ApiModelProperty("自定义参数")
	private String state;

	/**
	 * 提交时间
	 */
	@ApiModelProperty("提交时间")
	private Long submitTime;

	/**
	 * 执行时间
	 */
	@ApiModelProperty("开始执行时间")
	private Long startTime;

	/**
	 * 执行时间
	 */
	@ApiModelProperty("结束时间")
	private Long finishTime;

	/**
	 * 图片URL
	 */
	@ApiModelProperty("图片url")
	private String imageUrl;

	/**
	 * 任务的状态，默认为未开始
	 */
	private TaskStatus status = TaskStatus.NOT_START;

	/**
	 * 任务进度
	 */
	@ApiModelProperty("任务进度")
	private String progress;

    /**
     * 任务失败的原因
     */
	@ApiModelProperty("失败原因")
	private String failReason;


	//这个类还包含了一些隐藏的字段，如：key、finalPrompt、notifyHook、relatedTaskId、messageId、messageHash。这些字段在进行JSON序列化时会被忽略，不会被包含在生成的JSON中。
	// Hidden -- start
	@JsonIgnore
	private String key;
	@JsonIgnore
	private String finalPrompt;
	@JsonIgnore
	private String notifyHook;
	@JsonIgnore
	private String relatedTaskId;
	@JsonIgnore
	private String messageId;
	@JsonIgnore
	private String messageHash;
	// Hidden -- end

	//此外，这个类还包含了一个lock对象，用于线程的同步控制。类中定义了两个方法sleep和awake，分别用于使线程等待和唤醒等待的线程。
	@JsonIgnore
	private final transient Object lock = new Object();

	/**
	 * 使线程等待
	 * @throws InterruptedException
	 */
	public void sleep() throws InterruptedException {
		synchronized (this.lock) {
			this.lock.wait();
		}
	}

	/**
	 * 唤醒等待的线程
	 */
	public void awake() {
		synchronized (this.lock) {
			this.lock.notifyAll();
		}
	}
	@JsonIgnore
	public void start() {
		this.startTime = System.currentTimeMillis();
		this.status = TaskStatus.SUBMITTED;
		this.progress = "0%";
	}

	@JsonIgnore
	public void success() {
		this.finishTime = System.currentTimeMillis();
		this.status = TaskStatus.SUCCESS;
		this.progress = "100%";
	}

	public void fail(String reason) {
		this.finishTime = System.currentTimeMillis();
		this.status = TaskStatus.FAILURE;
		this.failReason = reason;
		this.progress = "";
	}
}
