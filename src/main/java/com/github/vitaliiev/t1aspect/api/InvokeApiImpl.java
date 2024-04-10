package com.github.vitaliiev.t1aspect.api;

import com.github.vitaliiev.t1aspect.service.InvocationBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class InvokeApiImpl implements InvokeApiDelegate {

	@Autowired
	InvocationBean invocationBean;

	@Override
	public ResponseEntity<Void> invoke(Integer millis, Boolean async, Boolean fail) {
		int delay = millis == null ? 1000 : millis;
		if (Boolean.TRUE.equals(async)) {
			invocationBean.invokeAsync(delay, Boolean.TRUE.equals(fail));
		} else {
			invocationBean.invoke(delay, Boolean.TRUE.equals(fail));
		}
		return ResponseEntity.ok(null);
	}
}
