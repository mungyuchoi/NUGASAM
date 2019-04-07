package com.moon.nugasam;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.Map;

@IgnoreExtraProperties
public class FirebasePost {
    public String id;
    public String name;
    public Integer nuga;
    public String imageUrl;

    public FirebasePost(){
        // Default constructor required for calls to DataSnapshot.getValue(FirebasePost.class)
    }

    public FirebasePost(String id, String name, Integer nuga) {
        this.id = id;
        this.name = name;
        this.nuga = nuga;
    }

    public FirebasePost(String id, String name, Integer nuga, String imageUrl) {
        this.id = id;
        this.name = name;
        this.nuga = nuga;
        this.imageUrl = imageUrl;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("id", id);
        result.put("name", name);
        result.put("nuga", nuga);
        result.put("imageUrl", imageUrl);
        return result;
    }
}