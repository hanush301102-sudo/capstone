export interface AuthUser {
  email: string;
  firstName: string;
  lastName: string;
  roles: string[];
}

export interface AuthResponse {
  token: string;
  email: string;
  roles: string[];
}

export interface Job {
  id: number;
  title: string;
  description?: string;
  creativeBrief: string;
  styleKeywords?: string;
  referenceLinks?: string;
  budgetMin?: number;
  budgetMax?: number;
  deadline?: string;
  status: string;
  createdAt: string;
  clientProfileId: number;
  companyName?: string;
  skills: string[];
  applicationCount: number;
}

export interface CreatorCard {
  id: number;
  headline?: string;
  bio?: string;
  experienceYears?: number;
  availability: string;
  hourlyRate?: number;
  onTimeDeliveryRate: number;
  avgResponseTimeHours: number;
  completedProjects: number;
  skills: string[];
}

export interface Application {
  id: number;
  jobId: number;
  jobTitle: string;
  creatorProfileId: number;
  creatorHeadline?: string;
  coverLetter?: string;
  briefResponse?: string;
  proposedRate?: number;
  estimatedDays?: number;
  matchScore: number;
  responseTimeHours: number;
  status: string;
  appliedAt: string;
  samplePortfolioIds: number[];
}

export interface Project {
  id: number;
  jobId: number;
  jobTitle: string;
  applicationId: number;
  title: string;
  status: string;
  startDate?: string;
  endDate?: string;
  deadline?: string;
  completedOn?: string;
}

export interface PortfolioItem {
  id: number;
  creatorProfileId: number;
  title: string;
  description?: string;
  mediaUrl?: string;
  workType?: string;
  verificationStatus: string;
  collaborationRole?: string;
  outcomeStats?: string;
  createdAt: string;
  skills: string[];
}

export interface NotificationItem {
  id: number;
  message: string;
  type: string;
  read: boolean;
  createdAt: string;
}

export interface Skill {
  id: number;
  name: string;
  category: string;
}
