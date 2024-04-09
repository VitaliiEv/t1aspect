package com.github.vitaliiev.t1aspect.service;

import com.github.vitaliiev.t1aspect.annotation.TrackAsyncTime;
import com.github.vitaliiev.t1aspect.annotation.TrackTime;
import org.springframework.stereotype.Component;

@Component
public class InvocationBean {


	@TrackTime
	public void invoke(int millis) {
		sleep(millis);
	}

	@TrackAsyncTime
	public void invokeAsync(int millis) {
		sleep(millis);
	}

	private void sleep(int millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

}
