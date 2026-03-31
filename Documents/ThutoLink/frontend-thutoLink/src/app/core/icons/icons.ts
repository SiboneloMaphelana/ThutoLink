import type { IconifyIcon } from '@iconify/types';
import school from '@iconify/icons-ic/baseline-school';
import groups from '@iconify/icons-ic/outline-groups';
import assignment from '@iconify/icons-ic/baseline-assignment';
import dashboard from '@iconify/icons-ic/outline-dashboard';
import calendar from '@iconify/icons-ic/outline-calendar-month';
import notifications from '@iconify/icons-ic/outline-notifications';
import review from '@iconify/icons-ic/outline-rate-review';
import forum from '@iconify/icons-ic/outline-forum';
import family from '@iconify/icons-ic/outline-family-restroom';
import person from '@iconify/icons-ic/outline-person';
import teacher from '@iconify/icons-ic/outline-co-present';
import book from '@iconify/icons-ic/outline-menu-book';
import logout from '@iconify/icons-ic/outline-logout';
import send from '@iconify/icons-ic/outline-send';
import addCircle from '@iconify/icons-ic/outline-add-circle';
import checkCircle from '@iconify/icons-ic/outline-check-circle';
import pending from '@iconify/icons-ic/outline-pending-actions';
import warning from '@iconify/icons-ic/outline-warning-amber';
import insights from '@iconify/icons-ic/outline-insights';
import taskAlt from '@iconify/icons-ic/round-task-alt';

export type AppIconName =
  | 'school'
  | 'groups'
  | 'assignment'
  | 'dashboard'
  | 'calendar'
  | 'notifications'
  | 'review'
  | 'forum'
  | 'family'
  | 'person'
  | 'teacher'
  | 'book'
  | 'logout'
  | 'send'
  | 'addCircle'
  | 'checkCircle'
  | 'pending'
  | 'warning'
  | 'insights'
  | 'taskAlt';

export const appIcons: Record<AppIconName, IconifyIcon> = {
  school: school as IconifyIcon,
  groups: groups as IconifyIcon,
  assignment: assignment as IconifyIcon,
  dashboard: dashboard as IconifyIcon,
  calendar: calendar as IconifyIcon,
  notifications: notifications as IconifyIcon,
  review: review as IconifyIcon,
  forum: forum as IconifyIcon,
  family: family as IconifyIcon,
  person: person as IconifyIcon,
  teacher: teacher as IconifyIcon,
  book: book as IconifyIcon,
  logout: logout as IconifyIcon,
  send: send as IconifyIcon,
  addCircle: addCircle as IconifyIcon,
  checkCircle: checkCircle as IconifyIcon,
  pending: pending as IconifyIcon,
  warning: warning as IconifyIcon,
  insights: insights as IconifyIcon,
  taskAlt: taskAlt as IconifyIcon
};
