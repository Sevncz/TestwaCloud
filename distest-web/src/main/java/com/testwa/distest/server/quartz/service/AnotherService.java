package com.testwa.distest.server.quartz.service;

import org.springframework.stereotype.Service;

@Service
public class AnotherService {

	public void test(String who) {
		System.out.println("Another Job is running! " + who);
	}

}