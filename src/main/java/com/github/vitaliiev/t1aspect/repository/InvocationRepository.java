package com.github.vitaliiev.t1aspect.repository;

import com.github.vitaliiev.t1aspect.model.Invocation;
import com.github.vitaliiev.t1aspect.model.MethodInvocationSum;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;

public interface InvocationRepository extends JpaRepository<Invocation, Long>{

	@Query("""
	select new com.github.vitaliiev.t1aspect.model.MethodInvocationSum(i.serviceName as serviceName, 
			i.methodName as methodName, 
			sum(case when i.finish is not null then 1 else 0 end) as invocations,
			sum(case when i.finish is not null and i.exceptionType is null then 1 else 0 end) as successful,
			sum(case when i.finish is not null and i.exceptionType is not null then 1 else 0 end) as failed,
			sum(case when i.finish is null then 1 else 0 end) as unfinished,
			
			sum(case when i.finish is not null then i.durationNanos else 0 end) as invocationsSumNano,
			sum(case when i.finish is not null 
				and i.exceptionType is null then i.durationNanos else 0 end) as successfulSumNano,
			sum(case when i.finish is not null 
				and i.exceptionType is not null then i.durationNanos else 0 end) as failedSumNano)
		from Invocation i
		where i.start >= :from and i.start <= :to
		group by i.serviceName, i.methodName
	""")
	List<MethodInvocationSum> getStats(@NotNull Instant from, @NotNull Instant to);

	@Query("""
	select new com.github.vitaliiev.t1aspect.model.MethodInvocationSum(i.serviceName as serviceName,
			i.methodName as methodName, 
			sum(case when i.finish is not null then 1 else 0 end) as invocations,
			sum(case when i.finish is not null and i.exceptionType is null then 1 else 0 end) as successful,
			sum(case when i.finish is not null and i.exceptionType is not null then 1 else 0 end) as failed,
			sum(case when i.finish is null then 1 else 0 end) as unfinished,
			
			sum(case when i.finish is not null then i.durationNanos else 0 end) as invocationsSumNano,
			sum(case when i.finish is not null 
				and i.exceptionType is null then i.durationNanos else 0 end) as successfulSumNano,
			sum(case when i.finish is not null 
				and i.exceptionType is not null then i.durationNanos else 0 end) as failedSumNano)
		from Invocation i
		where ((cast (:from as timestamp) is null) or i.start >= :from)
			and ((cast (:to as timestamp) is null) or i.start <= :to)
			and i.serviceName = :service
		group by i.serviceName, i.methodName
	""")
	List<MethodInvocationSum> getServiceStats(@NotNull String service, Instant from, Instant to);


	@Query("""
	select new com.github.vitaliiev.t1aspect.model.MethodInvocationSum(i.serviceName as serviceName,
			i.methodName as methodName, 
			sum(case when i.finish is not null then 1 else 0 end) as invocations,
			sum(case when i.finish is not null and i.exceptionType is null then 1 else 0 end) as successful,
			sum(case when i.finish is not null and i.exceptionType is not null then 1 else 0 end) as failed,
			sum(case when i.finish is null then 1 else 0 end) as unfinished,
			
			sum(case when i.finish is not null then i.durationNanos else 0 end) as invocationsSumNano,
			sum(case when i.finish is not null 
				and i.exceptionType is null then i.durationNanos else 0 end) as successfulSumNano,
			sum(case when i.finish is not null 
				and i.exceptionType is not null then i.durationNanos else 0 end) as failedSumNano)
		from Invocation i
		where ((cast (:from as timestamp) is null) or i.start >= :from)
			and ((cast (:to as timestamp) is null) or i.start <= :to)
			and i.serviceName = :service
			and i.methodName = :method
	""")
	List<MethodInvocationSum> getMethodStats(@NotNull String service, @NotNull String method, Instant from, Instant to);
}
