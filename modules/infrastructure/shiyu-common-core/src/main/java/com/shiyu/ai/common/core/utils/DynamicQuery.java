package com.shiyu.ai.common.core.utils;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/** 高性能 DynamicQuery - MethodHandle 预编译缓存字段访问 - Predicate + Comparator 缓存 - 可选过滤 + 排序 */
public class DynamicQuery {

    /**
     * {@code Op} 表示平台基础设施模块中的一组受控业务状态或分类。
     */
    public enum Op {
        EQ,
        NE,
        GT,
        LT,
        GE,
        LE,
        LIKE,
        IN
    }

    /**
     * {@code FilterRule} 封装平台基础设施模块中不可变的结构化数据，并作为相关操作之间的值对象。
     * @param field field 属性，表示该记录组件承载的数据。
     * @param op op 属性，表示该记录组件承载的数据。
     * @param value 值，表示该记录组件承载的数据。
     */
    public record FilterRule(String field, Op op, Object value) {}

    /**
     * {@code SortRule} 封装平台基础设施模块中不可变的结构化数据，并作为相关操作之间的值对象。
     * @param field field 属性，表示该记录组件承载的数据。
     * @param asc asc 属性，表示该记录组件承载的数据。
     * @param nullFirst nullFirst 属性，表示该记录组件承载的数据。
     */
    public record SortRule(String field, boolean asc, boolean nullFirst) {
        /**
         * {@code SortRule} 创建并初始化当前类型实例。
         *
         * @param field 参数值，用于执行当前操作。
         * @param asc 参数值，用于执行当前操作。
         */
        public SortRule(String field, boolean asc) {
            this(field, asc, false);
        }
    }

    // ----------------------- MethodHandle 缓存 -----------------------
    private static final Map<Class<?>, Map<String, MethodHandle>> HANDLE_CACHE =
            new ConcurrentHashMap<>();

