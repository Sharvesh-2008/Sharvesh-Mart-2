# Changelog

All notable changes to SHARVESHMART. One line per release (spec Section 15.5).

## [Unreleased]

## [0.1.0] - 2026-08-02
- Week 1: project skeleton (Maven + Tomcat 9 + HikariCP + H2), DB schema v1 + seed
- Authentication: register / login / logout with session regeneration and timeout
- Front Controller (DispatcherServlet), AuthFilter, EncodingFilter, RequestIdFilter
- Base DAO layer (UserDao) with PreparedStatement-only SQL
- Tests: service (Mockito), DAO (embedded H2), util; Checkstyle + SpotBugs in CI
- `GET /api/v1/health` health endpoint
