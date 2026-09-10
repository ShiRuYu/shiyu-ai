package com.shiyu.ai.common.vector;

import com.shiyu.ai.common.vector.config.VectorStoreProperties;
import com.shiyu.ai.common.vector.factory.VectorStoreFactory;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
