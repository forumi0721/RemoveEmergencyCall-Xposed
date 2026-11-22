# RemoveEmergencyCall

SystemUI 잠금 화면의 긴급 통화 버튼을 비활성화하는 libxposed 모듈입니다.

## 대상 앱

- `com.android.systemui`

대상 패키지는 libxposed 정적 scope 지원을 위해 `app/src/main/resources/META-INF/xposed/scope.list`에도 선언되어 있습니다.

## 동작

- 긴급 통화 버튼 뷰의 생성 및 화면 구성 시점에 버튼을 숨기고 비활성화합니다.
- 대상 ROM에서 확인 가능한 뷰 클래스에 적용합니다.

## 요구 사항

- Android 13 (API 33) 이상
- LSPosed 및 모듈과 호환되는 Android 환경

## 설치 및 적용

1. LSPosed가 설치된 루팅 Android 기기에 APK를 설치합니다.
2. LSPosed에서 모듈을 활성화하고 `com.android.systemui`를 scope로 선택합니다.
3. 기기를 재부팅하거나 SystemUI를 재시작합니다.

## 빌드

로컬 디버그 APK는 다음 명령으로 빌드합니다.

```sh
./build.sh debug
```

Lint는 다음 명령으로 실행합니다.

```sh
./gradlew lintDebug
```

## 참고

- 앱 또는 Android ROM 업데이트로 내부 클래스·메서드가 바뀌면 모듈 동작이 달라질 수 있습니다.
- 이 모듈은 개인 연구 및 호환성 조정 목적으로 제공됩니다. 적용 권한이 있는 앱과 기기에서 사용하세요.

## 라이선스

Apache License 2.0을 적용합니다. 자세한 내용은 [LICENSE](LICENSE)를 참고하세요.
