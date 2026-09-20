# Retrospective (RETRO.md)

One line per sprint: what worked, what didn't, one change for the next sprint
(spec Section 16.2).

## Sprint 1 (Week 1, Jul 27 – Aug 2)
- What worked: skeleton + auth built and smoke-tested locally with portable Maven/Tomcat.
- What didn't: winget had no Maven package; fell back to a portable download into `tools/`.
- Next change: pin the Tomcat/H2 setup into a repeatable run script.
