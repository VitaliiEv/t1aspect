package com.github.vitaliiev.t1aspect.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
public class Invocation {
	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;
	@NotNull
	@Column(nullable = false, length = 254)
	private String serviceName;
	@NotNull
	@Column(nullable = false, length = 254)
	private String methodName;
	private String invokedBy;
	@NotNull
	private Instant start;
	private Instant finish;
	private Long durationNanos;

	private String exceptionType;
	private String exceptionMessage;
}
