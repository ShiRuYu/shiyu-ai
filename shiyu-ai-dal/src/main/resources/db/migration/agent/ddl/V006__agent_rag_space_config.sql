UPDATE `agent_version`
SET `graph_config` = REPLACE(`graph_config`, '"similarityThreshold"', '"scoreThreshold"')
WHERE `graph_config` LIKE '%"nodeType":"RAG_RETRIEVAL"%';
