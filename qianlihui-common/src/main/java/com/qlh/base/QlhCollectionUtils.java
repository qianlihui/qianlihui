package com.qlh.base;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Slf4j
public class QlhCollectionUtils {

    public static <T> List<List<T>> combine(List<T> inputs, int len) {
        inputs = new ArrayList<>(inputs);
        List<List<T>> outputs = new ArrayList<>();
        if (len == 1) {
            for (T o : inputs) {
                List<T> tList = new ArrayList<>();
                tList.add(o);
                outputs.add(tList);
            }
        } else {
            while (inputs.size() >= len) {
                Object o = inputs.get(0);
                inputs.remove(0);
                for (List tList : combine(inputs, len - 1)) {
                    tList.add(0, o);
                    outputs.add(tList);
                }
            }
        }
        return outputs;
    }

    public static <T> List<T> copy(List<T> src, int from, int len) {
        List<T> list = new ArrayList<>(len);
        for (int i = from; i < from + len; i++) {
            list.add(src.get(i));
        }
        return list;
    }

    public static boolean isEmpty(Collection collection) {
        return collection == null || collection.size() == 0;
    }

    public static boolean isNotEmpty(Collection collection) {
        return !isEmpty(collection);
    }

}
