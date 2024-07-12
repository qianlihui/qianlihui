package com.qlh.base;

import java.util.HashMap;
import java.util.Map;

public interface QlhEnum<T, TCode> {

    Map<Object, Object> code = new HashMap<>();
    Map<Object, String> desc = new HashMap<>();

    default void init(TCode code, String desc) {
        QlhEnum.code.put(this, code);
        QlhEnum.desc.put(this, desc);
    }

    default TCode getCode() {
        return (TCode) code.get(this);
    }

    default String getDesc() {
        return desc.get(this);
    }

    default T getByCode(TCode code) {
        if (code != null) {
            for (QlhEnum enumBase : this.getClass().getEnumConstants()) {
                if (code.toString().equalsIgnoreCase(enumBase.getCode().toString())) {
                    return (T) enumBase;
                }
            }
        }
        return null;
    }

    default T getByDesc(String desc) {
        for (QlhEnum enumBase : this.getClass().getEnumConstants()) {
            if (enumBase.getDesc().equalsIgnoreCase(desc)) {
                return (T) enumBase;
            }
        }
        return null;
    }

    default boolean isEquals(TCode code) {
        return getCode().toString().equalsIgnoreCase(code.toString());
    }

    default boolean isEquals(QlhEnum snailEnum) {
        return snailEnum != null && snailEnum.getClass() == getClass() && isEquals((TCode) snailEnum.getCode());
    }
}
