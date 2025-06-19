# ParQ Backend Server

## 🚀 서버 실행 방법

### 1. 필수 요구사항
- Java 17 이상
- Maven 3.6 이상
- MySQL 데이터베이스

### 2. 서버 실행
```bash
# 프로젝트 루트 디렉토리에서
cd parq-backend

# Maven으로 빌드 및 실행
./mvnw spring-boot:run

# 또는
mvn spring-boot:run
```

### 3. 서버 접속 확인
- 서버가 정상적으로 실행되면 `http://localhost:8080`에서 접속 가능
- API 테스트: `http://localhost:8080/api/parkinglots`

## 📡 API 엔드포인트

### 주차장 관련 API
- `GET /api/parkinglots` - 전체 주차장 조회
- `GET /api/parkinglots/{name}` - 이름으로 주차장 조회
- `POST /api/parkinglots` - 주차장 등록

### 사용자 관련 API
- `POST /api/user/register` - 회원가입
- `POST /api/user/login` - 로그인

### 즐겨찾기 관련 API
- `POST /api/favorites` - 즐겨찾기 추가
- `DELETE /api/favorites/{id}` - 즐겨찾기 삭제

## 🔧 안드로이드 앱 연동

### 1. 서버 URL 설정
안드로이드 앱의 `RetrofitClient.kt`에서 BASE_URL을 실제 서버 주소로 변경:

```kotlin
// 로컬 테스트용
private const val BASE_URL = "http://10.0.2.2:8080/"

// 실제 서버용
private const val BASE_URL = "http://project-db-campus.smhrd.com:8080/"
```

### 2. 네트워크 보안 설정
안드로이드 앱의 `network_security_config.xml`에 서버 도메인 추가:

```xml
<domain-config cleartextTrafficPermitted="true">
    <domain includeSubdomains="true">project-db-campus.smhrd.com</domain>
</domain-config>
```

## 🗄️ 데이터베이스 설정

현재 설정된 데이터베이스 정보:
- Host: project-db-campus.smhrd.com:3307
- Database: campus_25SW_LI_p2_4
- Username: campus_25SW_LI_p2_4
- Password: smhrd4

## 📝 주의사항

1. **CORS 설정**: 현재 `@CrossOrigin(origins = "*")`로 설정되어 있어 모든 도메인에서 접근 가능
2. **보안**: 실제 운영 환경에서는 적절한 보안 설정 필요
3. **포트**: 기본 포트 8080 사용, 변경 시 안드로이드 앱의 BASE_URL도 함께 변경 필요 