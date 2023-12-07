CREATE TABLE TestContext
(
    eid       bigserial PRIMARY KEY,
    id        text    NOT NULL,
    context   jsonb   NOT NULL,
    processed boolean NOT NULL DEFAULT false,
    state     text    NOT NULL,
    UNIQUE (id, state)
);

CREATE INDEX test_context_index ON testcontext (processed, state);