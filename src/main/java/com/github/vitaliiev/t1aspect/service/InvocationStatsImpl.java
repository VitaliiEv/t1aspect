package com.github.vitaliiev.t1aspect.service;

import com.github.vitaliiev.t1aspect.annotation.TrackTime;
import com.github.vitaliiev.t1aspect.model.MethodInvocation;
import com.github.vitaliiev.t1aspect.model.MethodInvocationSum;
import com.github.vitaliiev.t1aspect.repository.InvocationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;

@Service
public class InvocationStatsImpl implements InvocationStats {

	@Autowired
	private InvocationRepository repository;

	private static final int MAX_GLOBAL_DAYS = 7;
	private static final Duration MAX_GLOBAL_DURATION = Duration.ofDays(MAX_GLOBAL_DAYS);
	private static final Comparator<MethodInvocation> METHOD_INVOCATION_COMPARATOR =
			Comparator.comparing(MethodInvocation::getServiceName)
					.thenComparing(MethodInvocation::getMethodName);

	@TrackTime
	@Override
	@Transactional(readOnly = true)
	public List<MethodInvocation> getStats(Instant from, Instant to) {
		checkFromIsNotAfterFinish(from, to);
		Duration duration = Duration.between(from, to);
		if (duration.compareTo(MAX_GLOBAL_DURATION) > 0) {
			String reason = String.format("Maximum period is %d days", MAX_GLOBAL_DAYS);
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, reason);
		}
		List<MethodInvocationSum> stats = repository.getStats(from, to);
		return getMethodInvocations(stats);
	}

	@TrackTime
	@Override
	@Transactional(readOnly = true)
	public List<MethodInvocation> getServiceStats(String service, Instant from, Instant to) {
		checkFromIsNotAfterFinish(from, to);
		List<MethodInvocationSum> stats = repository.getServiceStats(service, from, to);
		return getMethodInvocations(stats);
	}

	@TrackTime
	@Override
	@Transactional(readOnly = true)
	public List<MethodInvocation> getMethodStats(String service, String method, Instant from, Instant to) {
		checkFromIsNotAfterFinish(from, to);
		List<MethodInvocationSum> stats = repository.getMethodStats(service, method, from, to);
		return getMethodInvocations(stats);
	}

	private List<MethodInvocation> getMethodInvocations(List<MethodInvocationSum> stats) {
		return stats.stream()
				.map(this::toMethodInvocation)
				.sorted(METHOD_INVOCATION_COMPARATOR)
				.toList();
	}

	private MethodInvocation toMethodInvocation(MethodInvocationSum methodInvocationSum) {
		Long invocations = methodInvocationSum.getInvocations();
		Long successful = methodInvocationSum.getSuccessful();
		Long failed = methodInvocationSum.getFailed();

		return new MethodInvocation()
				.serviceName(methodInvocationSum.getServiceName())
				.methodName(methodInvocationSum.getMethodName())
				.invocations(invocations)
				.successful(successful)
				.failed(failed)
				.unfinished(methodInvocationSum.getUnfinished())
				.invocationsAvg(calcAvg(methodInvocationSum.getInvocationsSumNano(), invocations))
				.successfulAvg(calcAvg(methodInvocationSum.getSuccessfulSumNano(), successful))
				.failedAvg(calcAvg(methodInvocationSum.getFailedSumNano(), failed));
	}

	private Double calcAvg(Long sum, Long quantity) {
		if (sum == null || sum.equals(0L) || quantity == null || quantity.equals(0L)) {
			return 0d;
		}
		return sum.doubleValue() / quantity.doubleValue();
	}

	private void checkFromIsNotAfterFinish(Instant from, Instant to) {
		if (from != null && to != null && from.isAfter(to)) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Date from must be before date to");
		}
	}

}
