package com.wismna.geoffroy.donext;

/**
 * Created by geoffroy on 15-11-25.
 */
public class TaskList {
    private long id;
    private String name;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String comment) {
        this.name = comment;
    }

    // Will be used by the ArrayAdapter in the ListView
    @Override
    public String toString() {
        return name;
    }
}
