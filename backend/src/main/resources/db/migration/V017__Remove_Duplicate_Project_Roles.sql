-- V017: Remove duplicate project roles
-- Keep only uppercase alias versions (OWNER, ADMIN, MEMBER, VIEWER)

-- First, update any project_members that reference the lowercase role IDs to use uppercase ones
UPDATE project_members pm
SET role_id = (
    SELECT r2.id
    FROM hb_role_in_project r2
    WHERE UPPER(r2.alias) = UPPER(
        (SELECT r1.alias FROM hb_role_in_project r1 WHERE r1.id = pm.role_id)
    )
    AND r2.alias = UPPER(r2.alias)
    LIMIT 1
)
WHERE EXISTS (
    SELECT 1 FROM hb_role_in_project r
    WHERE r.id = pm.role_id
    AND r.alias != UPPER(r.alias)
);

-- Delete the lowercase duplicates (owner, admin, member, viewer)
DELETE FROM hb_role_in_project
WHERE alias IN ('owner', 'admin', 'member', 'viewer');
