# Problem Statement

## 1. Title

CreatorHire — Creator Marketplace for Hiring Video Editors, Designers, and Scriptwriters

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

Clients may need to manually search through many professionals while evaluating their skills, portfolio, experience, availability, and expected budget separately.

Creative professionals also struggle to discover relevant opportunities that match their skills, experience, availability, and preferred type of work.

CreatorHire provides a structured marketplace that connects clients with creative professionals and manages the workflow from job posting and application to candidate selection and project creation.

## 5. Proposed Solution

CreatorHire will provide the following features.

### Authentication

- User registration
- User login
- JWT-based authentication
- Role-based authorization

### Client Features

- Create and manage client profile
- Create creative job postings
- Specify required skills
- Specify budget range
- Specify project deadline
- View applications
- Shortlist applicants
- Accept or reject applications
- Manage hired projects

### Creative Professional Features

- Create and manage professional profile
- Add skills
- Add portfolio items
- Set availability
- Browse available jobs
- Search and filter jobs
- Apply for jobs
- Track application status
- Track hired projects

### Admin Features

- Manage users
- Moderate job postings
- Review reports
- Suspend or deactivate accounts
- Monitor platform activity

### Platform Features

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
8. Job
9. JobSkill
10. Application
11. Project
12. Notification
13. Report

## 7. User Roles & Permissions

### Client

Can:

- Manage own profile
- Create jobs
- Edit and delete own jobs
- View applications for own jobs
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
- Manage own portfolio
- Browse jobs
- Search and filter jobs
- Apply for jobs
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
- Review reports
- Suspend or deactivate accounts
- Monitor platform activity

## 8. Success Criteria

- A user should be able to register and log in successfully.
- A client should be able to create a job posting in under 3 minutes.
- A creative professional should be able to search and filter jobs.
- A creative professional should be able to submit an application.
- A client should be able to review and shortlist applicants.
- A client should be able to accept an application and create a project.
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
- Advanced AI matching in the initial MVP

AI-based creator-job matching will be considered as the enhancement feature for the later phase.

## 10. Chosen Track

Java — Spring Boot