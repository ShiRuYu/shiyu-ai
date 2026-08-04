ALTER TABLE `knowledge_space`
    ADD COLUMN IF NOT EXISTS `binding_mode` VARCHAR(20) DEFAULT 'OPTIONAL';

UPDATE `knowledge_space`
SET `binding_mode` = 'OPTIONAL'
WHERE `binding_mode` IS NULL OR `binding_mode` = '';
