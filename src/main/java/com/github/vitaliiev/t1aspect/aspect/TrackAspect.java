package com.github.vitaliiev.t1aspect.aspect;

import com.github.vitaliiev.t1aspect.model.Invocation;
import com.github.vitaliiev.t1aspect.model.TrackException;
import com.github.vitaliiev.t1aspect.service.InvocationTracker;
import com.github.vitaliiev.t1aspect.util.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.SchedulingTaskExecutor;
import org.springframework.security.concurrent.DelegatingSecurityContextCallable;
import org.springframework.stereotype.Component;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;

@Component
@Aspect
@Slf4j
@Order(0)
public class TrackAspect {

	@Autowired
	private InvocationTracker invocationTracker;
	@Autowired
	SchedulingTaskExecutor executor;

	@Pointcut("@annotation(com.github.vitaliiev.t1aspect.annotation.TrackTime)")
	public void trackTime() {
	}

	@Pointcut("@annotation(com.github.vitaliiev.t1aspect.annotation.TrackAsyncTime)")
	public void trackAsyncTime() {
	}

	@Around(value = "trackTime()")
	public Object aroundTrackTime(ProceedingJoinPoint proceedingJoinPoint) {
		Invocation invocation = createInvocation(proceedingJoinPoint);
		return invokeAndTrack(proceedingJoinPoint, invocation);
	}

	@Around(value = "trackAsyncTime()")
	public CompletableFuture<Object> aroundTrackAsyncTime(ProceedingJoinPoint proceedingJoinPoint) {
		Invocation invocation = createInvocation(proceedingJoinPoint);
		Callable<Object> invokeAndTrack = () -> invokeAndTrack(proceedingJoinPoint, invocation);
		Callable<Object> task = new DelegatingSecurityContextCallable<>(invokeAndTrack);
		return executor.submitCompletable(task);
	}

	private Invocation createInvocation(ProceedingJoinPoint proceedingJoinPoint) {
		Signature signature = proceedingJoinPoint.getSignature();
		String serviceName = signature.getDeclaringTypeName();
		String methodName = signature.getName();
		String invokedBy = SecurityUtils.getCurrentUserName();
		return invocationTracker.registerStart(serviceName, methodName, invokedBy);
	}

	private Object invokeAndTrack(ProceedingJoinPoint proceedingJoinPoint, Invocation invocation) {
		try {
			Object result = proceedingJoinPoint.proceed();
			invocation = invocationTracker.registerSuccess(invocation);
			return result;
		} catch (Throwable e) {
			invocation = invocationTracker.registerFail(invocation, e);
			throw new TrackException(e);
		} finally {
			invocationTracker.submit(invocation);
		}
	}

}
