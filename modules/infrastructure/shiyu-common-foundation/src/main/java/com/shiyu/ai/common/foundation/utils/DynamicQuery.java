package com.shiyu.ai.common.foundation.utils;

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

/**
 * 封装 Dynamic 操作所需的请求条件和输入数据。
 */
public class DynamicQuery {

    /**
     * 定义 Op 可用的枚举值及其业务语义。
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
     * 封装 Filter Rule 相关的不可变数据及其字段约束。
     */
    public record FilterRule(String field, Op op, Object value) {}

    /**
     * 封装 Sort Rule 相关的不可变数据及其字段约束。
     */
    public record SortRule(String field, boolean asc, boolean nullFirst) {
        /**
         * 执行 Sort Rule 相关业务操作，并维护必要的状态和协作关系。
         *
         * @param field 用于完成本次业务处理的 field 参数。
         * @param asc 用于完成本次业务处理的 asc 参数。
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
     * 查询 Dynamic 相关业务数据，并返回处理结果。
     *
     * @param list 用于完成本次业务处理的 list 参数。
     * @param filters 用于完成本次业务处理的 filters 参数。
     * @param sorts 用于完成本次业务处理的 sorts 参数。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
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
     * 构建 Builder 相关的对象、流程或运行时配置。
     */
    public static class Builder<T> {
        /**
         * 来源，表示当前对象中的对应属性。
         */
        private final List<T> source;
        private final List<FilterRule> filters = new ArrayList<>();
        private final List<SortRule> sorts = new ArrayList<>();

        /**
         * 构建或转换 Builder 相关业务操作，并维护必要的状态和协作关系。
         *
         * @param source 用于完成本次业务处理的 source 参数。
         */
        public Builder(List<T> source) {
            this.source = source;
        }

        /**
         * 执行 Builder 相关业务数据，并返回处理结果。
         *
         * @param field 用于完成本次业务处理的 field 参数。
         * @param op 用于完成本次业务处理的 op 参数。
         * @param val 用于完成本次业务处理的 val 参数。
         * @return 返回 Builder 相关操作生成的结果数据。
         */
        public Builder<T> filter(String field, Op op, Object val) {
            filters.add(new FilterRule(field, op, val));
            return this;
        }

        /**
         * 执行 Builder 相关业务数据，并返回处理结果。
         *
         * @param field 用于完成本次业务处理的 field 参数。
         * @param asc 用于完成本次业务处理的 asc 参数。
         * @return 返回 Builder 相关操作生成的结果数据。
         */
        public Builder<T> sort(String field, boolean asc) {
            sorts.add(new SortRule(field, asc));
            return this;
        }

        /**
         * 执行 Builder 相关业务数据，并返回处理结果。
         *
         * @param field 用于完成本次业务处理的 field 参数。
         * @param asc 用于完成本次业务处理的 asc 参数。
         * @param nullFirst 用于完成本次业务处理的 nullFirst 参数。
         * @return 返回 Builder 相关操作生成的结果数据。
         */
        public Builder<T> sort(String field, boolean asc, boolean nullFirst) {
            sorts.add(new SortRule(field, asc, nullFirst));
            return this;
        }

        /**
         * 构建或转换 Builder 相关业务数据，并返回处理结果。
         *
         * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
         */
        public List<T> build() {
            return DynamicQuery.query(source, filters, sorts);
        }
    }
}
