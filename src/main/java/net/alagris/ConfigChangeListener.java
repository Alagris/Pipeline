package net.alagris;

public interface ConfigChangeListener<T> {
    void onChange(T newValue, T oldValue);
}
