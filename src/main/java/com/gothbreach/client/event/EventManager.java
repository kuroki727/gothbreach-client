package com.gothbreach.client.event;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.*;

public class EventManager {
    private static final Map<Class<? extends Event>, List<Handler>> handlers = new ConcurrentHashMap<>();

    public static void register(Object listener) {
        for (Method m : listener.getClass().getDeclaredMethods()) {
            if (m.isAnnotationPresent(EventHandler.class)) {
                Class<?>[] params = m.getParameterTypes();
                if (params.length == 1 && Event.class.isAssignableFrom(params[0])) {
                    EventHandler ann = m.getAnnotation(EventHandler.class);
                    handlers.computeIfAbsent((Class<? extends Event>) params[0], k -> new CopyOnWriteArrayList<>())
                        .add(new Handler(listener, m, ann.priority()));
                }
            }
        }
    }

    public static void post(Event event) {
        List<Handler> list = handlers.get(event.getClass());
        if (list != null) {
            list.stream().sorted(Comparator.comparingInt(h -> h.priority)).forEach(h -> h.invoke(event));
        }
    }

    private record Handler(Object owner, Method method, int priority) {
        void invoke(Event e) {
            if (owner instanceof com.gothbreach.client.module.Module mod && !mod.isEnabled()) {
                return;
            }
            try { method.invoke(owner, e); } catch (Exception ex) { ex.printStackTrace(); }
        }
    }
}
