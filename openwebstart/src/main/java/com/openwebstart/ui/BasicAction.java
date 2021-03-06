package com.openwebstart.ui;

public abstract class BasicAction<T> implements Action<T> {

    private final boolean active;

    private final String name;

    private final String description;

    protected BasicAction(final String name, final String description) {
        this(true, name, description);
    }

    protected BasicAction(final boolean active, final String name, final String description) {
        this.active = active;
        this.name = name;
        this.description = description;
    }

    @Override
    public boolean isActive() {
        return active;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }
}
