package com.github.vitaliiev.t1aspect.service;

import com.github.vitaliiev.t1aspect.annotation.TrackAsyncTime;
import com.github.vitaliiev.t1aspect.annotation.TrackTime;
import org.springframework.stereotype.Component;

import java.util.concurrent.ThreadLocalRandom;

@Component
public class InvocationBean {


	@TrackTime
	public void invoke(int millis, boolean fail) {
		if (fail) {
			fail(millis);
		} else {
			sleep(millis);
		}
	}

	@TrackAsyncTime
	public void invokeAsync(int millis, boolean fail) {
		if (fail) {
			fail(millis);
		} else {
			sleep(millis);
		}
	}

	private void fail(int millis) {
		int failAfter = ThreadLocalRandom.current().nextInt(millis);
		sleep(failAfter);
		throw new RuntimeException(String.format("Method failed intentionally after delay %d", failAfter));
	}

	private void sleep(int millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

}
