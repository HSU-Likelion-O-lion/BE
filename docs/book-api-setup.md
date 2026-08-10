# 외부 도서 검색 API 설정

현재 도서 검색은 외부 제공자를 우선 사용하고, 외부 제공자가 비활성화되었거나 호출에 실패하면 로컬 `books` 테이블을 사용합니다.

## 연동 제공자

1. 네이버 책 검색 API
2. 카카오 책 검색 API

두 제공자의 응답은 `BookSearchProvider`가 `BookSearchResult`로 변환한 뒤 기존 `/api/books/search` 응답 형식으로 반환합니다.

## 필요한 환경 변수

네이버 개발자센터에서 검색 API를 사용하는 애플리케이션을 등록한 뒤 다음 값을 설정합니다.

```dotenv
BOOK_API_NAVER_CLIENT_ID=발급받은_클라이언트_ID
BOOK_API_NAVER_CLIENT_SECRET=발급받은_클라이언트_시크릿
```

카카오 디벨로퍼스에서 REST API 키를 발급한 뒤 다음 값을 설정합니다.

```dotenv
BOOK_API_KAKAO_REST_API_KEY=발급받은_REST_API_키
```

키는 저장소에 커밋하지 말고 로컬 `.env`, 배포 환경 변수 또는 GitHub Actions Secrets로 관리합니다.

## 팀원이 진행할 순서

1. 네이버 애플리케이션을 등록하고 책 검색 API 권한을 활성화합니다.
2. 카카오 앱을 생성하고 REST API 키를 확인합니다.
3. 로컬 실행 환경에 위 환경 변수를 등록합니다.
4. `GET /api/books/search?q=아몬드`로 외부 검색 결과를 확인합니다.
5. 키를 제거한 뒤 같은 요청을 보내 로컬 DB fallback이 동작하는지 확인합니다.
6. 실제 키는 PR이나 이슈에 남기지 않고, 팀 배포 환경에도 동일한 이름으로 등록합니다.

알라딘 OpenAPI와 국립중앙도서관 Open API는 추후 ISBN 기반 상세 조회나 도서 메타데이터 보강이 필요할 때 동일한 `BookSearchProvider` 확장 지점에 추가할 수 있습니다.
