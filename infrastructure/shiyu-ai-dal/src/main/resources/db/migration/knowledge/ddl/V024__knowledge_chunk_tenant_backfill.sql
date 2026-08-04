-- Chunks created by the original embedded worker did not carry the tenant
-- context because they were written from a background thread. Restore the
-- source-of-truth tenant and space values from their owning documents.
UPDATE `vector_knowledge_chunk` c
SET `tenant_id` = (SELECT d.`tenant_id` FROM `knowledge_document` d
                   WHERE d.`id` = c.`document_id`)
WHERE c.`tenant_id` IS NULL;

UPDATE `vector_knowledge_chunk` c
SET `space_id` = (SELECT d.`space_id` FROM `knowledge_document` d
                  WHERE d.`id` = c.`document_id`)
WHERE c.`space_id` IS NULL;
