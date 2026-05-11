# 가계부 (MoneyApp)

Android 네이티브 가계부 앱. 카드 승인 SMS와 결제·송금 알림을 자동으로 읽어 가계부 항목을 만들어 준다. Kotlin + Jetpack Compose + Room.

## 주요 기능

- **자동 수집**
  - SMS 승인문자 (`SmsReceiver`) — 삼성카드, KB국민카드, 하나은행
  - 알림 (`TxNotificationListener`) — 카드/은행/페이 앱 푸시
  - 가맹점명 기반 카테고리 자동 추천
  - 중복 감지 (같은 가맹점·금액 ±2분)
- **승인 흐름**: 자동 감지된 거래는 "대기" 탭에서 승인하기 전까지 합계에 포함되지 않음
- **수동 입력**: 홈에서 FAB → 다이얼로그
- **월별 요약**: 수입 / 지출 / 잔액, 월 이동 가능
- **백업**: JSON/CSV로 내보내기·JSON 불러오기 (SAF 사용)
- **로컬 저장**: Room DB (`money.db`), 외부 전송 없음

## 프로젝트 구조

```
app/src/main/java/com/moneyapp/
  MoneyApplication.kt
  MainActivity.kt
  data/            # Room entity / DAO / repository
  parser/          # SMS 본문 파싱 (카드사별)
  receiver/        # SmsReceiver, NotificationListenerService, Ingestor
  backup/          # JSON/CSV export & import
  ui/              # Compose 화면 + ViewModel + 테마
```

## 빌드

Android Studio Hedgehog 이상에서 열고 그대로 Run. CLI는 `./gradlew assembleDebug`.

> 본 저장소에는 Gradle wrapper 바이너리가 포함되어 있지 않다. 최초 1회 `gradle wrapper --gradle-version 8.7`로 생성하거나 Android Studio가 자동 동기화하도록 둔다.

## 권한 안내

- `RECEIVE_SMS`, `READ_SMS` — 런타임 권한 요청
- 알림 접근 권한 — **설정 → 알림 접근**에서 직접 켜야 함 (앱 내 "설정" 탭의 버튼이 해당 화면을 연다)
- Android 13+: `POST_NOTIFICATIONS`

## 지원 확장

새로운 카드사·은행 추가는 `parser/TransactionParser` 구현체를 만들고 `ParserRegistry.parsers`에 등록하면 된다. 알림 리스너는 `TxNotificationListener.WATCH_PACKAGES`에 패키지명을 추가.

## 한계

- 카드사가 SMS 포맷을 변경하면 정규식 갱신이 필요하다.
- iOS는 지원 불가 (SMS·알림 접근 차단).
