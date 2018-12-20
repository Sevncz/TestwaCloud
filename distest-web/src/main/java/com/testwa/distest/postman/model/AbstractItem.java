package com.testwa.distest.postman.model;

import com.testwa.distest.postman.PostmanRequestRunner;
import com.testwa.distest.postman.PostmanRunResult;
import lombok.Data;

@Data
abstract class AbstractItem {

//    public abstract String getID();
    public abstract void add(AbstractItem item);
    public abstract void remove(AbstractItem item);
    public abstract AbstractItem getChild(int i);
    public abstract boolean run(boolean haltOnError, PostmanRequestRunner runner, PostmanVariables var, PostmanRunResult runResult);

}
