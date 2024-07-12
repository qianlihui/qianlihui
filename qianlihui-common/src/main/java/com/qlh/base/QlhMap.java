package com.qlh.base;

import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

public class QlhMap implements Map {

    private Map internal = new HashMap();

    public QlhMap() {

    }

    public QlhMap(Map map) {
        this.internal = map;
    }

    public void putPath(String key, Object value) {
        if (StringUtils.isNoneBlank(key) && key.contains(".")) {
            String[] props = key.split("\\.");
            if (props.length > 1) {
                Map vMap = this;
                for (int i = 0; i < props.length - 1; i++) {
                    Map tMap = (Map) vMap.get(props[i]);
                    if (tMap == null) {
                        tMap = new HashMap();
                        vMap.put(props[i], tMap);
                    }
                    vMap = tMap;
                }
                vMap.put(props[props.length - 1], value);
                return;
            }
        }
        put(key, value);
    }

    public <T> List<T> getList(String key, Class<T>... clazz) {
        Object o = get(key);
        if (o != null && o instanceof List) {

            if (clazz.length > 0) {
                return (List<T>) ((List) o).stream()
                        .map(e -> QlhJsonUtils.toObject(QlhJsonUtils.toJson(e), clazz[0]))
                        .collect(Collectors.toList());
            }
            return (List) o;
        }
        return null;
    }

    public QlhMap getMap(String key) {
        Object o = get(key);
        if (o != null && o instanceof Map) {
            QlhMap map = new QlhMap();
            map.putAll((Map) o);
            return map;
        }
        return null;
    }

    public String getString(String key, String... defaultValue) {
        Object o = get(key);
        if (o == null) {
            if (defaultValue.length > 0 && defaultValue[0] != null)
                return defaultValue[0];
        }
        return o == null ? null : o.toString();
    }

    public Long getLong(String key, Long... defaultValue) {
        try {
            return Long.parseLong(getString(key));
        } catch (Exception e) {
            if (defaultValue.length > 0 && defaultValue[0] != null)
                return defaultValue[0];
            throw new RuntimeException(e);
        }
    }

    public Integer getInteger(String key, Integer... defaultValue) {
        try {
            return Integer.parseInt(getString(key));
        } catch (Exception e) {
            if (defaultValue.length > 0 && defaultValue[0] != null)
                return defaultValue[0];
            throw new RuntimeException(e);
        }
    }

    public Double getDouble(String key, Double... defaultValue) {
        try {
            return Double.parseDouble(getString(key));
        } catch (Exception e) {
            if (defaultValue.length > 0 && defaultValue[0] != null)
                return defaultValue[0];
            throw new RuntimeException(e);
        }
    }

    public BigDecimal getBigDecimal(String key, BigDecimal... defaultValue) {
        try {
            return new BigDecimal(getString(key));
        } catch (Exception e) {
            if (defaultValue.length > 0 && defaultValue[0] != null)
                return defaultValue[0];
            throw new RuntimeException(e);
        }
    }

    public Boolean getBoolean(String key, Boolean... defaultValue) {
        try {
            if (QlhStringUtils.isBlank(getString(key)) && defaultValue.length > 0) {
                return defaultValue[0];
            }
            return new Boolean(getString(key));
        } catch (Exception e) {
            if (defaultValue.length > 0 && defaultValue[0] != null)
                return defaultValue[0];
            throw new RuntimeException(e);
        }
    }

    /**
     * 清除给给定类型的对象
     */
    public QlhMap removeType(Class... classes) {
        Iterator<Entry> iterator = entrySet().iterator();
        while (iterator.hasNext()) {
            Entry next = iterator.next();
            if (next.getValue() != null) {
                for (Class clazz : classes) {
                    if (clazz.isInstance(next.getValue())) {
                        iterator.remove();
                        break;
                    }
                }
            }
        }
        return this;
    }

    public QlhMap removeEmpty() {
        Iterator<Entry> iterator = entrySet().iterator();
        while (iterator.hasNext()) {
            Entry next = iterator.next();
            if (next.getValue() == null
                    || (next.getValue() instanceof String && StringUtils.isBlank((String) next.getValue()))) {
                iterator.remove();
            }
        }
        return this;
    }

    public Object getOneContains(String key) {
        return getOneContains(key, null);
    }

    public Object getOneContains(String key, Object dv) {
        Object mapKey = keySet().stream().filter(e -> e instanceof String && e.toString().contains(key)).findAny().orElse(null);
        Object o = get(mapKey);
        return o == null ? dv : o;
    }

    public QlhMap append(Object key, Object value) {
        put(key, value);
        return this;
    }

    /*** 覆盖方法 ***/
    @Override
    public int size() {
        return internal.size();
    }

    @Override
    public boolean isEmpty() {
        return internal.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return internal.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return internal.containsValue(value);
    }

    @Override
    public Object get(Object key) {
        return internal.get(key);
    }

    @Override
    public Object put(Object key, Object value) {
        return internal.put(key, value);
    }

    @Override
    public Object remove(Object key) {
        return internal.remove(key);
    }

    @Override
    public void putAll(Map m) {
        internal.putAll(m);
    }

    @Override
    public void clear() {
        internal.clear();
    }

    @Override
    public Set keySet() {
        return internal.keySet();
    }

    @Override
    public Collection values() {
        return internal.values();
    }

    @Override
    public Set<Entry> entrySet() {
        return internal.entrySet();
    }
}
