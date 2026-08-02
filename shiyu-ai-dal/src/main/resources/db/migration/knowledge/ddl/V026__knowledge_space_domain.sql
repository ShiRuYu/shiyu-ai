-- Classify knowledge spaces by business domain while keeping the knowledge model generic.
ALTER TABLE `knowledge_space`
    ADD COLUMN IF NOT EXISTS `domain_code` VARCHAR(32) NOT NULL DEFAULT 'GENERAL';

UPDATE `knowledge_space`
SET `domain_code` = 'ENTERPRISE'
WHERE `code` = 'enterprise-policy'
  AND (`domain_code` IS NULL OR `domain_code` = 'GENERAL');

UPDATE `knowledge_space`
SET `domain_code` = 'GENERAL'
WHERE `domain_code` IS NULL OR `domain_code` = '';
