-- PRE is the single stored direction for a prerequisite edge. NEXT rows were
-- legacy mirror edges and made the same pair render as both 前置 and 后置.
DELETE FROM `knowledge_relation`
WHERE `relation_type` = 'NEXT';
