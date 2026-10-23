# 📝 Secret Memo (시크릿 메모)

> **EncryptedSharedPreferences + MVVM + Koin** 기반의 구글 플레이 배포 표준 보안 메모 안드로이드 애플리케이션  
> 바탕화면 위젯(AppWidget) 및 상단 제어센터 타일(Quick Settings Tile) 지원

---

## 🌟 주요 기능 (Key Features)

- 🔐 **보안 암호화 저장소**: Jetpack Security (`EncryptedSharedPreferences`)를 적용하여 메모 데이터를 AES-256 규격으로 암호화 저장
- 📌 **상단 고정 및 메모 정렬**: 중요한 메모를 상단에 고정 표시하며, 최신 수정일순 자동 정렬
- 🔍 **실시간 메모 검색**: 제목 및 내용 기준 실시간 검색 필터링
- 🖼️ **바탕화면 카드 위젯 (`AppWidgetProvider`)**: 홈 화면에서 최신/고정 메모 실시간 미리보기 및 메모 변경 시 위젯 자동 업데이트
- ⚡ **상단바 빠른 메모 타일 (`TileService`)**: 상단 제어센터(Quick Settings)에서 단 한 번의 터치로 메모 작성 화면 즉시 열기
- 📱 **Material 3 UI & Responsive Design**: Staggered Grid 2열 카드 레이아웃 및 Edge-to-Edge 전면 화면 적용

---

## 🛠 아키텍처 및 기술 스택 (Tech Stack)

| 구분 | 적용 기술 |
| :--- | :--- |
| **Language** | Kotlin |
| **Architecture** | MVVM (Model - View - ViewModel) |
| **Dependency Injection** | Koin 4.0 (`koin-android`) |
| **Async & Reactive Data** | Kotlin Coroutines, StateFlow |
| **Security** | Jetpack Security (`androidx.security:security-crypto`) |
| **UI Framework** | Material3, ViewBinding, StaggeredGridLayoutManager |
| **System Integration** | AppWidget (`AppWidgetProvider`), Quick Settings (`TileService`) |
| **Data Serialization** | Gson, ProGuard/R8 Keep Rules |
| **Testing** | JUnit4 (Repository & Data Source Unit Tests) |

---

## 📐 패키지 구조 (Project Structure)

```
com.dev.secretmemo
├── data
│   ├── datasource       # EncryptedSharedPreferences 기반 데이터 소스
│   ├── model            # Memo 데이터 모델 (UUID, title, content, isPinned 등)
│   └── repository       # 메모 저장소 인터페이스 및 정렬/CRUD 구현체
├── di
│   └── AppModule.kt     # Koin 싱글톤 & ViewModel 의존성 모듈
├── ui
│   ├── main             # 메인 화면 (MainActivity, MainViewModel, MemoAdapter)
│   └── edit             # 메모 편집 화면 (MemoEditActivity, MemoEditViewModel)
└── widget
    ├── MemoAppWidget.kt        # 바탕화면 위젯 Provider
    └── QuickMemoTileService.kt # 상단 제어센터 TileService
```

---

## ⚙️ 빌드 및 설치 방법 (Getting Started)

### 요구 사항
- Android Studio Ladybug (2024.2.1) 이상
- JDK 11 이상
- Min SDK: 33 (Android 13+) / Target SDK: 37

### 빌드 명령어
```bash
# Debug APK 빌드
./gradlew app:assembleDebug

# Release APK 빌드 (R8 난독화 적용)
./gradlew app:assembleRelease

# 단위 테스트 실행
./gradlew app:testDebugUnitTest
```
