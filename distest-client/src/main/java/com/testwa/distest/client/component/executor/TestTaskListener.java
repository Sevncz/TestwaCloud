package com.testwa.distest.client.component.executor;public interface TestTaskListener {    void onStartup(AbstractTestTask task, boolean success);    void onComplete(AbstractTestTask task);    void onCancel(AbstractTestTask task);}