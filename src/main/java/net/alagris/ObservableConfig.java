package net.alagris;

import java.util.HashSet;

public class ObservableConfig<T> {
    private T value;
    private HashSet<ConfigChangeListener<T>> listeners = new HashSet<>(0);

    public ObservableConfig() {
    }

    public ObservableConfig(T value) {
        this(value, true);
    }

    public ObservableConfig(T value, boolean triggerEvent) {
        if (triggerEvent) {
            setValue(value);
        } else {
            this.value = value;
        }
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        for (ConfigChangeListener<T> listener : listeners) {
            listener.onChange(value, getValue());
        }
        this.value = value;
    }

    public void addListener(ConfigChangeListener<T> listener) {
        listeners.add(listener);
    }

    public void removeListener(ConfigChangeListener<T> listener) {
        listeners.remove(listener);
    }
    
    @Override
    public String toString() {
        return String.valueOf(value);
    }

}
