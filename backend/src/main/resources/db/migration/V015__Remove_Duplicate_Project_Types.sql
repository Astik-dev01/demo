-- V015: Remove duplicate project types (lowercase aliases)
-- There were duplicate project types with lowercase aliases (design, marketing, software)
-- that duplicated the uppercase ones (DESIGN, MARKETING, SOFTWARE)

-- Step 1: Migrate projects from lowercase type IDs to uppercase type IDs
-- (Only if the lowercase types exist and are used)

-- design -> DESIGN
UPDATE projects
SET type_id = (SELECT id FROM hb_project_type WHERE alias = 'DESIGN' LIMIT 1)
WHERE type_id IN (SELECT id FROM hb_project_type WHERE alias = 'design');

-- marketing -> MARKETING
UPDATE projects
SET type_id = (SELECT id FROM hb_project_type WHERE alias = 'MARKETING' LIMIT 1)
WHERE type_id IN (SELECT id FROM hb_project_type WHERE alias = 'marketing');

-- software -> SOFTWARE
UPDATE projects
SET type_id = (SELECT id FROM hb_project_type WHERE alias = 'SOFTWARE' LIMIT 1)
WHERE type_id IN (SELECT id FROM hb_project_type WHERE alias = 'software');

-- Step 2: Delete the duplicate lowercase project types
DELETE FROM hb_project_type WHERE alias IN ('design', 'marketing', 'software');
