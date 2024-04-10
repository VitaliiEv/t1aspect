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
		CompletableFuture<Invocation> invocation = createInvocation(proceedingJoinPoint);
		try {
			Object result = proceedingJoinPoint.proceed();
			invocation.thenCompose(invocationTracker::registerSuccess);
			return result;
		} catch (Throwable e) {
			invocation.thenCompose(i -> invocationTracker.registerFail(i, e));
			throw new TrackException(e);
		}
	}

	@Around(value = "trackAsyncTime()")
	public CompletableFuture<Object> aroundTrackAsyncTime(ProceedingJoinPoint proceedingJoinPoint) {
		CompletableFuture<Invocation> invocation = createInvocation(proceedingJoinPoint);
		try {
			Callable<Object> task = getTask(proceedingJoinPoint);
			return executor.submitCompletable(task)
					.whenComplete((o, t) -> {
						if (t != null) {
							invocation.thenCompose(i -> invocationTracker.registerFail(i, t));
							log.error("Exception occurred while processing async method: {}", t.getMessage());
						} else {
							invocation.thenCompose(invocationTracker::registerSuccess);
						}
					});
		} catch (Throwable e) {
			invocation.thenCompose(i -> invocationTracker.registerFail(i, e));
			throw new TrackException(e);
		}

	}

	private CompletableFuture<Invocation> createInvocation(ProceedingJoinPoint proceedingJoinPoint) {
		Signature signature = proceedingJoinPoint.getSignature();
		String serviceName = signature.getDeclaringTypeName();
		String methodName = signature.getName();
		String invokedBy = SecurityUtils.getCurrentUserName();
		return invocationTracker.registerStart(serviceName, methodName, invokedBy);
	}

	private Callable<Object> getTask(ProceedingJoinPoint proceedingJoinPoint) {
		return new DelegatingSecurityContextCallable<>(() ->{
			try {
				return proceedingJoinPoint.proceed();
			} catch (Throwable e) {
				throw new TrackException(e);
			}
		});
	}

}
