-- V014: Remove duplicate task statuses (lowercase aliases)
-- There were duplicate statuses with lowercase aliases (todo, in_progress, review, done)
-- that duplicated the uppercase ones (TODO, IN_PROGRESS, IN_REVIEW, DONE)

-- Step 1: Migrate tasks from lowercase status IDs to uppercase status IDs
-- (Only if the lowercase statuses exist)

-- todo -> TODO
UPDATE tasks
SET status_id = (SELECT id FROM hb_task_status WHERE alias = 'TODO' LIMIT 1)
WHERE status_id IN (SELECT id FROM hb_task_status WHERE alias = 'todo');

-- in_progress -> IN_PROGRESS
UPDATE tasks
SET status_id = (SELECT id FROM hb_task_status WHERE alias = 'IN_PROGRESS' LIMIT 1)
WHERE status_id IN (SELECT id FROM hb_task_status WHERE alias = 'in_progress');

-- review -> IN_REVIEW
UPDATE tasks
SET status_id = (SELECT id FROM hb_task_status WHERE alias = 'IN_REVIEW' LIMIT 1)
WHERE status_id IN (SELECT id FROM hb_task_status WHERE alias = 'review');

-- done -> DONE
UPDATE tasks
SET status_id = (SELECT id FROM hb_task_status WHERE alias = 'DONE' LIMIT 1)
WHERE status_id IN (SELECT id FROM hb_task_status WHERE alias = 'done');

-- Step 2: Migrate board_columns from lowercase status IDs to uppercase status IDs

UPDATE board_columns
SET status_id = (SELECT id FROM hb_task_status WHERE alias = 'TODO' LIMIT 1)
WHERE status_id IN (SELECT id FROM hb_task_status WHERE alias = 'todo');

UPDATE board_columns
SET status_id = (SELECT id FROM hb_task_status WHERE alias = 'IN_PROGRESS' LIMIT 1)
WHERE status_id IN (SELECT id FROM hb_task_status WHERE alias = 'in_progress');

UPDATE board_columns
SET status_id = (SELECT id FROM hb_task_status WHERE alias = 'IN_REVIEW' LIMIT 1)
WHERE status_id IN (SELECT id FROM hb_task_status WHERE alias = 'review');

UPDATE board_columns
SET status_id = (SELECT id FROM hb_task_status WHERE alias = 'DONE' LIMIT 1)
WHERE status_id IN (SELECT id FROM hb_task_status WHERE alias = 'done');

-- Step 3: Delete the duplicate lowercase statuses
DELETE FROM hb_task_status WHERE alias IN ('todo', 'in_progress', 'review', 'done');
