package com.shiyu.ai.education.storage;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import com.shiyu.ai.common.storage.FileStorageManager;
import com.shiyu.ai.common.storage.StorageProperties;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EducationResourceStorageSeederTest {

    @Test
    void copiesBundledEducationResourcesIntoLocalStorage(@TempDir Path directory) throws Exception {
        StorageProperties storageProperties = new StorageProperties();
        storageProperties.getLocal().setPath(directory.toString());
        FileStorageManager manager = new FileStorageManager(storageProperties);

        StorageSeedProperties seedProperties = new StorageSeedProperties();
        new EducationResourceStorageSeeder(manager, seedProperties).run(null);

        assertEquals(4, manager.list("tenant/1/education-resources").size());
    }
}
