package com.github.vitaliiev.t1aspect.service;

import com.github.vitaliiev.t1aspect.model.Invocation;
import com.github.vitaliiev.t1aspect.repository.InvocationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.SchedulingTaskExecutor;
import org.springframework.security.concurrent.DelegatingSecurityContextCallable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.concurrent.Callable;

@Service
@Slf4j
public class InvocationTrackerImpl implements InvocationTracker {
	@Autowired
	private InvocationRepository repository;
	@Autowired
	SchedulingTaskExecutor executor;

	@Override
	public Invocation registerStart(String serviceName, String methodName, String invokedBy) {
		Invocation invocation = new Invocation();
		invocation.setStart(Instant.now());
		invocation.setServiceName(serviceName);
		invocation.setMethodName(methodName);
		invocation.setInvokedBy(invokedBy);
		return invocation;
	}

	@Override
	public Invocation registerSuccess(Invocation invocation) {
		invocation.setFinish(Instant.now());
		return invocation;
	}

	@Override
	public Invocation registerFail(Invocation invocation, Throwable throwable) {
		invocation.setFinish(Instant.now());
		invocation.setExceptionType(throwable.getClass().getName());
		invocation.setExceptionMessage(throwable.getMessage());
		return invocation;
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void submit(Invocation invocation) {
		Callable<Invocation> task = new DelegatingSecurityContextCallable<>(() -> repository.save(invocation));
		executor.submitCompletable(task)
				.whenComplete(this::handleSubmission);
	}

	private void handleSubmission(Invocation invocation, Throwable throwable) {
		if (throwable != null) {
			log.error("Error occurred when submitting invocation tracking info: {}", throwable.getMessage());
		}
	}
}
