export type UserRole = 'ADMIN' | 'TEACHER' | 'PARENT' | 'LEARNER';

export interface UserSummary {
  id: string;
  fullName: string;
  email: string;
  role: UserRole;
}

export interface DemoCredential {
  label: string;
  email: string;
  password: string;
  role: UserRole;
}

export interface LoginResponse {
  token: string;
  user: UserSummary;
  demoCredentials: DemoCredential[];
}

export interface SchoolSummary {
  id: string;
  name: string;
  district: string;
}

export interface StatCard {
  label: string;
  value: string;
  caption: string;
}

export interface ParentLink {
  learnerId: string;
  learnerName: string;
  parent: UserSummary;
}

export interface ClassView {
  id: string;
  name: string;
  gradeLabel: string;
  subject: string;
  teacher: UserSummary;
  learners: UserSummary[];
  parentLinks: ParentLink[];
}

export interface FileAttachmentView {
  fileName: string;
  contentType: string;
  size: number;
}

export interface FileUploadPayload {
  fileName: string;
  contentType: string;
  size: number;
  base64Data: string;
}

export interface SubmissionView {
  id: string;
  learner: UserSummary;
  content: string;
  submittedAt: string;
  score: number | null;
  feedback: string | null;
  status: string;
  attachment: FileAttachmentView | null;
}

export interface AssignmentView {
  id: string;
  classId: string;
  className: string;
  title: string;
  description: string;
  dueDate: string;
  publishedAt: string;
  status: string;
  attachment: FileAttachmentView | null;
  submissions: SubmissionView[];
}

export interface AttendanceEntryView {
  learner: UserSummary;
  status: 'PRESENT' | 'ABSENT' | 'LATE';
}

export interface AttendanceView {
  id: string;
  classId: string;
  className: string;
  date: string;
  entries: AttendanceEntryView[];
}

export interface AnnouncementView {
  id: string;
  classId: string;
  className: string;
  title: string;
  body: string;
  sentAt: string;
  teacher: UserSummary;
}

export interface MessageView {
  id: string;
  classId: string;
  className: string;
  subject: string;
  body: string;
  sentAt: string;
  teacher: UserSummary;
  parent: UserSummary;
}

export interface NotificationView {
  id: string;
  type: string;
  title: string;
  body: string;
  createdAt: string;
  readAt: string | null;
  read: boolean;
}

export interface DashboardResponse {
  currentUser: UserSummary;
  school: SchoolSummary;
  stats: StatCard[];
  classes: ClassView[];
  assignments: AssignmentView[];
  attendance: AttendanceView[];
  announcements: AnnouncementView[];
  messages: MessageView[];
  notifications: NotificationView[];
  demoCredentials: DemoCredential[];
}
