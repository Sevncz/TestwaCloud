package com.testwa.distest.server.quartz.service;

import org.springframework.stereotype.Service;

@Service
public class SimpleService {

	public void test() {
		System.out.println("Simple Job is running!");
	}

}