package com.github.vitaliiev.t1aspect.service;

import com.github.vitaliiev.t1aspect.model.MethodInvocation;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;

import java.time.Instant;
import java.util.List;

@Validated
public interface InvocationStats {

	@Validated
	List<MethodInvocation> getStats(@NotNull Instant from, @NotNull Instant to);

	@Validated
	List<MethodInvocation> getServiceStats(@NotNull String service, Instant from, Instant to);

	@Validated
	List<MethodInvocation> getMethodStats(@NotNull String service, @NotNull String method, Instant from, Instant to);
}
