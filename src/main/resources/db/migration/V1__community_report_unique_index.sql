-- 기존 운영 DB는 ddl-auto로 생성된 스키마를 기준으로 baseline 한 뒤 실행한다.
-- 신규 설치 시에는 별도의 초기 스키마 프로비저닝 후 이 마이그레이션을 실행한다.
DO $$
BEGIN
    IF to_regclass('public.community_post_reports') IS NOT NULL THEN
        CREATE UNIQUE INDEX IF NOT EXISTS uk_community_report_post_user
            ON community_post_reports (post_id, user_id);
    END IF;
END $$;
