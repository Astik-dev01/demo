-- Add default boards to projects that don't have one

-- Insert boards for projects without boards
INSERT INTO boards (id, project_id, name, is_default, position, is_deleted, created_at, updated_at)
SELECT
    gen_random_uuid(),
    p.id,
    'Main Board',
    true,
    0,
    false,
    NOW(),
    NOW()
FROM projects p
WHERE p.is_deleted = false
  AND NOT EXISTS (
    SELECT 1 FROM boards b WHERE b.project_id = p.id AND b.is_deleted = false
  );

-- Insert default columns for newly created boards
-- To Do column
INSERT INTO board_columns (id, board_id, name, color, position, is_deleted, created_at, updated_at)
SELECT
    gen_random_uuid(),
    b.id,
    'To Do',
    '#E5E7EB',
    0,
    false,
    NOW(),
    NOW()
FROM boards b
WHERE NOT EXISTS (
    SELECT 1 FROM board_columns bc WHERE bc.board_id = b.id AND bc.is_deleted = false
);

-- In Progress column
INSERT INTO board_columns (id, board_id, name, color, position, is_deleted, created_at, updated_at)
SELECT
    gen_random_uuid(),
    b.id,
    'In Progress',
    '#FEF3C7',
    1,
    false,
    NOW(),
    NOW()
FROM boards b
WHERE NOT EXISTS (
    SELECT 1 FROM board_columns bc WHERE bc.board_id = b.id AND bc.name = 'In Progress' AND bc.is_deleted = false
)
AND EXISTS (
    SELECT 1 FROM board_columns bc WHERE bc.board_id = b.id AND bc.name = 'To Do' AND bc.is_deleted = false
);

-- Done column
INSERT INTO board_columns (id, board_id, name, color, position, is_deleted, created_at, updated_at)
SELECT
    gen_random_uuid(),
    b.id,
    'Done',
    '#D1FAE5',
    2,
    false,
    NOW(),
    NOW()
FROM boards b
WHERE NOT EXISTS (
    SELECT 1 FROM board_columns bc WHERE bc.board_id = b.id AND bc.name = 'Done' AND bc.is_deleted = false
)
AND EXISTS (
    SELECT 1 FROM board_columns bc WHERE bc.board_id = b.id AND bc.name = 'In Progress' AND bc.is_deleted = false
);
