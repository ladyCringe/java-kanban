import model.Epic;
import model.Subtask;
import model.Task;
import model.TaskStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import service.*;

import static org.junit.jupiter.api.Assertions.*;

class ManagersTest {

    TaskManager manager;
    HistoryManager historyManager;
    Task task;
    Subtask subtask;
    Epic epic;

    @BeforeEach
    public void beforeEach() {
        manager = Managers.getDefault();
        historyManager = Managers.getDefaultHistory();
        task = new Task("Test model.Task", "Test Description", TaskStatus.NEW);
        epic = new Epic("Test model.Epic", "Test Description");
        manager.createTask(task);
        manager.createEpic(epic);
        subtask = new Subtask("SubtaskName", "SubtaskDescription",TaskStatus.NEW, epic.getId());
        manager.createSubtask(subtask);
    }

    @Test
    void testGetDefaultTaskManager() {
        assertNotNull(manager);
        assertInstanceOf(InMemoryTaskManager.class, manager);
        assertEquals(task, manager.getTaskById(task.getId()));
        assertEquals(subtask, manager.getSubtaskById(subtask.getId()));
        assertEquals(epic, manager.getEpicById(epic.getId()));
    }

    @Test
    void testGetDefaultHistoryManager() {
        assertNotNull(historyManager);
        assertInstanceOf(InMemoryHistoryManager.class, historyManager);

        historyManager.add(task);
        historyManager.add(subtask);
        historyManager.add(epic);

        // Проверяем, что задача добавлена в историю
        assertEquals(3, historyManager.getHistory().size());
        assertEquals(task, historyManager.getHistory().get(historyManager.getHistory().indexOf(task)));
        assertEquals(subtask, historyManager.getHistory().get(historyManager.getHistory().indexOf(subtask)));
        assertEquals(epic, historyManager.getHistory().get(historyManager.getHistory().indexOf(epic)));
    }
}