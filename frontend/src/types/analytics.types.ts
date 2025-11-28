export interface TasksByStatus {
  status: string;
  color: string;
  count: number;
}

export interface TasksByPriority {
  priority: string;
  color: string;
  count: number;
}

export interface RecentActivity {
  taskId: string;
  taskKey: string;
  taskTitle: string;
  activityType: string;
  description: string;
  userName: string;
  timestamp: string;
}

export interface UpcomingDeadline {
  taskId: string;
  taskKey: string;
  taskTitle: string;
  projectName: string;
  priority: string | null;
  priorityColor: string | null;
  dueDate: string;
  daysRemaining: number;
}

export interface DashboardOverview {
  totalProjects: number;
  activeProjects: number;
  totalTasks: number;
  completedTasks: number;
  overdueTasks: number;
  tasksInProgress: number;
  totalTimeSpentMinutes: number;
  completionRate: number;
  tasksByStatus: TasksByStatus[];
  tasksByPriority: TasksByPriority[];
  recentActivity: RecentActivity[];
  upcomingDeadlines: UpcomingDeadline[];
}

export interface TasksByAssignee {
  userId: string;
  userName: string;
  avatarUrl: string | null;
  totalTasks: number;
  completedTasks: number;
  inProgressTasks: number;
}

export interface TimeByDay {
  date: string;
  minutesSpent: number;
  tasksCompleted: number;
}

export interface ProjectAnalytics {
  projectId: string;
  projectName: string;
  projectKey: string;
  totalTasks: number;
  completedTasks: number;
  inProgressTasks: number;
  todoTasks: number;
  overdueTasks: number;
  completionRate: number;
  estimatedHours: number;
  spentHours: number;
  remainingHours: number;
  totalTimeSpentMinutes: number;
  averageCompletionTimeMinutes: number;
  tasksByStatus: TasksByStatus[];
  tasksByPriority: TasksByPriority[];
  tasksByAssignee: TasksByAssignee[];
  timeByDay: TimeByDay[];
}

export interface BurndownDataPoint {
  date: string;
  remainingTasks: number;
  completedTasks: number;
}

export interface BurndownChart {
  startDate: string;
  endDate: string;
  totalTasks: number;
  completedTasks: number;
  dataPoints: BurndownDataPoint[];
  idealLine: BurndownDataPoint[];
}

export interface VelocityDataPoint {
  period: string;
  startDate: string;
  endDate: string;
  tasksCompleted: number;
  hoursSpent: number;
}

export interface VelocityChart {
  averageVelocity: number;
  totalTasksCompleted: number;
  totalHoursSpent: number;
  dataPoints: VelocityDataPoint[];
}

// Admin Dashboard Types
export interface ProjectStats {
  projectId: string;
  projectName: string;
  projectKey: string;
  taskCount: number;
  completedTaskCount: number;
  memberCount: number;
}

export interface UserStats {
  userId: string;
  userName: string;
  avatarUrl: string | null;
  completedTasks: number;
  totalTasks: number;
  timeTrackedMinutes: number;
}

export interface AdminDashboard {
  totalUsers: number;
  activeUsers: number;
  newUsersThisMonth: number;
  totalProjects: number;
  activeProjects: number;
  archivedProjects: number;
  totalTasks: number;
  completedTasks: number;
  overdueTasks: number;
  tasksCreatedThisMonth: number;
  totalTeams: number;
  totalTimeTrackedMinutes: number;
  timeTrackedThisMonthMinutes: number;
  recentActivity: RecentActivity[];
  topProjects: ProjectStats[];
  topUsers: UserStats[];
}
