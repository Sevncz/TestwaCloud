package com.testwa.distest.postman.model;

import com.testwa.core.utils.UUID;
import com.testwa.distest.postman.PostmanRequestRunner;
import com.testwa.distest.postman.PostmanRunResult;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@Data
public class PostmanItem extends AbstractItem{
    private String itemId;
	private String name;
	private List<PostmanEvent> event;
	private PostmanRequest request;
	private List<PostmanResponse> response;
    private List<PostmanItem> item;

    public PostmanItem() {
        this.itemId = UUID.uuid();
    }

    @Override
    public String getID() {
        return this.itemId;
    }

    @Override
    public void add(AbstractItem item) {

    }

    @Override
    public void remove(AbstractItem item) {

    }

    @Override
    public AbstractItem getChild(int i) {
        return null;
    }

    @Override
    public boolean run(boolean haltOnError, PostmanRequestRunner runner, PostmanVariables var, PostmanRunResult runResult) {
        boolean isSuccessful = true;
        runResult.totalRequest++;
        log.info("======> POSTMAN request: " + this.getName());
        try {
            boolean runSuccess = runner.run(this, runResult);
            if (!runSuccess) {
                runResult.failedRequest++;
                runResult.failedRequestName.add(this.getName() + "." + this.getName());
            }
            isSuccessful = runSuccess && isSuccessful;
            if (haltOnError && !isSuccessful) {
                return isSuccessful;
            }
        } catch (Throwable e) {
            e.printStackTrace();
            runResult.failedRequest++;
            runResult.failedRequestName.add(this.getName() + "." + this.getName());
            return false;
        }
        return isSuccessful;
    }
}