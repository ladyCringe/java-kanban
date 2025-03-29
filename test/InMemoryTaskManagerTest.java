import model.Subtask;
import model.Task;
import model.TaskStatus;
import org.junit.jupiter.api.Test;
import service.InMemoryTaskManager;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryTaskManagerTest extends TaskManagerTest<InMemoryTaskManager> {

    @Override
    protected InMemoryTaskManager createManager() {
        return new InMemoryTaskManager();
    }

    @Test
    void getPrioritizedTasksReturnsSortedList() {
        Task task1 = new Task("Task1", "desc", TaskStatus.NEW,
                LocalDateTime.of(2023, 1, 1, 10, 30), Duration.ofMinutes(30));
        Task task2 = new Task("Task2", "desc", TaskStatus.NEW,
                LocalDateTime.of(2023, 1, 1, 10, 0), Duration.ofMinutes(30));
        manager.createTask(task1);
        manager.createTask(task2);

        List<Task> sorted = manager.getPrioritizedTasks();
        assertEquals(task2, sorted.get(0));
        assertEquals(task1, sorted.get(1));
    }

    @Test
    void createSubtaskPrioritized() {
        manager.createEpic(epic);
        Subtask sub = new Subtask("Sub", "desc", TaskStatus.NEW,
                LocalDateTime.of(2024, 3, 2, 12, 0),
                Duration.ofMinutes(45),
                epic.getId());
        manager.createSubtask(sub);

        assertEquals(2, manager.getPrioritizedTasks().size());
        assertEquals(epic, manager.getPrioritizedTasks().getFirst());
        assertEquals(sub, manager.getPrioritizedTasks().getLast());
    }

    @Test
    void updateEpicPrioritizedTasks() {
        manager.createEpic(epic);

        Subtask subtask = new Subtask("Sub", "desc", TaskStatus.NEW,
                LocalDateTime.of(2023, 3, 1, 10, 0),
                Duration.ofMinutes(60), epic.getId());
        manager.createSubtask(subtask);

        epic.setName("Updated name");
        epic.setDescription("Updated description");

        assertNotNull(manager.getEpicById(epic.getId()).getStartTime());
        manager.updateEpic(epic);

        assertEquals(2, manager.getPrioritizedTasks().size());
        assertEquals(epic, manager.getPrioritizedTasks().getFirst());
        assertEquals(subtask, manager.getPrioritizedTasks().getLast());
    }

}