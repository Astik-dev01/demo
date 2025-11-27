package kg.taskflow.scheduler;

import kg.taskflow.db.entity.Task;
import kg.taskflow.db.repository.TaskRepository;
import kg.taskflow.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DeadlineReminderScheduler {

    private final TaskRepository taskRepository;
    private final NotificationService notificationService;

    /**
     * Send deadline reminders every day at 9:00 AM
     * Notifies assignees about tasks due today, tomorrow, and in 3 days
     */
    @Scheduled(cron = "0 0 9 * * *") // Every day at 9:00 AM
    public void sendDeadlineReminders() {
        log.info("Starting deadline reminder job...");

        LocalDate today = LocalDate.now();
        LocalDate tomorrow = today.plusDays(1);
        LocalDate inThreeDays = today.plusDays(3);

        // Tasks due today
        List<Task> tasksDueToday = taskRepository.findTasksDueOn(today);
        for (Task task : tasksDueToday) {
            sendReminder(task, 0);
        }
        log.info("Sent {} reminders for tasks due today", tasksDueToday.size());

        // Tasks due tomorrow
        List<Task> tasksDueTomorrow = taskRepository.findTasksDueOn(tomorrow);
        for (Task task : tasksDueTomorrow) {
            sendReminder(task, 1);
        }
        log.info("Sent {} reminders for tasks due tomorrow", tasksDueTomorrow.size());

        // Tasks due in 3 days
        List<Task> tasksDueInThreeDays = taskRepository.findTasksDueOn(inThreeDays);
        for (Task task : tasksDueInThreeDays) {
            sendReminder(task, 3);
        }
        log.info("Sent {} reminders for tasks due in 3 days", tasksDueInThreeDays.size());

        log.info("Deadline reminder job completed");
    }

    private void sendReminder(Task task, int daysUntilDue) {
        if (task.getAssignee() == null) {
            return;
        }

        try {
            String taskKey = task.getProject().getProjectKey() + "-" + task.getNumber();
            notificationService.notifyDeadlineReminder(
                    task.getAssignee().getId(),
                    task.getId(),
                    taskKey,
                    task.getTitle(),
                    daysUntilDue
            );
        } catch (Exception e) {
            log.error("Failed to send deadline reminder for task {}: {}", task.getId(), e.getMessage());
        }
    }
}
