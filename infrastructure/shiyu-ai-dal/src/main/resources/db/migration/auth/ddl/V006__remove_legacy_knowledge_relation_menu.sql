-- Remove the retired standalone knowledge-relation menu from existing H2
-- databases. Relationship editing is now embedded in the graph-insight page.
-- The repeatable seed file covers fresh data; this versioned migration is
-- required for databases that already contain cloned tenant menu rows.

DELETE FROM `auth_role_scope_menu`
WHERE `menu_id` IN (
    SELECT `id`
    FROM `auth_menu`
    WHERE `code` = 'KnowledgeRelation'
       OR `path` IN ('/knowledge/relation', '/knowledge/relations')
       OR `component` LIKE '%knowledge-relation%'
);

DELETE FROM `auth_tenant_menu`
WHERE `menu_id` IN (
    SELECT `id`
    FROM `auth_menu`
    WHERE `code` = 'KnowledgeRelation'
       OR `path` IN ('/knowledge/relation', '/knowledge/relations')
       OR `component` LIKE '%knowledge-relation%'
);

DELETE FROM `auth_menu`
WHERE `code` = 'KnowledgeRelation'
   OR `path` IN ('/knowledge/relation', '/knowledge/relations')
   OR `component` LIKE '%knowledge-relation%';
