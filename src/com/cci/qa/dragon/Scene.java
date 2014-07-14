package com.cci.qa.dragon;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Scene {
    private final String
        name,
        condition,
        criteria;
    private final Map<String, List<File>> devicesPhoto = new HashMap<String, List<File>>();
    
    public Scene(String name, String condition, String criteria) {
        this.name = name;
        this.condition = condition;
        this.criteria = criteria;
    }
    
    public Scene create(String device) {
        if(null == devicesPhoto.get(device)) {
            devicesPhoto.put(device, new ArrayList<File>());
        }
        return this;
    }
    
    public Scene addPhoto(String device, File photo) {
    	devicesPhoto.get(device).add(photo);
        return this;
    }
    
    public String getName() {
        return name;
    }
    
    public String getCondition() {
        return condition;
    }
    
    public String getCriteria() {
        return criteria;
    }

    public Map<String, List<File>> getDevicesPhoto() {
        return devicesPhoto;
    }
}
