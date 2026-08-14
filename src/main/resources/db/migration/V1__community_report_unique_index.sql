-- 기존 운영 DB는 ddl-auto로 생성된 스키마를 기준으로 baseline 한 뒤 실행한다.
-- 신규 설치 시에는 전체 초기 스키마를 먼저 프로비저닝해야 한다.
DO $$
DECLARE
    duplicate_count BIGINT;
BEGIN
    IF to_regclass('public.community_post_reports') IS NULL THEN
        RAISE EXCEPTION 'community_post_reports table must exist before V1 migration';
    END IF;

    SELECT COUNT(*) INTO duplicate_count
    FROM (
        SELECT post_id, user_id
        FROM community_post_reports
        GROUP BY post_id, user_id
        HAVING COUNT(*) > 1
    ) duplicates;

    IF duplicate_count > 0 THEN
        RAISE EXCEPTION 'duplicate community reports found; clean them before V1 migration';
    END IF;

    CREATE UNIQUE INDEX IF NOT EXISTS uk_community_report_post_user
        ON community_post_reports (post_id, user_id);
END $$;
