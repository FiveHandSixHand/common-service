# Common Module

다잇다 프로젝트의 공통 모듈입니다.  
모든 마이크로서비스에서 공통으로 사용하는 응답 형식, 예외 처리, 도메인 기반 클래스를 제공합니다.

---

## 📌 모듈 정보

- Group: `com.fhsh.daitda`
- Artifact: `common`
- Version: `0.1.3-SNAPSHOT`

---

## 📦 패키지 구조

```
com.fhsh.daitda
├── domain
│   ├── BaseEntity            # createdAt, updatedAt, deletedAt
│   └── BaseUserEntity        # BaseEntity + createdBy, updatedBy, deletedBy + Soft Delete
├── exception
│   ├── BusinessException     # 비즈니스 예외
│   ├── CommonErrorCode       # 공통 에러 코드
│   ├── ErrorCode             # 에러 코드 인터페이스
│   ├── ErrorResponse         # 에러 응답 DTO
│   └── GlobalExceptionHandler # 전역 예외 처리
└── response
    └── CommonResponse        # 공통 응답 래퍼
```

---

## 🔧 의존성 추가 방법

`build.gradle`에 아래를 추가합니다.

```groovy
implementation 'com.fhsh.daitda:common:0.1.3-SNAPSHOT'
```

Gradle 새로고침:
```bash
./gradlew dependencies --refresh-dependencies
```
또는 IntelliJ Gradle 패널 새로고침 버튼 클릭

> ⚠️ 버전 업데이트 시 팀 공지를 확인하고 버전을 맞춰주세요.

---

## 📖 주요 클래스

### CommonResponse\<T\>

모든 API 응답은 `CommonResponse<T>`로 래핑합니다.

```json
{
  "status": 200,
  "message": "요청이 성공적으로 처리되었습니다.",
  "data": { ... }
}
```

**사용 예시:**
```java
// 성공 - data 포함
return ResponseEntity.ok(CommonResponse.success(data));

// 성공 - data 없음
return ResponseEntity.ok(CommonResponse.success());

// 실패
return ResponseEntity.badRequest()
    .body(CommonResponse.fail(400, "잘못된 요청입니다."));
```

---

### BaseUserEntity

모든 엔티티는 `BaseUserEntity`를 상속합니다.

| 필드 | 타입 | 설명 |
|------|------|------|
| `createdAt` | `LocalDateTime` | 생성 시각 |
| `updatedAt` | `LocalDateTime` | 수정 시각 |
| `deletedAt` | `LocalDateTime` | 삭제 시각 (Soft Delete) |
| `createdBy` | `String` | 생성자 |
| `updatedBy` | `String` | 수정자 |
| `deletedBy` | `String` | 삭제자 |

**Soft Delete 사용 예시:**
```java
entity.softDelete(deletedBy);  // 논리적 삭제
entity.restore(restoredBy);    // 복구
entity.isDeleted();            // 삭제 여부 확인
```

> 조회 시 반드시 `deleted_at IS NULL` 조건을 포함해야 합니다.

---

### ErrorCode / BusinessException

`BusinessException`은 `ErrorCode`를 받아 예외를 발생시킵니다.

```java
// 사용 예시
throw new BusinessException(CommonErrorCode.INVALID_INPUT);
```

**공통 에러 코드 (`CommonErrorCode`):**

| 코드 | HTTP Status | 설명 |
|------|-------------|------|
| `UNAUTHORIZED` | 401 | 인증이 필요합니다 |
| `FORBIDDEN` | 403 | 권한이 없습니다 |
| `INVALID_INPUT` | 400 | 잘못된 입력입니다 |
| `INVALID_PATH` | 404 | 잘못된 경로입니다 |
| `INTERNAL_SERVER_ERROR` | 500 | 서버 오류가 발생했습니다 |
| `CONFLICT` | 409 | 이미 존재하는 데이터입니다 |

---

### AuthenticatedUser

Gateway에서 전달된 헤더로 인증된 사용자 정보를 추출합니다.

```java
// 컨트롤러에서 사용 예시
@GetMapping
public ResponseEntity<?> getDelivery(
    @RequestHeader("X-User-Id") String userId,
    @RequestHeader("X-User-Email") String email,
    @RequestHeader("X-User-Role") String role
) {
    AuthenticatedUser user = AuthenticatedUser.fromHeaders(userId, email, role);
    // user.userId(), user.role(), user.isAdmin() 등 사용 가능
}
```

---

## 🔄 버전 히스토리

| 버전 | 주요 변경 사항 |
|------|---------------|
| `0.1.3-SNAPSHOT` | 현재 버전 |
| `0.1.1-SNAPSHOT` | `ErrorCode` enum → interface 변경, `CommonErrorCode` 분리 |
| `0.1.0-SNAPSHOT` | 최초 릴리즈 |
