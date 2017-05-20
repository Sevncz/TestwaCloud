package com.testwa.distest.server.quartz.job;

public interface TestwaCron {

	void register();

	void reschedule( String cronExpression );
	
	void pause();
	
	void resume();

}