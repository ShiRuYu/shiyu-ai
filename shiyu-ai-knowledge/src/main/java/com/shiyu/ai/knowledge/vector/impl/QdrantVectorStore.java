package com.shiyu.ai.knowledge.vector.impl;

import com.shiyu.ai.knowledge.vector.VectorRecord;
import com.shiyu.ai.knowledge.vector.VectorStore;
import com.shiyu.ai.knowledge.vector.config.VectorStoreProperties;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

@Slf4j
public class QdrantVectorStore implements VectorStore {

    private final int dimension;
    private final String collection;
    private Object client;

    public QdrantVectorStore(VectorStoreProperties properties) {
        VectorStoreProperties.Qdrant qdrant = properties.getQdrant();
        this.dimension = properties.getDimension();
        this.collection = qdrant.getCollection();
        initClient(qdrant);
    }

    private void initClient(VectorStoreProperties.Qdrant config) {
        try {
            Class<?> qdrantGrpcClass = Class.forName("io.qdrant.client.QdrantGrpcClient");
            Class<?> qdrantClientClass = Class.forName("io.qdrant.client.QdrantClient");

            Object grpcClient = qdrantGrpcClass.getMethod("newBuilder", String.class, int.class, boolean.class)
                    .invoke(null, config.getHost(), config.getPort(), config.isUseTls());
            grpcClient = grpcClient.getClass().getMethod("build").invoke(grpcClient);

            client = qdrantClientClass.getConstructor(grpcClient.getClass().getInterfaces()[0])
                    .newInstance(grpcClient);

            log.info("QdrantVectorStore 已连接: {}:{}, collection={}",
                    config.getHost(), config.getPort(), collection);
        } catch (ClassNotFoundException e) {
            log.warn("qdrant-client 未在 classpath 中，QdrantVectorStore 不可用");
        } catch (Exception e) {
            log.error("Qdrant 连接失败", e);
        }
    }

    @Override
    public void upsert(VectorRecord record) {
        if (client == null) return;
        try {
            Class<?> pointIdClass = Class.forName("io.qdrant.client.grpc.PointId");
            Class<?> valueClass = Class.forName("io.qdrant.client.grpc.Value");
            Class<?> structClass = Class.forName("com.google.protobuf.Struct");

            Object pointId = pointIdClass.getMethod("newBuilder").invoke(null);
            pointId = pointId.getClass().getMethod("setUuid", String.class)
                    .invoke(pointId, record.id());
            pointId = pointId.getClass().getMethod("build").invoke(pointId);

            Map<String, Object> payload = new HashMap<>(record.metadata());
            payload.remove("_score");

            Object struct = structClass.getMethod("newBuilder").invoke(null);
            for (var entry : payload.entrySet()) {
                Object valBuilder = valueClass.getMethod("newBuilder").invoke(null);
                if (entry.getValue() instanceof String s) {
                    valBuilder = valBuilder.getClass().getMethod("setStringValue", String.class).invoke(valBuilder, s);
                } else if (entry.getValue() instanceof Number n) {
                    valBuilder = valBuilder.getClass().getMethod("setDoubleValue", double.class).invoke(valBuilder, n.doubleValue());
                }
                struct = struct.getClass().getMethod("putFields", String.class, valueClass)
                        .invoke(struct, entry.getKey(), valBuilder.getClass().getMethod("build").invoke(valBuilder));
            }
            struct = struct.getClass().getMethod("build").invoke(struct);

            Object pointStruct = Class.forName("io.qdrant.client.grpc.PointStruct")
                    .getMethod("newBuilder", pointId.getClass().getInterfaces()[0], List.class, structClass.getClass().getInterfaces()[0])
                    .invoke(null, pointId, List.of(record.vector()), struct);
            pointStruct = pointStruct.getClass().getMethod("build").invoke(pointStruct);

            client.getClass().getMethod("upsert", String.class, List.class)
                    .invoke(client, collection, List.of(pointStruct));
        } catch (Exception e) {
            log.error("Qdrant upsert 失败: id={}", record.id(), e);
        }
    }

    @Override
    public List<VectorRecord> search(float[] queryVector, int topK) {
        if (client == null) return List.of();
        try {
            Object result = client.getClass().getMethod("search", String.class, float[].class, int.class)
                    .invoke(client, collection, queryVector, topK);

            @SuppressWarnings("unchecked")
            List<Object> scoredPoints = (List<Object>) result.getClass()
                    .getMethod("getResultList").invoke(result);

            List<VectorRecord> records = new ArrayList<>();
            for (Object sp : scoredPoints) {
                String id = (String) sp.getClass().getMethod("getId").invoke(sp);
                double score = (double) sp.getClass().getMethod("getScore").invoke(sp);

                @SuppressWarnings("unchecked")
                Map<String, Object> payload = (Map<String, Object>) sp.getClass()
                        .getMethod("getPayloadMap").invoke(sp);

                Map<String, Object> meta = new LinkedHashMap<>(payload);
                meta.put("_score", score);
                records.add(new VectorRecord(id, null, meta));
            }
            return records;
        } catch (Exception e) {
            log.error("Qdrant search 失败", e);
            return List.of();
        }
    }

    @Override
    public void delete(String id) {
        if (client == null) return;
        try {
            client.getClass().getMethod("delete", String.class, List.class)
                    .invoke(client, collection, List.of(id));
        } catch (Exception e) {
            log.error("Qdrant delete 失败: id={}", id, e);
        }
    }

    @Override
    public boolean supportFilter() {
        return true;
    }
}
