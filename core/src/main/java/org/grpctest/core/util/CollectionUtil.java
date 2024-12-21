package org.grpctest.core.util;

import java.util.List;
import java.util.Objects;

public class CollectionUtil {

    public static <T> List<T> trimLastNElements(List<T> list, int n) {
        if (n <= 0) return list;
        if (list.size() <= n) {
            list.clear();
        } else {
            list.subList(list.size() - n, list.size()).clear();
        }
        return list;
    }

    public static <T> List<T> trimAllExceptFirstElement(List<T> list) {
        if (Objects.isNull(list) || list.isEmpty()) return list;
        return trimLastNElements(list, list.size() - 1);
    }
}
