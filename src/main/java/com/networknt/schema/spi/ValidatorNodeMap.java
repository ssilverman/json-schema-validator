package com.networknt.schema.spi;

import java.util.HashMap;
import java.util.concurrent.ConcurrentMap;

public class ValidatorNodeMap extends HashMap<String, ValidatorNodeFactory<? extends ValidatorNode>> {

    public ValidatorNodeMap() {
        super();
    }

    public ValidatorNodeMap(ConcurrentMap<String, ValidatorNodeFactory<? extends ValidatorNode>> map) {
        super(map);
    }
}
