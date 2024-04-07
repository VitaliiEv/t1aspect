package com.github.vitaliiev.t1aspect.repository;

import com.github.vitaliiev.t1aspect.model.Invocation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvocationRepository extends JpaRepository<Invocation, Long> {
}
