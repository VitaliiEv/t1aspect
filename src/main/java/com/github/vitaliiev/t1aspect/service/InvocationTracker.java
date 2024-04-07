package com.github.vitaliiev.t1aspect.service;

import com.github.vitaliiev.t1aspect.model.Invocation;

public interface InvocationTracker {

	Invocation registerStart(String serviceName, String methodName, String invokedBy);

	Invocation registerSuccess(Invocation invocation);

	Invocation registerFail(Invocation invocation, Throwable throwable);

	void submit(Invocation invocation);
}
