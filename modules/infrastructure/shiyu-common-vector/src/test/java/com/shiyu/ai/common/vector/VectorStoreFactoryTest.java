package com.shiyu.ai.common.vector;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.shiyu.ai.common.vector.config.VectorStoreProperties;
import com.shiyu.ai.common.vector.factory.VectorStoreFactory;

import org.junit.jupiter.api.Test;

/**
 * 验证 向量 Store Factory 相关功能、边界条件、异常路径和协作行为。
 */
class VectorStoreFactoryTest {

    @Test
    void pgvectorRequiresJdbcInfrastructure() {
        VectorStoreProperties properties = new VectorStoreProperties();
        properties.setDimension(3);

        assertThatThrownBy(() -> VectorStoreFactory.create("pgvector", properties))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("JdbcTemplate");
    }
}
