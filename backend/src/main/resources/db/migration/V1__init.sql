-- CreatorHire initial schema (PostgreSQL)
-- Matches JPA entity mappings exactly

CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
    email_verified BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL
);

CREATE TABLE roles (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(32) NOT NULL UNIQUE
);

CREATE TABLE user_roles (
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role_id BIGINT NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);

CREATE TABLE client_profiles (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    company_name VARCHAR(255),
    bio TEXT,
    industry VARCHAR(100),
    created_at TIMESTAMP NOT NULL
);

CREATE TABLE creator_profiles (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    headline VARCHAR(255),
    bio TEXT,
    experience_years INTEGER,
    availability VARCHAR(32) NOT NULL DEFAULT 'AVAILABLE',
    hourly_rate NUMERIC(12,2),
    on_time_delivery_rate NUMERIC(5,2) NOT NULL DEFAULT 0,
    avg_response_time_hours INTEGER NOT NULL DEFAULT 0,
    completed_projects INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL
);

CREATE TABLE skills (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    category VARCHAR(32) NOT NULL
);

CREATE TABLE creator_skills (
    id BIGSERIAL PRIMARY KEY,
    creator_profile_id BIGINT NOT NULL REFERENCES creator_profiles(id) ON DELETE CASCADE,
    skill_id BIGINT NOT NULL REFERENCES skills(id) ON DELETE CASCADE,
    level VARCHAR(32) NOT NULL,
    UNIQUE (creator_profile_id, skill_id)
);

CREATE TABLE portfolios (
    id BIGSERIAL PRIMARY KEY,
    creator_profile_id BIGINT NOT NULL REFERENCES creator_profiles(id) ON DELETE CASCADE,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    media_url VARCHAR(1024),
    work_type VARCHAR(32),
    verification_status VARCHAR(32) NOT NULL DEFAULT 'UNVERIFIED',
    collaboration_role VARCHAR(255),
    outcome_stats VARCHAR(255),
    created_at TIMESTAMP NOT NULL
);

CREATE TABLE portfolio_skills (
    id BIGSERIAL PRIMARY KEY,
    portfolio_id BIGINT NOT NULL REFERENCES portfolios(id) ON DELETE CASCADE,
    skill_id BIGINT NOT NULL REFERENCES skills(id) ON DELETE CASCADE,
    UNIQUE (portfolio_id, skill_id)
);

CREATE TABLE jobs (
    id BIGSERIAL PRIMARY KEY,
    client_profile_id BIGINT NOT NULL REFERENCES client_profiles(id) ON DELETE CASCADE,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    creative_brief TEXT NOT NULL,
    style_keywords VARCHAR(512),
    reference_links TEXT,
    budget_min NUMERIC(12,2),
    budget_max NUMERIC(12,2),
    deadline DATE,
    status VARCHAR(32) NOT NULL DEFAULT 'OPEN',
    created_at TIMESTAMP NOT NULL
);

CREATE TABLE job_skills (
    id BIGSERIAL PRIMARY KEY,
    job_id BIGINT NOT NULL REFERENCES jobs(id) ON DELETE CASCADE,
    skill_id BIGINT NOT NULL REFERENCES skills(id) ON DELETE CASCADE,
    UNIQUE (job_id, skill_id)
);

CREATE TABLE applications (
    id BIGSERIAL PRIMARY KEY,
    job_id BIGINT NOT NULL REFERENCES jobs(id) ON DELETE CASCADE,
    creator_profile_id BIGINT NOT NULL REFERENCES creator_profiles(id) ON DELETE CASCADE,
    cover_letter TEXT,
    brief_response TEXT,
    proposed_rate NUMERIC(12,2),
    estimated_days INTEGER,
    match_score NUMERIC(5,2) NOT NULL DEFAULT 0,
    response_time_hours INTEGER NOT NULL DEFAULT 0,
    status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
    applied_at TIMESTAMP NOT NULL,
    UNIQUE (job_id, creator_profile_id)
);

CREATE TABLE application_samples (
    id BIGSERIAL PRIMARY KEY,
    application_id BIGINT NOT NULL REFERENCES applications(id) ON DELETE CASCADE,
    portfolio_id BIGINT NOT NULL REFERENCES portfolios(id) ON DELETE CASCADE,
    UNIQUE (application_id, portfolio_id)
);

CREATE TABLE projects (
    id BIGSERIAL PRIMARY KEY,
    job_id BIGINT NOT NULL REFERENCES jobs(id) ON DELETE CASCADE,
    application_id BIGINT NOT NULL UNIQUE REFERENCES applications(id) ON DELETE CASCADE,
    title VARCHAR(255) NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'NOT_STARTED',
    start_date DATE,
    end_date DATE,
    deadline DATE,
    completed_on DATE
);

CREATE TABLE notifications (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    message VARCHAR(1024) NOT NULL,
    type VARCHAR(64) NOT NULL,
    read BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL
);

CREATE TABLE reports (
    id BIGSERIAL PRIMARY KEY,
    reporter_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    reported_user_id BIGINT REFERENCES users(id) ON DELETE SET NULL,
    job_id BIGINT REFERENCES jobs(id) ON DELETE SET NULL,
    reason VARCHAR(255) NOT NULL,
    description TEXT,
    status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP NOT NULL
);

CREATE TABLE email_otps (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    otp_hash VARCHAR(64) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    attempts INTEGER NOT NULL DEFAULT 0,
    used BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_user_roles_user_id ON user_roles(user_id);
CREATE INDEX idx_user_roles_role_id ON user_roles(role_id);
CREATE INDEX idx_client_profiles_user_id ON client_profiles(user_id);
CREATE INDEX idx_creator_profiles_user_id ON creator_profiles(user_id);
CREATE INDEX idx_creator_skills_creator_profile_id ON creator_skills(creator_profile_id);
CREATE INDEX idx_creator_skills_skill_id ON creator_skills(skill_id);
CREATE INDEX idx_portfolios_creator_profile_id ON portfolios(creator_profile_id);
CREATE INDEX idx_portfolio_skills_portfolio_id ON portfolio_skills(portfolio_id);
CREATE INDEX idx_portfolio_skills_skill_id ON portfolio_skills(skill_id);
CREATE INDEX idx_jobs_client_profile_id ON jobs(client_profile_id);
CREATE INDEX idx_job_skills_job_id ON job_skills(job_id);
CREATE INDEX idx_job_skills_skill_id ON job_skills(skill_id);
CREATE INDEX idx_applications_job_id ON applications(job_id);
CREATE INDEX idx_applications_creator_profile_id ON applications(creator_profile_id);
CREATE INDEX idx_application_samples_application_id ON application_samples(application_id);
CREATE INDEX idx_application_samples_portfolio_id ON application_samples(portfolio_id);
CREATE INDEX idx_projects_job_id ON projects(job_id);
CREATE INDEX idx_projects_application_id ON projects(application_id);
CREATE INDEX idx_notifications_user_id ON notifications(user_id);
CREATE INDEX idx_reports_reporter_id ON reports(reporter_id);
CREATE INDEX idx_reports_reported_user_id ON reports(reported_user_id);
CREATE INDEX idx_reports_job_id ON reports(job_id);
CREATE INDEX idx_email_otps_user_id ON email_otps(user_id);
