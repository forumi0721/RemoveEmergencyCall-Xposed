# RemoveEmergencyCall

RemoveEmergencyCall은 Android SystemUI(`com.android.systemui`)용 LSPosed 모듈입니다. SystemUI의 긴급 통화 버튼 컨트롤러 흐름을 후킹하여 잠금 화면의 긴급 통화 버튼을 숨깁니다.

## 대상 앱

- `com.android.systemui`

대상 패키지는 libxposed 정적 scope 지원을 위해 `app/src/main/resources/META-INF/xposed/scope.list`에도 선언되어 있습니다.

## 주요 기능

- `EmergencyButtonController.updateEmergencyCallButton`을 후킹합니다.
- 긴급 통화 버튼 view가 attach될 때 숨김 처리합니다.
- `EmergencyButton.setVisibility`의 직접 visibility 변경을 차단합니다.
- libxposed API 101 메타데이터와 protective 예외 모드를 사용합니다.

## 빌드

이 프로젝트는 libxposed 작업공간의 공통 기준에 맞춰져 있습니다.

- Gradle `9.6.1`
- Android Gradle Plugin `9.3.0`
- libxposed API `101.0.1`
- Java `17`
- compileSdk `36`
- targetSdk `36`
- minSdk `26`

디버그 APK 빌드:

```sh
./gradlew assembleDebug
```

Lint 실행:

```sh
./gradlew lintDebug
```

## 설치

1. LSPosed가 설치된 루팅 Android 기기에 APK를 설치합니다.
2. LSPosed에서 `RemoveEmergencyCall` 모듈을 활성화합니다.
3. 권장 scope 앱인 `com.android.systemui`를 선택합니다.
4. 기기를 재부팅하거나 SystemUI를 재시작합니다.

## 참고

- 이 모듈은 개인 연구와 기기 커스터마이징 목적으로 작성되었습니다.
- SystemUI는 기기와 ROM에 따라 내부 구현이 다릅니다. ROM 업데이트로 내부 클래스 또는 메서드 이름이 바뀌면 훅 수정이 필요할 수 있습니다.

## 라이선스

이 프로젝트는 Apache License 2.0을 따릅니다. 자세한 내용은 [LICENSE](LICENSE)를 참고하세요.
