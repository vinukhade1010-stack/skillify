# Skillify: AI Tutor

Skillify is an AI tutor for college students. Students sign up with their degree details, take AI-written quizzes on topics matched to their course, and get a personal learning roadmap with embedded videos for every skill gap.

## Features
- Sign up with education details (college, degree, branch, year, goals)
- Topics chosen by degree and branch
- AI-generated quizzes with timer, streaks and explanations
- AI roadmap based on quiz score, plus "what to learn next"
- Embedded YouTube videos for each roadmap stage
- Ask Skilly chat, score history chart, dark mode
- Responsive for portrait and landscape

## Tech stack
- Frontend: HTML, CSS, JavaScript, GSAP
- Backend: Java 17, Spring Boot 3, Spring Data JPA, Maven
- Database: H2 (default) or MySQL
- APIs: Google Gemini API (AI), YouTube Data API v3 (videos)

## Run it
1. Install JDK 17 or newer.
2. Get a Google API key from Google AI Studio and set it as an environment variable:
   - Windows PowerShell: `$env:GOOGLE_API_KEY="your-key"`
   - Mac/Linux: `export GOOGLE_API_KEY=your-key`
   - Eclipse: Run Configurations > Environment tab
3. Optional: set `YOUTUBE_API_KEY` for a separate YouTube key. Otherwise the Google key is used, and its project needs YouTube Data API v3 enabled.
4. Start the app: `mvn spring-boot:run` (or run `SkillifyApplication` in Eclipse).
5. Open http://localhost:5000

## Notes
- Never commit API keys. Keys are read from environment variables only.
- To use MySQL, edit `src/main/resources/application.properties`.
- The AI model name is set in `application.properties` (`ai.model`).

## Project structure
```
src/main/java/com/skillify/     Spring Boot backend (controllers, services, entities)
src/main/resources/static/      Frontend pages, CSS and JS
src/main/resources/application.properties
pom.xml
```
