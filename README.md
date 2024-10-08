# BackEnd

## 코드 스타일 가이드
- 이 프로젝트는 **Google Java Style Guide**를 따릅니다.
- **대문자로 표기할 약어 명시**: 약어의 중간 단어를 소문자로 표기하고, 프로젝트 별로 모두 대문자로 표기할 약어의 목록을 명시합니다.
  - 예: `HttpApiUrl` (API만 대문자로 표기), `HTTPAPIURL` (모두 대문자 표기).
- **패키지 이름은 소문자로 구성**: 언더스코어(`_`)나 대문자를 사용하지 않고 소문자로 작성합니다.
  - 예: `package com.navercorp.apigateway` (Good), `package com.navercorp.apiGateway` (Bad).
- **클래스 및 인터페이스 이름**: 클래스와 인터페이스는 **대문자 카멜표기법**(Pascal case)을 사용합니다.
  - 예: `public class Reservation` (Good), `public class reservation` (Bad).
- **테스트 클래스는 'Test'로 끝남**: JUnit 등으로 작성한 테스트 클래스는 `'Test'`로 끝납니다.
  - 예: `public class WatcherTest`.
- **메서드 이름에 소문자 카멜표기법 적용**: 메서드는 첫 단어를 소문자로 시작하는 소문자 카멜표기법을 사용합니다.
  - 예: `renderHtml()`, `toString()`.
- **상수는 대문자와 언더스코어로 구성**: 상수는 대문자로 작성하고 단어는 언더스코어로 구분합니다.
  - 예: `public final int UNLIMITED = -1;`.
- **변수에 소문자 카멜표기법 적용**: 멤버 변수, 지역 변수, 메서드 파라미터에는 소문자 카멜표기법을 사용합니다.
  - 예: `private boolean authorized;`
- **static import에만 와일드 카드 허용**: 일반 import에서는 와일드카드(`*`)를 사용하지 않으며, static import에서만 허용합니다.
- **제한자 선언의 순서**: 제한자는 Java Language Specification에 명시된 순서로 작성합니다.
  - 예: `public static final`.
- **배열에서 대괄호는 타입 뒤에 선언**: 배열 선언 시 대괄호(`[]`)는 타입 뒤에 붙입니다.
  - 예: `String[] names` (Good), `String names[]` (Bad).
- **중괄호는 K&R 스타일로 선언**: 클래스, 메서드, 조건문 등의 블록을 감쌉니다. 열고 닫는 중괄호의 위치에 주의합니다.
  - 예: `if (condition) { ... } else { ... }`
