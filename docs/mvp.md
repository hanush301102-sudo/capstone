# MVP Scope — CreatorHire

> **Positioning:** Style-first, verified, match-scored hiring. Stop fishing through generic applications — hire creators whose style, skills, and reliability are proven.

## 1. Purpose

The MVP delivers a working end-to-end marketplace for hiring creative professionals: registration, brief-based job posting, creator discovery, match-scored applications, comparison-based shortlisting, hiring, project tracking, and reliability records — built with **Java (Spring Boot)** backend and **React** frontend, backed by a relational database.

The MVP's 5 differentiators against generic hiring applications:

1. **Creative Briefs** — structured briefs kill vague posts and generic applications.
2. **Match Score** — transparent rule-based scoring ranks every application.
3. **Verified Portfolio + Comparison** — skill-tagged portfolio with verification status, plus side-by-side shortlist comparison.
4. **Reliability Record** — on-time delivery rate and response time earned from completed projects.
5. **Creator Radar** — inverse discovery: find creators by skill + availability before posting.

## 2. MVP Features

### Authentication & Authorization

- User registration (client, creative professional, admin)
- User login
- JWT-based authentication
- Role-based access control (CLIENT / CREATOR / ADMIN)

### Client

- Manage own client profile
- Create, edit, and delete own job postings, each with a **creative brief** (style keywords, reference links, must-have sample notes)
- Specify required skills, budget range, and project deadline per job
- **Discover creators by skill and availability (Creator Radar)**
- View applications for own jobs **ranked by match score**
- **Compare shortlisted applicants side-by-side** (score, rate, samples, reliability)
- Shortlist, accept, or reject applicants
- Manage projects created from accepted applications

### Creative Professional

- Manage own professional profile and availability
- Manage own skills
- Manage own portfolio items **tagged with skills, work type, and collaboration role**; request verification
- Browse, search, and filter available jobs
- Apply for jobs with a **brief response, proposed rate, estimated timeline, and selected portfolio samples**
- Track own applications (including match score)
- Track own hired projects
- Accumulate a **reliability record** (on-time delivery rate, average response time, completed projects)

### Admin

- Manage users
- Moderate job postings
- **Verify portfolio items**
- Review reports
- Suspend or deactivate accounts
- Monitor platform activity

## 3. Core Workflows (End-to-End)

1. **Registration & Login** — user registers, is assigned a role, and authenticates via JWT.
2. **Job Posting with Brief** — client creates a job with skills, budget, deadline **plus a creative brief** (style keywords, reference links).
3. **Creator Radar (inverse discovery)** — client browses/filter creators by skill and availability without posting.
4. **Job Discovery** — creator searches and filters open jobs.
5. **Brief-based Application** — creator submits cover letter **plus brief response, proposed rate, estimated days, and portfolio samples**; backend computes a **match score** (skill overlap, budget fit, availability, deadline fit, reliability).
6. **Ranked Shortlisting & Comparison** — client reviews applications sorted by match score and compares shortlisted creators side-by-side.
7. **Hiring** — client accepts an application and a project is created.
8. **Tracking & Reliability** — both parties track project status; completion (on-time or late) updates the creator's reliability record; notifications are generated at each workflow step.

## 4. Success Criteria

- A user should be able to register and log in successfully.
- A client should be able to create a job posting with a creative brief in under 3 minutes.
- A client should be able to discover creators by skill and availability.
- A creative professional should be able to search and filter jobs.
- A creative professional should be able to submit an application with a brief response and samples.
- Every application should receive an auto-computed match score.
- A client should be able to compare, shortlist, and accept/reject applicants.
- A client should be able to accept an application and create a project.
- Reliability metrics should update when projects complete.
- Users should only access resources permitted by their role.
- Core workflows should work end-to-end from frontend to backend to database and back to the frontend.
- The application should be deployable on a public cloud URL.

## 5. Out of Scope for MVP

- Real-money escrow payments and financial transactions
- Video calling and real-time chat
- Advanced contract/legal management and payroll
- Dispute resolution
- Native mobile applications
- Large-scale recommendation infrastructure
- ML-based matching (rule-based match scoring only in MVP)

## 6. Technical Approach

- **Backend**: Spring Boot (Java 21), Spring Security + JWT, Spring Data JPA
- **Matching**: rule-based `MatchScoringService` (transparent, weighted score — no ML)
- **Database**: MySQL with the entity model defined in the [ER Diagram](../er-diagram.md)
- **Frontend**: React + Vite + TypeScript, parallax marketing landing, card-based creator/job discovery (see UI/UX reference: freelancer-card marketplace pattern)
- **Deployment**: Public cloud (Railway), containerized with Docker
