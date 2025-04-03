import model.Task;
import model.TaskStatus;
import model.TaskType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import service.Managers;
import service.TaskManager;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class TaskTest {

    private Task task;
    private TaskManager manager;

    @BeforeEach
    public void beforeEach() {
        task = new Task("testName", "testDescription", TaskStatus.IN_PROGRESS);
        manager = Managers.getDefault();
    }

    @Test
    void getNameTest() {
        assertEquals("testName", task.getName());
    }

    @Test
    void setName() {
        task.setName("testName2");
        assertEquals("testName2", task.getName());
    }

    @Test
    void getDescription() {
        assertEquals("testDescription", task.getDescription());
    }

    @Test
    void setDescription() {
        task.setDescription("newDescription");
        assertEquals("newDescription", task.getDescription());
    }

    @Test
    void getStatus() {
        assertEquals(TaskStatus.IN_PROGRESS, task.getStatus());
        task.setStatus(TaskStatus.DONE);
        assertEquals(TaskStatus.DONE, task.getStatus());
        task.setStatus(TaskStatus.NEW);
        assertEquals(TaskStatus.NEW, task.getStatus());

    }

    @Test
    void setStatus() {
        task.setStatus(TaskStatus.NEW);
        assertEquals(TaskStatus.NEW, task.getStatus());
    }

    @Test
    void getIdBeforePuttingInTasksInManager() {
        assertNull(task.getId());
    }

    @Test
    void getType() {
        manager.createTask(task);
        assertEquals(TaskType.TASK, task.getType());
    }

    @Test
    void getIdAfterPuttingInTasksInManager() {
        manager.createTask(task);
        assertEquals(1, task.getId());
    }

    @Test
    void setId() {
        task.setId(1);
        assertEquals(1, task.getId());
    }

    @Test
    void testEquals() {
        manager.createTask(task);
        Task task2 = new Task("testName2", "testDescription2", TaskStatus.NEW);
        task2.setId(task.getId());
        assertEquals(task, task2);
        assertEquals(task, task);
        assertNotEquals(null, task);
        assertNotEquals(2, task);
    }

    @Test
    void testSetStartTime() {
        LocalDateTime now = LocalDateTime.now();
        task.setStartTime(now);
        assertEquals(now, task.getStartTime());
    }

    @Test
    void testSetDuration() {
        Duration duration = Duration.ofMinutes(90);
        task.setDuration(duration);
        assertEquals(duration, task.getDuration());
    }

    @Test
    void testGetEndTimeWhenStartAndDurationAreSet() {
        LocalDateTime start = LocalDateTime.of(2024, 3, 29, 14, 0);
        Duration duration = Duration.ofMinutes(45);
        task.setStartTime(start);
        task.setDuration(duration);

        LocalDateTime expectedEnd = start.plus(duration);
        assertEquals(expectedEnd, task.getEndTime());
    }

    @Test
    void testGetEndTimeWhenStartIsNull() {
        task.setDuration(Duration.ofMinutes(30));
        assertNull(task.getEndTime());
    }

    @Test
    void testGetEndTimeWhenDurationIsNull() {
        task.setStartTime(LocalDateTime.now());
        assertNull(task.getEndTime());
    }

    @Test
    void testGetEndTimeWhenStartAndDurationAreNull() {
        assertNull(task.getEndTime());
    }
}