    private static MethodHandle getGetter(Class<?> clazz, String field) {
        return HANDLE_CACHE
                .computeIfAbsent(clazz, k -> new ConcurrentHashMap<>())
                .computeIfAbsent(
                        field,
                        f -> {
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
                .<Predicate<T>>map(
                        rule ->
                                t -> {
                                    Object fv = getFieldValue(t, rule.field());
                                    Object val = rule.value();

                                    return switch (rule.op()) {
                                        case EQ -> Objects.equals(fv, val);
                                        case NE -> !Objects.equals(fv, val);
                                        case GT -> compare(fv, val) > 0;
                                        case LT -> compare(fv, val) < 0;
                                        case GE -> compare(fv, val) >= 0;
                                        case LE -> compare(fv, val) <= 0;
                                        case LIKE ->
                                                fv != null
                                                        && fv.toString()
                                                                .toLowerCase()
                                                                .contains(
                                                                        val.toString()
                                                                                .toLowerCase());
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
    @SuppressWarnings({"unchecked", "rawtypes"})
    private static <T> Comparator<T> buildComparator(List<SortRule> sorts) {
        if (sorts == null || sorts.isEmpty()) return (o1, o2) -> 0;

        Comparator<T> comparator = (o1, o2) -> 0;

        for (SortRule rule : sorts) {
            Comparator<T> c =
                    Comparator.comparing(
                            o -> (Comparable<Object>) getFieldValue(o, rule.field()),
                            (a, b) -> compareWithNull(a, b, rule.asc(), rule.nullFirst()));
            comparator = comparator.thenComparing(c);
        }

        return comparator;
    }

    private static int compareWithNull(
            Comparable<Object> a, Comparable<Object> b, boolean asc, boolean nullFirst) {
        if (a == null && b == null) return 0;
        if (a == null) return nullFirst ? -1 : 1;
        if (b == null) return nullFirst ? 1 : -1;

        int res = a.compareTo(b);
        return asc ? res : -res;
    }

    // ----------------------- 缓存 Predicate + Comparator -----------------------
    private static final Cache<String, Predicate<?>> PREDICATE_CACHE =
            Caffeine.newBuilder().maximumSize(500).expireAfterAccess(1, TimeUnit.HOURS).build();
    private static final Cache<String, Comparator<?>> COMPARATOR_CACHE =
            Caffeine.newBuilder().maximumSize(500).expireAfterAccess(1, TimeUnit.HOURS).build();

    @SuppressWarnings("unchecked")
    private static <T> Predicate<T> getCachedPredicate(String key, List<FilterRule> filters) {
        return (Predicate<T>) PREDICATE_CACHE.get(key, k -> buildPredicate(filters));
    }

    @SuppressWarnings("unchecked")
    private static <T> Comparator<T> getCachedComparator(String key, List<SortRule> sorts) {
        return (Comparator<T>) COMPARATOR_CACHE.get(key, k -> buildComparator(sorts));
    }

    // ----------------------- 查询主方法 -----------------------
    /**
     * {@code query} 查询并返回当前操作所需的数据。
     *
     * @param list 参数值，用于执行当前操作。
     * @param filters 参数值，用于执行当前操作。
     * @param sorts 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    public static <T> List<T> query(List<T> list, List<FilterRule> filters, List<SortRule> sorts) {
        if (list == null || list.isEmpty()) return List.of();

        String key = buildCacheKey(filters, sorts);

        Predicate<T> predicate = getCachedPredicate(key, filters);
        Comparator<T> comparator = getCachedComparator(key, sorts);

        return list.stream().filter(predicate).sorted(comparator).collect(Collectors.toList());
    }

    private static String buildCacheKey(List<FilterRule> filters, List<SortRule> sorts) {
        String f = filters == null ? "" : filters.toString();
        String s = sorts == null ? "" : sorts.toString();
        return f + "|" + s;
    }

    // ----------------------- 链式 Builder -----------------------
    /**
     * {@code Builder} 承载平台基础设施模块的领域状态或协作行为，负责维护本类型的职责边界。
     */
    public static class Builder<T> {
        /**
         * 来源，表示当前对象中的对应属性。
         */
        private final List<T> source;
        private final List<FilterRule> filters = new ArrayList<>();
        private final List<SortRule> sorts = new ArrayList<>();

        /**
         * {@code Builder} 创建并初始化当前类型实例。
         *
         * @param source 参数值，用于执行当前操作。
         */
        public Builder(List<T> source) {
            this.source = source;
        }

        /**
         * {@code filter} 执行当前类型定义的业务操作。
         *
         * @param field 参数值，用于执行当前操作。
         * @param op 参数值，用于执行当前操作。
         * @param val 参数值，用于执行当前操作。
         *
         * @return 返回当前操作产生的结果。
         */
        public Builder<T> filter(String field, Op op, Object val) {
            filters.add(new FilterRule(field, op, val));
            return this;
        }

        /**
         * {@code sort} 执行当前类型定义的业务操作。
         *
         * @param field 参数值，用于执行当前操作。
         * @param asc 参数值，用于执行当前操作。
         *
         * @return 返回当前操作产生的结果。
         */
        public Builder<T> sort(String field, boolean asc) {
            sorts.add(new SortRule(field, asc));
            return this;
        }

        /**
         * {@code sort} 执行当前类型定义的业务操作。
         *
         * @param field 参数值，用于执行当前操作。
         * @param asc 参数值，用于执行当前操作。
         * @param nullFirst 参数值，用于执行当前操作。
         *
         * @return 返回当前操作产生的结果。
         */
        public Builder<T> sort(String field, boolean asc, boolean nullFirst) {
            sorts.add(new SortRule(field, asc, nullFirst));
            return this;
        }

        /**
         * {@code build} 执行当前类型定义的业务操作。
         *
         * @return 返回当前操作产生的结果。
         */
        public List<T> build() {
            return DynamicQuery.query(source, filters, sorts);
        }
    }
}
