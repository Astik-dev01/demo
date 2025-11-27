-- V018: Remove duplicate task priorities
-- Keep uppercase versions (CRITICAL, HIGH, MEDIUM, LOW, NONE) and remove lowercase duplicates

-- First, update any tasks that reference the lowercase priority IDs to use uppercase ones
UPDATE tasks t
SET priority_id = (
    SELECT p2.id
    FROM hb_task_priority p2
    WHERE UPPER(p2.alias) = UPPER(
        (SELECT p1.alias FROM hb_task_priority p1 WHERE p1.id = t.priority_id)
    )
    AND p2.alias = UPPER(p2.alias)
    LIMIT 1
)
WHERE EXISTS (
    SELECT 1 FROM hb_task_priority p
    WHERE p.id = t.priority_id
    AND p.alias != UPPER(p.alias)
);

-- Delete the lowercase duplicates
DELETE FROM hb_task_priority
WHERE alias IN ('high', 'medium', 'low', 'highest', 'lowest');
