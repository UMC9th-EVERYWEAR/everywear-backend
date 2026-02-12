# EveryWear Backend

EveryWear Backend는 외부 쇼핑몰 상품을 연동 및 관리하고, AI 가상 피팅과 리뷰 분석을 위한 데이터를 제공하는 **Spring Boot 기반 REST API 서버**입니다.

## 👥 Team
---
| BE(팀장) | BE | BE | BE |
| :---: | :---: | :---: | :---: |
| <img src="https://github.com/junjunseo.png" width="150" height="150"/> | <img src="https://github.com/2ivii.png" width="150" height="150"/> | <img src="https://github.com/whiteys1.png" width="150" height="150"/> | <img src="https://github.com/taerimiiii.png" width="150" height="150"/> |
| 임준서<br/><a href="https://github.com/junjunseo">@junjunseo</a> | 윤정민<br/><a href="https://github.com/2ivii">@2ivii</a> | 신영섭<br/><a href="https://github.com/whiteys1">@whiteys</a> | 김태림<br/><a href="https://github.com/taerimiiii">@taerimiiii</a> |

## 💻 Tech Stack
---
- **Framework/Language**: Spring Boot 3.x, Java 21
- **Build/Database**: Gradle, MySQL, Spring Data JPA
- **AI & Security**: Gemini API (Gemini 2.5 Flash, gemini-3-pro-preview, gemini-3-pro-image-preview), OpenAI API (GPT-4o), JWT 기반 소셜 로그인
- **Docs**: Swagger (SpringDoc)

## **📂 Project Structure**
---
도메인형 (Domain-driven)
```
everywear-backend/
├── .github/                       # Issue/PR 템플릿 및 CI/CD 설정
├── src/main/java/com/umc/EveryWear/
│   ├── EveryWearApplication.java
│   ├── global/                    # 전역 공통 모듈
│   │   ├── apiPayload/            # 공통 API 응답 형식
│   │   ├── config/                # Security, Swagger, WebClient, S3, Gemini 등 설정
│   │   ├── controller/            # 헬스체크 등 전역 컨트롤러
│   │   ├── entity/                # 공통 엔티티
│   │   ├── exception/             # 공통 예외
│   │   ├── handler/               # 공통 핸들러
│   │   └── security/              # JWT, OAuth2 인증/인가
│   │
│   └── domain/                    # 도메인별 패키지
│       ├── fitting/               # 코디/피팅 (AI 이미지 분석·추천)
│       │   ├── controller/
│       │   ├── converter/
│       │   ├── dto/
│       │   ├── entity/
│       │   ├── enums/
│       │   ├── repository/
│       │   ├── service/
│       │   └── exception/
│       ├── auth/                  # 인증 (로그인/로그아웃/토큰 갱신)
│       ├── user/                  # 사용자 및 프로필
│       ├── product/               # 상품 (쇼핑몰 크롤링-등록-조회)
│       ├── closet/                # 옷장 (카테고리별 상품 조회)
│       ├── home/                  # 홈
│       ├── review/                # 리뷰 (AI 리뷰/키워드)
│       └── alarm/                 # 알림
│ 
├── gradle/                        # Gradle Wrapper
├── build.gradle
├── settings.gradle
└── dockerfile
```

## **🛠️ Architecture**
---
<img width="1005" height="541" alt="스크린샷 2026-02-12 오후 7 13 10" src="https://github.com/user-attachments/assets/37592b38-da0f-4187-9cc1-c7bd93f18fc6" />


## **📝 Commit Convention**
---
| type | 의미 | 예시 |
| --- | --- | --- |
| ✨ **feat** | 새로운 기능 | 로그인 API 구현 |
| 🐞 **fix** | 버그 수정 | NPE 해결 |
| 📝 **docs** | 문서 수정 | README 업데이트 |
| ⚙️ **setting** | 프로젝트/환경 설정 | yml, CI |
| **♻️ refactor** | 기능 변화 없는 코드 리팩터링 | Service 분리 |
| 🎨 **style** | 포맷/세미콜론/네이밍 등 | 포맷팅, 공백 |
| 🧪 **test** | 테스트 코드 | Controller 단위 테스트 |
| 🧹 **chore** | 패키지 관리, 기타잡무 | Gradle 설정 변경 |
