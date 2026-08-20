CREATE TABLE IF NOT EXISTS reflection_shares (
    share_id BIGSERIAL PRIMARY KEY,
    reflection_id BIGINT NOT NULL REFERENCES reflections(reflection_id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL,
    theme_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL,
    image_key VARCHAR(500),
    failure_reason VARCHAR(500),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_reflection_shares_user_id
    ON reflection_shares (user_id);

CREATE INDEX IF NOT EXISTS idx_reflection_shares_reflection_id
    ON reflection_shares (reflection_id);
