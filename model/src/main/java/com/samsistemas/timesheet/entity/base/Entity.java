package com.samsistemas.timesheet.entity.base;

/**
 * @author jonatan.salas
 */
public class Entity {
    protected long id;

    public Entity setId(long id) {
        this.id = id;
        return this;
    }

    public long getId() {
        return id;
    }
}