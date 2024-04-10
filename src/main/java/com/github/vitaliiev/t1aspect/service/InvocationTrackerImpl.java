package com.github.vitaliiev.t1aspect.service;

import com.github.vitaliiev.t1aspect.model.Invocation;
import com.github.vitaliiev.t1aspect.model.TrackException;
import com.github.vitaliiev.t1aspect.repository.InvocationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

@Service
@Slf4j
public class InvocationTrackerImpl implements InvocationTracker {
	@Autowired
	private InvocationRepository repository;

	@Async
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public CompletableFuture<Invocation> registerStart(String serviceName, String methodName, String invokedBy) {
		Invocation invocation = new Invocation();
		invocation.setStart(Instant.now());
		invocation.setServiceName(serviceName);
		invocation.setMethodName(methodName);
		invocation.setInvokedBy(invokedBy);
		return CompletableFuture.completedFuture(repository.save(invocation));
	}

	@Async
	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public CompletableFuture<Invocation> registerSuccess(Invocation invocation) {
		setFinish(invocation);
		return CompletableFuture.completedFuture(repository.save(invocation));
	}

	@Async
	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public CompletableFuture<Invocation> registerFail(Invocation invocation, Throwable throwable) {
		setFinish(invocation);
		Throwable unwrapped = unwrap(throwable);
		invocation.setExceptionType(unwrapped.getClass().getName());
		invocation.setExceptionMessage(unwrapped.getMessage());
		return CompletableFuture.completedFuture(repository.save(invocation));
	}

	private void setFinish(Invocation invocation) {
		Instant finish = Instant.now();
		long nanos = Duration.between(invocation.getStart(), finish).toNanos();
		invocation.setFinish(finish);
		invocation.setDurationNanos(nanos);
	}

	private Throwable unwrap(Throwable throwable) {
		if (throwable instanceof CompletionException completionException) {
			return unwrap(completionException.getCause());
		} else if (throwable instanceof TrackException trackException) {
			return unwrap(trackException.getCause());
		} else {
			return throwable;
		}
	}
}
