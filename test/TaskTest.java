import model.Task;
import model.TaskStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import service.Managers;
import service.TaskManager;

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
        assertEquals(task.getName(),"testName");
    }

    @Test
    void setName() {
        task.setName("testName2");
        assertEquals("testName2",task.getName());
    }

    @Test
    void getDescription() {
        assertEquals(task.getDescription(),"testDescription");
    }

    @Test
    void setDescription() {
        task.setDescription("newDescription");
        assertEquals("newDescription",task.getDescription());
    }

    @Test
    void getStatus() {
        assertEquals(task.getStatus(),TaskStatus.IN_PROGRESS);
        task.setStatus(TaskStatus.DONE);
        assertEquals(task.getStatus(),TaskStatus.DONE);
        task.setStatus(TaskStatus.NEW);
        assertEquals(task.getStatus(),TaskStatus.NEW);

    }

    @Test
    void setStatus() {
        task.setStatus(TaskStatus.NEW);
        assertEquals(task.getStatus(),TaskStatus.NEW);
    }

    @Test
    void getIdBeforePuttingInTasksInManager() {
        assertNull(task.getId());
    }

    @Test
    void getIdAfterPuttingInTasksInManager() {
        manager.createTask(task);
        assertEquals(task.getId(),1);
    }

    @Test
    void setId() {
        task.setId(1);
        assertEquals(task.getId(),1);
    }

    @Test
    void testEquals() {
        manager.createTask(task);
        Task task2 = new Task("testName2", "testDescription2", TaskStatus.NEW);
        task2.setId(task.getId());
        assertEquals(task, task2);
        assertEquals(task, task);
        assertNotEquals(task,null);
        assertNotEquals(task,2);
    }
}