-- Link board columns to task statuses for auto-move functionality

-- Link "To Do" columns to TODO status
UPDATE taskflow.board_columns bc
SET status_id = (SELECT id FROM taskflow.hb_task_status WHERE alias = 'TODO' LIMIT 1)
WHERE bc.name = 'To Do' AND bc.status_id IS NULL;

-- Link "In Progress" columns to IN_PROGRESS status
UPDATE taskflow.board_columns bc
SET status_id = (SELECT id FROM taskflow.hb_task_status WHERE alias = 'IN_PROGRESS' LIMIT 1)
WHERE bc.name = 'In Progress' AND bc.status_id IS NULL;

-- Link "Done" columns to DONE status
UPDATE taskflow.board_columns bc
SET status_id = (SELECT id FROM taskflow.hb_task_status WHERE alias = 'DONE' LIMIT 1)
WHERE bc.name = 'Done' AND bc.status_id IS NULL;

-- Also handle Russian names if they exist
UPDATE taskflow.board_columns bc
SET status_id = (SELECT id FROM taskflow.hb_task_status WHERE alias = 'TODO' LIMIT 1)
WHERE bc.name IN ('К выполнению', 'Сделать') AND bc.status_id IS NULL;

UPDATE taskflow.board_columns bc
SET status_id = (SELECT id FROM taskflow.hb_task_status WHERE alias = 'IN_PROGRESS' LIMIT 1)
WHERE bc.name IN ('В работе', 'В процессе') AND bc.status_id IS NULL;

UPDATE taskflow.board_columns bc
SET status_id = (SELECT id FROM taskflow.hb_task_status WHERE alias = 'DONE' LIMIT 1)
WHERE bc.name IN ('Готово', 'Выполнено', 'Завершено') AND bc.status_id IS NULL;
