-- PRE is the single stored direction for a prerequisite edge. NEXT rows were
-- legacy mirror edges and made the same pair render as both 前置 and 后置.
-- Only remove NEXT rows that have the reverse PRE edge; standalone NEXT data
-- is kept for compatibility with older integrations.
DELETE FROM `knowledge_relation`
WHERE `relation_type` = 'NEXT'
  AND EXISTS (
      SELECT 1
      FROM `knowledge_relation` prerequisite
      WHERE prerequisite.`space_id` = `knowledge_relation`.`space_id`
        AND prerequisite.`source_id` = `knowledge_relation`.`target_id`
        AND prerequisite.`target_id` = `knowledge_relation`.`source_id`
        AND prerequisite.`relation_type` = 'PRE'
  );
