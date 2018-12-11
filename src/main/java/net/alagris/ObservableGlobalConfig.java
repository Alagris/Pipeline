package net.alagris;

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.Map.Entry;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;

public class ObservableGlobalConfig implements GlobalConfig {

    @Override
    public void onLoad() {

    }

    @Override
    public void applyCover(GlobalConfig other) {
        for (Entry<String, ObservableConfig<Object>> entry : opts.entrySet()) {
            try {
                entry.getValue().setValue(other.get(entry.getKey(), Object.class));
            } catch (NoSuchElementException e) {
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T get(String name, Class<T> type) throws NoSuchElementException {
        if (opts.containsKey(name)) {
            Object value = Classes.parseObject(type, opts.get(name));
//            if(type.isAssignableFrom(Obser))
            return (T) value;
        } else {
            throw new NoSuchElementException("No config named: " + name);
        }
    }

    @Override
    public void put(String variable, Object value) {
        setOpts(variable, value);
    }

    @JsonAnyGetter
    public Map<String, Object> getOpts() {
        class MapView extends AbstractMap<String, Object>{
            
            @Override
            public Set<Object> entrySet() {
                class SetView extends AbstractSet<Object>{
                    private final Set<Entry<String, ObservableConfig<Object>>> set = opts.entrySet();
                    
                    @Override
                    public Iterator<Object> iterator() {
                        class IteratorView{
                            
                        };
                        return set.iterator();
                    }

                    @Override
                    public int size() {
                        return set.size();
                    }
                    
                }
                return new SetView();
            }
            
        }
        return new MapView();
    }

    @JsonAnySetter
    public void setOpts(String k, Object v) {
        this.opts.put(k, new ObservableConfig<Object>(v));
    }

    private HashMap<String, ObservableConfig<Object>> opts = new HashMap<>();

    @Override
    public void onMake() {

    }

}
