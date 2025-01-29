import model.Task;
import model.TaskStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import service.HistoryManager;
import service.Managers;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryHistoryManagerTest {

    HistoryManager historyManager;

    @BeforeEach
    public void beforeEach() {
        historyManager = Managers.getDefaultHistory();
    }

    @Test
    void testAddTaskToHistory() {
        Task task1 = new Task( "model.Task 1", "Description 1", TaskStatus.NEW);
        historyManager.add(task1);

        assertEquals(1, historyManager.getHistory().size());
        assertEquals(task1, historyManager.getHistory().getFirst());
    }

    @Test
    void testHistoryMaxSize() {
        for (int i = 0; i <= 12; i++) {
            historyManager.add(new Task("model.Task " + i, "Description " + i, TaskStatus.NEW));
        }

        assertEquals(10, historyManager.getHistory().size());
        assertEquals("model.Task 3", historyManager.getHistory().getFirst().getName());
    }

    @Test
    void testPreservePreviousTaskVersionInHistory() {
        Task task = new Task("model.Task 1", "Description 1", TaskStatus.NEW);
        historyManager.add(task);

        task.setName("Updated model.Task 1");
        task.setDescription("Updated Description 1");
        historyManager.add(task);

        assertEquals(2, historyManager.getHistory().size());
        assertEquals("model.Task 1", historyManager.getHistory().get(0).getName());
        assertEquals("Updated model.Task 1", historyManager.getHistory().get(1).getName());
    }
}