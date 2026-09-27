# MyTasteZip

2025년 5월 **모바일컴퓨팅** 전공 과목 프로젝트로 만든 Android 앱입니다. 나만의 맛집을 저장하고 관리할 수 있습니다.

## 주요 기능
- **홈**: 카카오맵 기반 지도 및 현재 위치 표시
- **맛집 추가**: 카카오 로컬 API로 장소 검색 후 카테고리, 메모, 링크와 함께 저장
- **저장 목록**: Room DB에 저장한 맛집 목록 조회
- **커뮤니티**: 게시글 작성 및 조회

## 기술 스택
- Java, Android SDK (minSdk 27 / targetSdk 35)
- Room, ViewModel/LiveData, Navigation, ViewBinding
- Retrofit / OkHttp, Kakao Maps SDK, Google Play Services Location

## 실행 방법
`local.properties`에 카카오 개발자 키를 추가한 뒤 빌드합니다.

```properties
KAKAO_REST_API_KEY=발급받은_REST_API_키
KAKAO_NATIVE_APP_KEY=발급받은_네이티브_앱_키
```
