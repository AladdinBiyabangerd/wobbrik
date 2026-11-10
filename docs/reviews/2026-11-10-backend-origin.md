# Backend origin — 10 November 2026

- Branch: `chore/backend-origin`, based on main `8ffc0f3`.
- Reviewed diff: project Gradle property and README; HTTPS origin has no path or trailing slash. Existing property override and transport validation remain intact.
- User supplied `appsbackend.up.railway.app`; configured `https://appsbackend.up.railway.app`.
- HTTPS GET `/readyz`: HTTP 200. Authenticated gameplay/device flow not tested.
- No API/DTO/schema/domain changes; AppBackend code changes unnecessary for URL configuration.
- Local CI: pending main merge. No push/deploy/Console changes.
