# Personal Ambient Dashboard

A personal dashboard for a tablet or screen in the home that continuously shows useful information for the current moment.

## Concept

The app combines important daily information in one place instead of requiring the user to open several separate apps.

It may show:

- Current weather and forecast
- To-do tasks
- Calendar and schedule
- When to leave for work based on live travel data
- Reminders and things the user asked it to remember
- Context-aware suggestions and alerts

The dashboard should update in real time and prioritize what is relevant now.

## Example

> Leave in 15 minutes. Traffic is heavier than usual. Rain is expected this afternoon. You have two tasks due today.

## Target Experience

The primary display is a tablet mounted or placed somewhere in the home. The application should also work in a normal web browser.

## Base Technology

- Backend: Spring Boot with Java 25
- Frontend: Angular
- Database: PostgreSQL
- Real-time updates between backend and frontend

## Local development

Run the backend with `./mvnw spring-boot:run` from `backend/` and the frontend
with `npm start` from `frontend/`. The Angular development server proxies
`/api` requests to the backend on port 8080.

On a Sopra-managed macOS device, allow Java to use certificates from the
system keychain when starting the backend:

```sh
JAVA_TOOL_OPTIONS=-Djavax.net.ssl.trustStoreType=KeychainStore ./mvnw spring-boot:run
```
