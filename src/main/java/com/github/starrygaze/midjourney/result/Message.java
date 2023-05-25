package com.github.starrygaze.midjourney.result;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;

/**
 * 这个类名为Message，是一个通用的消息响应类，通常用于封装API接口的返回数据。它包含了状态码(code)，描述信息(description)，以及泛型对象(result)用于存储返回的数据。
 * 这个设计允许API接口返回统一的格式，方便前端处理和显示。这个类还定义了几个静态的消息代码，如SUCCESS_CODE，WAITING_CODE，NOT_FOUND_CODE，VALIDATION_ERROR_CODE
 * 和FAILURE_CODE。这些代码可以标识返回消息的类型，例如成功、等待、未找到、验证错误和失败。除了基本构造方法，这个类还提供了一些静态方法来快速创建Message对象。
 * 这些方法大多以返回消息类型的名称命名，如success，notFound，validationError和failure。这样可以使代码更简洁，更易于理解。例如，如果我们要创建一个表示成功的消息，
 * 只需调用Message.success()即可，无需手动创建Message对象并设置各个字段。如果我们要创建一个表示失败的消息，并附加错误描述，可以调用Message.failure(description)。
 * 总的来说，这个Message类是一个非常通用的响应消息类，可以用于API接口的返回数据，使得返回数据格式统一，便于处理和显示。
 * @param <T>
 */
@Getter
@ApiModel("返回结果")
public class Message<T> {

	@ApiModelProperty("状态码: 1成功, 2提示, 其他错误")
	private final int code;

	@ApiModelProperty("描述")
	private final String description;

	private final T result;

	public static final int SUCCESS_CODE = 1;

	public static final int WAITING_CODE = 2;

	public static final int NOT_FOUND_CODE = 3;

	public static final int VALIDATION_ERROR_CODE = 4;

	public static final int FAILURE_CODE = 9;

	public static <Y> Message<Y> success() {
		return new Message<>(SUCCESS_CODE, "成功");
	}

	public static <T> Message<T> success(T result) {
		return new Message<>(SUCCESS_CODE, "成功", result);
	}

	public static <T> Message<T> success(int code, String description, T result) {
		return new Message<>(code, description, result);
	}

	public static <Y> Message<Y> notFound() {
		return new Message<>(NOT_FOUND_CODE, "数据未找到");
	}

	public static <Y> Message<Y> validationError() {
		return new Message<>(VALIDATION_ERROR_CODE, "校验错误");
	}

	public static <Y> Message<Y> failure() {
		return new Message<>(FAILURE_CODE, "系统异常");
	}

	public static <Y> Message<Y> failure(String description) {
		return new Message<>(FAILURE_CODE, description);
	}

	public static <Y> Message<Y> of(int code, String description) {
		return new Message<>(code, description);
	}

	public static <T> Message<T> of(int code, String description, T result) {
		return new Message<>(code, description, result);
	}

	private Message(int code, String description) {
		this(code, description, null);
	}

	private Message(int code, String description, T result) {
		this.code = code;
		this.description = description;
		this.result = result;
	}
}
