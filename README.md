# 🔗 URL 단축 서비스

> 긴 URL을 짧고 간결하게 줄여주는 서비스입니다.
> JWT 기반 인증과 Redis를 활용한 Rate Limiting을 직접 설계하고 구현했습니다.

## 📎 링크

| 항목 | 링크 |
|---|---|
| 📝 프로젝트 정리 노션 | [Notion](https://bottlenose-balloon-0b4.notion.site/URL-38f5c23e942f8023990adbad4889aa47?source=copy_link) |
| 📋 API 명세서 | [Postman Documentation](https://documenter.getpostman.com/view/49786142/2sBY4LSN3M) |
| 🌐 ERD 이미지 | [ERD](https://www.erdcloud.com/d/vojQnLkYBiNPmZBbm) |

---

## 🛠 기술 스택

| 분류 | 기술 |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 4.1.0 |
| Database | MySQL 9.6.0 |
| Cache | Redis |
| Auth | JWT (Access Token + Refresh Token) |
| ORM | Spring Data JPA |
| Build | Gradle |
| Test | JUnit5, Mockito |

---

## ✨ 주요 기능

- **JWT 기반 인증** — 회원가입 / 로그인 / Access Token + Refresh Token 발급
- **URL 단축** — 원본 URL을 6자리 고유 코드로 단축
- **리다이렉트** — 단축 URL 접속 시 원본 URL로 302 리다이렉트
- **만료 기간 설정** — 단축 URL 유효기간 설정 (기본 30일)
- **클릭 통계** — 단축 URL별 총 클릭 수 조회
- **중복 URL 방지** — 동일 사용자가 같은 URL 중복 생성 방지
- **Rate Limiting** — Redis 기반 API별 요청 횟수 제한 (1분당 10회)

---

## 🏗 패키지 구조

```text
src/main/java/com/url/jjung/
├── domain/
│   ├── auth/           # 회원가입, 로그인
│   ├── url/            # URL 단축, 조회, 삭제
│   ├── redirect/       # 리다이렉트
│   └── stats/          # 클릭 통계
└── global/
    ├── config/         # Security, Redis 설정
    ├── exception/      # 전역 예외처리, ErrorCode
    ├── filter/         # JWT 필터, Rate Limit 필터
    ├── jwt/            # JWT 생성, 검증
    └── util/           # 공통 응답 형식
```

---

## 🔒 Rate Limiting 전략

API별로 다른 기준으로 요청 횟수를 제한합니다.

| API | 제한 기준 | 이유 |
|---|---|---|
| `POST /api/auth/login` | IP | 로그인 전이라 이메일 식별 불가 |
| `POST /api/urls` | 이메일 | 로그인 필수 API |
| `GET /r/{shortCode}` | 이메일 또는 IP | 토큰 있으면 이메일, 없으면 IP |

---

## ⚙️ 로컬 실행 방법

### 사전 준비
- Java 21
- MySQL 9.6.0
- Redis

### 실행

```bash
# 1. 저장소 클론
git clone https://github.com/JJungH0/url-project.git

# 2. DB 생성
mysql -u root -p
CREATE DATABASE url_shortener;

# 3. application-local.yml 작성
# src/main/resources/application-local.yml 생성 후 아래 내용 입력

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/url_shortener?serverTimezone=Asia/Seoul&characterEncoding=UTF-8
    username: {DB_USERNAME}
    password: {DB_PASSWORD}
  jpa:
    hibernate:
      ddl-auto: create
  data:
    redis:
      host: ${REDIS_HOST:localhost}
      port: 6379
jwt:
  secret: {256bit 이상 시크릿 키}

# 4. 실행
./gradlew bootRun --args='--spring.profiles.active=local'
```

### 환경변수 (배포 시)

| 변수 | 설명 |
|---|---|
| `DB_URL` | MySQL 접속 URL |
| `DB_USERNAME` | DB 사용자명 |
| `DB_PASSWORD` | DB 비밀번호 |
| `JWT_SECRET` | JWT 서명 키 (256bit 이상) |
| `BASE_URL` | 서비스 기본 URL |
| `REDIS_HOST` | Redis 호스트 |
