package com.testwa.distest.postman.model;


import com.testwa.core.utils.UUID;
import com.testwa.distest.postman.PostmanRequestRunner;
import com.testwa.distest.postman.PostmanRunResult;

import java.util.ArrayList;
import java.util.List;

public class PostmanFolder extends AbstractItem {
    private List<AbstractItem> items = new ArrayList<>();
    private String folderId;
    private String name;

    public PostmanFolder(String name) {
        this.name = name;
        this.folderId = UUID.uuid();
    }

    @Override
    public String getID() {
        return this.folderId;
    }

    @Override
    public void add(AbstractItem item) {
        items.add(item);
    }

    @Override
    public void remove(AbstractItem item) {
        items.remove(item);

    }

    @Override
    public AbstractItem getChild(int i) {
        return items.get(i);
    }

    @Override
    public boolean run(boolean haltOnError, PostmanRequestRunner runner, PostmanVariables var, PostmanRunResult runResult) {

        boolean isSuccessful = true;
        for(AbstractItem obj : items) {
            isSuccessful = obj.run(haltOnError, runner, var, runResult);
        }

        return isSuccessful;
    }

}
