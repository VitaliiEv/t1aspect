package com.github.vitaliiev.t1aspect.api;

import com.github.vitaliiev.t1aspect.model.MethodInvocation;
import com.github.vitaliiev.t1aspect.service.InvocationStats;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.List;

@Service
public class StatsImpl implements StatsApiDelegate {

	@Autowired
	private InvocationStats invocationStats;

	@Override
	public ResponseEntity<List<MethodInvocation>> getMethodStats(String service, String method, LocalDate from,
	                                                             LocalDate to) {
		List<MethodInvocation> invocations = invocationStats.getMethodStats(service, method, getFrom(from), getTo(to));
		return ResponseEntity.ok(invocations);
	}

	@Override
	public ResponseEntity<List<MethodInvocation>> getServiceStats(String service, LocalDate from, LocalDate to) {
		List<MethodInvocation> invocations = invocationStats.getServiceStats(service, getFrom(from), getTo(to));
		return ResponseEntity.ok(invocations);
	}

	@Override
	public ResponseEntity<List<MethodInvocation>> getStats(LocalDate from, LocalDate to) {
		List<MethodInvocation> invocations = invocationStats.getStats(getFrom(from), getTo(to));
		return ResponseEntity.ok(invocations);
	}

	private Instant getFrom(LocalDate from) {
		if (from == null) {
			return null;
		}
		return from.atStartOfDay(ZoneId.systemDefault()).toInstant();
	}

	private Instant getTo(LocalDate to) {
		if (to == null) {
			return null;
		}
		return LocalDateTime.of(to, LocalTime.MAX).atZone(ZoneId.systemDefault()).toInstant();
	}
}
