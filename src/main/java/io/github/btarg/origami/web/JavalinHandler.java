package io.github.btarg.origami.web;

import io.javalin.http.Handler;
import io.javalin.http.HandlerType;

public class JavalinHandler {
    private final String path;
    private final HandlerType type;
    private final Handler handler;

    public JavalinHandler(String path, HandlerType type, Handler handler) {
        this.path = path;
        this.type = type;
        this.handler = handler;
    }

    public String getPath() {
        return path;
    }

    public HandlerType getType() {
        return type;
    }

    public Handler getHandler() {
        return handler;
    }
}