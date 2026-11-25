CREATE TABLE targets (
    id TEXT PRIMARY KEY,
    category TEXT NOT NULL CHECK (category IN ('socio-economic', 'priority-reform')),
    title TEXT NOT NULL,
    definition TEXT NOT NULL,
    source_url TEXT NOT NULL,
    display_order INTEGER NOT NULL UNIQUE
) STRICT;
