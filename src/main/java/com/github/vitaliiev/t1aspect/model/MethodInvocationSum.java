package com.github.vitaliiev.t1aspect.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class MethodInvocationSum {
	private String serviceName;
	private String methodName;
	private Long invocations;
	private Long successful;
	private Long failed;
	private Long unfinished;
	private Long invocationsSumNano;
	private Long successfulSumNano;
	private Long failedSumNano;
}
