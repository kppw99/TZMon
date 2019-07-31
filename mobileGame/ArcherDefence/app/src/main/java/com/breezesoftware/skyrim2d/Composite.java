package com.breezesoftware.skyrim2d;

import java.util.ArrayList;
import java.util.List;

/**
 * This file is part of Test Kotlin Application
 * <p>
 * You can do everything with the code and files
 * <p>
 * Created by popof on 31.08.2018.
 */
public class Composite<T> {
    private boolean canDelete = false;

    private List<T> children = new ArrayList<>();

    public void addChild(T child) {
        children.add(child);
    }

    public List<T> getChildren() {
        return children;
    }

    public void postForDelete() {
        this.canDelete = true;
    }

    public boolean isCanDelete() {
        return canDelete;
    }

    public void removeChild(T child) {
        children.remove(child);
    }
}
