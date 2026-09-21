# Problem Statement

## 1. Title

CreatorHire — Creator Marketplace for Hiring Video Editors, Designers, and Scriptwriters

> **Tagline:** Stop fishing through generic applications. Hire creators whose style, skills, and reliability are proven.

## 2. Domain

Creator Economy / Creative Services / Freelancing

## 3. Who is the user?

### Client

Creators, startups, agencies, and small businesses that need to hire video editors, designers, or scriptwriters for specific projects.

### Creative Professional

Video editors, designers, and scriptwriters who want to showcase their skills and portfolio, discover relevant jobs, and apply for creative projects.

### Admin

The platform administrator responsible for managing users, moderating jobs and content, and handling reports.

## 4. What problem are we solving?

Creators, startups, agencies, and small businesses often need creative professionals for specific projects but struggle to identify suitable candidates efficiently.

Generic hiring platforms (general-purpose freelance marketplaces) leave four real-world pain points unsolved:

1. **Briefing mismatch** — clients don't know how to brief creative work. Vague job posts ("need a cool edit") lead to deliverables in the wrong style, rework, and disputes.
2. **Spam applications** — open job posts attract hundreds of generic copy-paste applications, forcing clients to screen manually for hours.
3. **Unverifiable portfolios** — anyone can upload images or links and claim the work as their own. Clients cannot tell what a creator actually did versus what they merely show.
4. **Unreliable creators** — missed deadlines and slow responses only surface after the hire. There is no portable record of a creator's delivery reliability.

Creative professionals face the mirror problem: they struggle to discover relevant opportunities that match their skills, experience, availability, and preferred type of work, and their genuine expertise is buried under unverified competition.

## 5. Proposed Solution

CreatorHire is a **style-first, verified, match-scored** marketplace. Unlike generic hiring applications, every hiring decision on CreatorHire is backed by evidence: a structured creative brief, an auto-computed match score, verified portfolio work, side-by-side applicant comparison, and a reliability record earned from completed projects.

### Authentication

- User registration
- User login
- JWT-based authentication
- Role-based authorization

### Client Features

- Create and manage client profile
- Create creative job postings with a **structured creative brief** (style keywords, reference links, must-have samples)
- Specify required skills
- Specify budget range
- Specify project deadline
- **Discover creators by skill and availability (Creator Radar)** before or without posting
- View applications **ranked by match score**
- **Compare shortlisted applicants side-by-side**
- Shortlist applicants
- Accept or reject applications
- Manage hired projects

### Creative Professional Features

- Create and manage professional profile
- Add skills
- Add portfolio items **tagged with skills, work type, and collaboration role (verified portfolio)**
- Set availability
- Browse available jobs
- Search and filter jobs
- Apply for jobs with a **brief response and relevant portfolio samples**
- Track application status (including match score feedback)
- Track hired projects
- Build a **reliability record** (on-time delivery rate, response time) from completed projects

### Admin Features

- Manage users
- Moderate job postings
- Review reports
- Verify portfolio items
- Suspend or deactivate accounts
- Monitor platform activity

### Platform Features

- Structured creative briefs on every job
- **Match-score computation on every application** (skill overlap, budget fit, availability, deadline fit, reliability)
- Verified, skill-tagged portfolio
- Applicant comparison view
- Reliability metrics derived from completed projects
- Creator discovery by availability and skill
- Job search and filtering
- Application management
- Candidate shortlisting
- Hiring workflow
- Project status tracking
- Notifications
- Role-based access control
- Audit/activity records

## 6. Core Entities / Database Tables

1. User
2. Role
3. ClientProfile
4. CreatorProfile
5. Skill
6. CreatorSkill
7. Portfolio
8. PortfolioSkill
9. Job
10. JobSkill
11. Application
12. ApplicationSample
13. Project
14. Notification
15. Report

## 7. User Roles & Permissions

### Client

Can:

- Manage own profile
- Create jobs (with creative brief)
- Edit and delete own jobs
- Browse creators by skill and availability
- View applications for own jobs, ranked by match score
- Compare shortlisted applicants
- Shortlist applicants
- Accept or reject applications
- Manage projects created from accepted applications

Cannot:

- Modify another client's jobs
- Modify another user's profile
- Access unauthorized private information

### Creative Professional

Can:

- Manage own profile
- Manage own skills
- Manage own portfolio (tagged, verification requested)
- Browse jobs
- Search and filter jobs
- Apply for jobs with brief response and samples
- Track own applications
- Track own projects

Cannot:

- Modify another creator's profile
- Modify another creator's applications
- Access unauthorized client information

### Admin

Can:

- Manage users
- Moderate jobs
- Verify portfolio items
- Review reports
- Suspend or deactivate accounts
- Monitor platform activity

## 8. Success Criteria

- A user should be able to register and log in successfully.
- A client should be able to create a job posting **with a creative brief** in under 3 minutes.
- A client should be able to discover creators by skill and availability without posting a job.
- A creative professional should be able to search and filter jobs.
- A creative professional should be able to submit an application **with a brief response and portfolio samples**.
- Every application should receive an **auto-computed match score** visible to the client.
- A client should be able to review, **compare side-by-side**, and shortlist applicants.
- A client should be able to accept an application and create a project.
- A creator's profile should display a **reliability record** derived from completed projects.
- Users should only access resources permitted by their role.
- Core workflows should work end-to-end from frontend to backend to database and back to the frontend.
- The application should be deployable on a public cloud URL.

## 9. Out of Scope

The initial product will not include:

- Real-money escrow payments
- Complex financial transactions
- Video calling
- Real-time chat
- Advanced contract/legal management
- Payroll management
- Advanced dispute resolution
- Native mobile applications
- Large-scale recommendation infrastructure
- ML-based matching (the MVP uses transparent rule-based match scoring instead)

ML-based creator-job matching will be considered as the enhancement feature for the later phase.

## 10. Chosen Track

Java — Spring Boot (backend), React + JavaScript/TypeScript (frontend)
