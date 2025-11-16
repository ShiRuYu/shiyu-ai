package com.shiyu.ai.common.core.utils;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * 高性能 DynamicQuery
 * - MethodHandle 预编译缓存字段访问
 * - Predicate + Comparator 缓存
 * - 可选过滤 + 排序
 */
public class DynamicQuery {

    // ----------------------- Filter / Sort Rule -----------------------
    public enum Op { EQ, NE, GT, LT, GE, LE, LIKE, IN }

    public record FilterRule(String field, Op op, Object value) {}
    public record SortRule(String field, boolean asc, boolean nullFirst) {
        public SortRule(String field, boolean asc) {
            this(field, asc, false);
        }
    }

    // ----------------------- MethodHandle 缓存 -----------------------
    private static final Map<Class<?>, Map<String, MethodHandle>> HANDLE_CACHE = new ConcurrentHashMap<>();

    private static MethodHandle getGetter(Class<?> clazz, String field) {
        return HANDLE_CACHE
                .computeIfAbsent(clazz, k -> new ConcurrentHashMap<>())
                .computeIfAbsent(field, f -> {
                    try {
                        Field declared = clazz.getDeclaredField(f);
                        declared.setAccessible(true);
                        return MethodHandles.lookup().unreflectGetter(declared);
                    } catch (Exception e) {
                        return null;
                    }
                });
    }

    private static Object getFieldValue(Object obj, String field) {
        try {
            MethodHandle mh = getGetter(obj.getClass(), field);
            if (mh == null) return null;
            return mh.invoke(obj);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    // ----------------------- Predicate 构建 -----------------------
    private static <T> Predicate<T> buildPredicate(List<FilterRule> filters) {
        if (filters == null || filters.isEmpty()) return t -> true;

        return filters.stream()
                .<Predicate<T>>map(rule -> t -> {
                    Object fv = getFieldValue(t, rule.field());
                    Object val = rule.value();

                    return switch (rule.op()) {
                        case EQ -> Objects.equals(fv, val);
                        case NE -> !Objects.equals(fv, val);
                        case GT -> compare(fv, val) > 0;
                        case LT -> compare(fv, val) < 0;
                        case GE -> compare(fv, val) >= 0;
                        case LE -> compare(fv, val) <= 0;
                        case LIKE -> fv != null && fv.toString().toLowerCase().contains(val.toString().toLowerCase());
                        case IN -> fv != null && ((Collection<?>) val).contains(fv);
                    };
                })
                .reduce(x -> true, Predicate::and);
    }

    @SuppressWarnings("unchecked")
    private static int compare(Object a, Object b) {
        if (a == null || b == null) return -1;
        return ((Comparable<Object>) a).compareTo(b);
    }

    // ----------------------- Comparator 构建 -----------------------
    private static <T> Comparator<T> buildComparator(List<SortRule> sorts) {
        if (sorts == null || sorts.isEmpty()) return (o1, o2) -> 0;

        Comparator<T> comparator = (o1, o2) -> 0;

        for (SortRule rule : sorts) {
            Comparator<T> c = Comparator.comparing(
                    o -> (Comparable<Object>) getFieldValue(o, rule.field()),
                    (a, b) -> compareWithNull(a, b, rule.asc(), rule.nullFirst())
            );
            comparator = comparator.thenComparing(c);
        }

        return comparator;
    }

    private static int compareWithNull(Comparable<Object> a, Comparable<Object> b, boolean asc, boolean nullFirst) {
        if (a == null && b == null) return 0;
        if (a == null) return nullFirst ? -1 : 1;
        if (b == null) return nullFirst ? 1 : -1;

        int res = a.compareTo(b);
        return asc ? res : -res;
    }

    // ----------------------- 缓存 Predicate + Comparator -----------------------
    private static final Map<String, Predicate<?>> PREDICATE_CACHE = new ConcurrentHashMap<>();
    private static final Map<String, Comparator<?>> COMPARATOR_CACHE = new ConcurrentHashMap<>();

    @SuppressWarnings("unchecked")
    private static <T> Predicate<T> getCachedPredicate(String key, List<FilterRule> filters) {
        return (Predicate<T>) PREDICATE_CACHE.computeIfAbsent(key, k -> buildPredicate(filters));
    }

    @SuppressWarnings("unchecked")
    private static <T> Comparator<T> getCachedComparator(String key, List<SortRule> sorts) {
        return (Comparator<T>) COMPARATOR_CACHE.computeIfAbsent(key, k -> buildComparator(sorts));
    }

    // ----------------------- 查询主方法 -----------------------
    public static <T> List<T> query(List<T> list, List<FilterRule> filters, List<SortRule> sorts) {
        if (list == null || list.isEmpty()) return List.of();

        String key = buildCacheKey(filters, sorts);

        Predicate<T> predicate = getCachedPredicate(key, filters);
        Comparator<T> comparator = getCachedComparator(key, sorts);

        return list.stream()
                .filter(predicate)
                .sorted(comparator)
                .collect(Collectors.toList());
    }

    private static String buildCacheKey(List<FilterRule> filters, List<SortRule> sorts) {
        String f = filters == null ? "" : filters.toString();
        String s = sorts == null ? "" : sorts.toString();
        return f + "|" + s;
    }

    // ----------------------- 链式 Builder -----------------------
    public static class Builder<T> {
        private final List<T> source;
        private final List<FilterRule> filters = new ArrayList<>();
        private final List<SortRule> sorts = new ArrayList<>();

        public Builder(List<T> source) {
            this.source = source;
        }

        public Builder<T> filter(String field, Op op, Object val) {
            filters.add(new FilterRule(field, op, val));
            return this;
        }

        public Builder<T> sort(String field, boolean asc) {
            sorts.add(new SortRule(field, asc));
            return this;
        }

        public Builder<T> sort(String field, boolean asc, boolean nullFirst) {
            sorts.add(new SortRule(field, asc, nullFirst));
            return this;
        }

        public List<T> build() {
            return DynamicQuery.query(source, filters, sorts);
        }
    }
}

