# MVP Scope — CreatorHire

## 1. Purpose

The MVP delivers a working end-to-end marketplace for hiring creative professionals: registration, job posting, job discovery, application, shortlisting, hiring, and project tracking — built with **Java (Spring Boot)** and backed by a relational database.

## 2. MVP Features

### Authentication & Authorization

- User registration (client, creative professional, admin)
- User login
- JWT-based authentication
- Role-based access control (Client / CREATOR / ADMIN)

### Client

- Manage own client profile
- Create, edit, and delete own job postings
- Specify required skills, budget range, and project deadline per job
- View applications for own jobs
- Shortlist, accept, or reject applicants
- Manage projects created from accepted applications

### Creative Professional

- Manage own professional profile and availability
- Manage own skills
- Manage own portfolio items
- Browse, search, and filter available jobs
- Apply for jobs and track application status
- Track own hired projects

### Admin

- Manage users
- Moderate job postings
- Review reports
- Suspend or deactivate accounts
- Monitor platform activity

## 3. Core Workflows (End-to-End)

1. **Registration & Login** — user registers, is assigned a role, and authenticates via JWT.
2. **Job Posting** — client creates a job with skills, budget, and deadline.
3. **Job Discovery** — creator searches and filters open jobs.
4. **Application** — creator submits an application for a job.
5. **Shortlisting** — client reviews and shortlists applicants.
6. **Hiring** — client accepts an application and a project is created.
7. **Tracking** — both parties track project status; notifications are generated at each workflow step.

## 4. Success Criteria

- A user should be able to register and log in successfully.
- A client should be able to create a job posting in under 3 minutes.
- A creative professional should be able to search and filter jobs.
- A creative professional should be able to submit an application.
- A client should be able to review and shortlist applicants.
- A client should be able to accept an application and create a project.
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
- Advanced AI matching (planned as a later enhancement)

## 6. Technical Approach

- **Backend**: Spring Boot (Java), Spring Security + JWT, Spring Data JPA
- **Database**: MySQL with the entity model defined in the [ER Diagram](er-diagram.md)
- **Deployment**: Public cloud (e.g., Render / Railway / AWS), containerized with Docker