package com.testwa.distest.client.component.executor.task;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

public class TaskDispatcher {

    private final AtomicReference<ExecutorService> es;
    private static volatile TaskDispatcher instance = null;

    private TaskDispatcher(){
        ExecutorService taskWorker = Executors.newFixedThreadPool(4);
        es = new AtomicReference<>();
        es.set(taskWorker);
    }

    public static TaskDispatcher getInstance(){
        if(instance == null){
            synchronized (TaskDispatcher.class){
                if(instance == null){
                    instance = new TaskDispatcher();
                }
            }
        }
        return instance;
    }

    public void submit(AbstractTestTask task) {
        es.get().submit(task::start);
    }

}
