# ParQ - 주차장 찾기 앱

## 📱 앱 소개
ParQ는 사용자가 주변 주차장을 쉽게 찾고, 실시간 주차 정보를 확인할 수 있는 안드로이드 앱입니다.

## 🚀 주요 기능
- 🗺️ Google Maps 기반 지도 서비스
- 🔍 주차장 검색 기능
- 📍 실시간 주차장 정보 표시
- 🧭 네이버 지도, Tmap, 카카오내비 연동
- 💾 검색 기록 및 즐겨찾기 기능
- 👤 사용자 로그인/회원가입
- ⭐ 주차장 즐겨찾기 기능

## 🔗 백엔드 연동

### 스프링 백엔드 서버
이 앱은 Spring Boot 기반 백엔드 서버와 연동됩니다.

#### 서버 실행 방법
```bash
cd parq-backend
./mvnw spring-boot:run
```

#### API 엔드포인트
- `GET /api/parkinglots` - 전체 주차장 조회
- `POST /api/parkinglots` - 주차장 등록
- `POST /api/user/register` - 회원가입
- `POST /api/user/login` - 로그인
- `POST /api/favorites` - 즐겨찾기 추가

### 연동 설정
1. **서버 URL 설정**: `RetrofitClient.kt`에서 BASE_URL 설정
2. **네트워크 보안**: `network_security_config.xml`에 서버 도메인 추가
3. **권한 설정**: `AndroidManifest.xml`에 인터넷 권한 확인

## 🛠️ 기술 스택
- **Frontend**: Android (Kotlin)
- **Backend**: Spring Boot (Java)
- **Database**: MySQL
- **Maps**: Google Maps API
- **Network**: Retrofit, OkHttp
- **Architecture**: RESTful API

## 📋 설치 및 실행

### 안드로이드 앱
1. Android Studio에서 프로젝트 열기
2. Gradle 동기화
3. 에뮬레이터 또는 실제 기기에서 실행

### 백엔드 서버
1. Java 17 이상 설치
2. Maven 설치
3. `parq-backend` 폴더에서 서버 실행

## 🔧 설정 파일
- `RetrofitClient.kt`: API 서버 URL 설정
- `network_security_config.xml`: 네트워크 보안 설정
- `AndroidManifest.xml`: 앱 권한 및 설정

## 📝 주의사항
- 백엔드 서버가 실행 중이어야 앱이 정상 작동합니다
- Google Maps API 키가 필요합니다
- 인터넷 연결이 필요합니다