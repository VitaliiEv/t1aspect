package com.github.vitaliiev.t1aspect.service;

import com.github.vitaliiev.t1aspect.model.Invocation;

import java.util.concurrent.CompletableFuture;

public interface InvocationTracker {

	CompletableFuture<Invocation> registerStart(String serviceName, String methodName, String invokedBy);

	CompletableFuture<Invocation> registerSuccess(Invocation invocation);

	CompletableFuture<Invocation> registerFail(Invocation invocation, Throwable throwable);

}
