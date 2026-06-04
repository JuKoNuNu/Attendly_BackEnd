# Attendly_BackEnd

WeLabs 모니터링 백엔드 (Spring Boot 3.2 / Java 21 / MySQL 8).

## 빠른 시작 (다른 노트북에서 동일 세팅)

### 1. 사전 준비
- JDK 21
- Docker Desktop (DB를 Docker로 띄울 경우)

### 2. 환경변수 설정
```bash
cp .env.example .env
```
`.env` 기본값: `root` / `12345678`, DB `welabs_monitoring`.
- 네이티브 MySQL이 이미 **3306**을 쓰면 → `.env`의 `DB_PORT=3307` 그대로 (Docker가 3307로 노출).
- Docker만 쓰고 3306으로 가려면 → `.env`의 `DB_PORT=3306`으로 변경.

> `.env`는 `docker-compose`와 앱(`spring-dotenv`)이 **함께** 읽습니다. 포트/비번은 여기 한 곳만 고치면 둘 다 반영됩니다.

### 3. DB 띄우기 (Docker)
```bash
docker compose up -d
```
- MySQL 8 컨테이너가 뜨고, **최초 기동 시** `src/main/resources/sql/`의
  `schema.sql` → `v6-schema.sql`이 자동 적용됩니다.
- 데이터는 `mysql_data` 볼륨에 보존됩니다.

> 이미 설치된 **네이티브 MySQL**을 쓰려면 Docker 대신 직접 스키마를 적용하세요:
> ```bash
> mysql -h localhost -P 3306 -uroot -p12345678 < src/main/resources/sql/schema.sql
> mysql -h localhost -P 3306 -uroot -p12345678 < src/main/resources/sql/v6-schema.sql
> ```
> 이 경우 `.env`의 `DB_PORT=3306`으로 맞추세요.

### 4. 앱 실행
```bash
./gradlew bootRun        # Windows: .\gradlew.bat bootRun
```
앱: http://localhost:8081

## 설정 메모
- `ddl-auto: validate` — 앱이 스키마를 만들지 않습니다. **반드시 위 SQL을 먼저 적용**해야 기동됩니다.
- Teams 알림은 `.env`의 `TEAMS_WEBHOOK_URL`이 비어있으면 발송을 건너뜁니다.

## DB 초기화(완전 리셋)
```bash
docker compose down -v   # 볼륨까지 삭제 → 다음 up 때 스키마 재적용
docker compose up -d
```
