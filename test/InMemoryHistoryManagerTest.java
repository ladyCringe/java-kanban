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
        Task task1 = new Task("model.Task 1", "Description 1", TaskStatus.NEW);
        task1.setId(1);
        historyManager.add(task1);

        assertEquals(1, historyManager.getHistory().size());
        assertEquals(task1, historyManager.getHistory().getFirst());
    }

    @Test
    void testNoDuplicatesInHistory() {
        Task task1 = new Task("Task 1", "Description 1", TaskStatus.NEW);
        historyManager.add(task1);
        assertEquals(0, historyManager.getHistory().size());

        task1.setId(1);
        historyManager.add(task1);
        historyManager.add(task1);

        assertEquals(1, historyManager.getHistory().size());
        assertEquals(task1, historyManager.getHistory().getFirst());
    }

    @Test
    void testRemoveFromHistory() {
        Task task1 = new Task("Task 1", "Description 1", TaskStatus.NEW);
        task1.setId(1);
        Task task2 = new Task("Task 2", "Description 2", TaskStatus.IN_PROGRESS);
        task2.setId(2);
        Task task3 = new Task("Task 3", "Description 3", TaskStatus.IN_PROGRESS);
        task3.setId(3);
        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);

        assertEquals(3, historyManager.getHistory().size());
        assertEquals(task1, historyManager.getHistory().getFirst());
        assertEquals(task3, historyManager.getHistory().getLast());

        Task task4 = new Task("Task 4", "Description 4", TaskStatus.IN_PROGRESS);
        task4.setId(4);

        historyManager.remove(task4.getId());

        assertEquals(3, historyManager.getHistory().size());
        assertEquals(task1, historyManager.getHistory().getFirst());
        assertEquals(task3, historyManager.getHistory().getLast());

        historyManager.remove(task3.getId());

        assertEquals(2, historyManager.getHistory().size());
        assertEquals(task1, historyManager.getHistory().getFirst());
        assertEquals(task2, historyManager.getHistory().getLast());

        historyManager.remove(task1.getId());

        assertEquals(1, historyManager.getHistory().size());
        assertEquals(task2, historyManager.getHistory().getFirst());
        assertEquals(task2, historyManager.getHistory().getLast());
    }

    @Test
    void testEmptyHistory() {
        assertTrue(historyManager.getHistory().isEmpty());
    }

    @Test
    void testNoDuplicates() {
        Task task = new Task("Task 1", "Description 1", TaskStatus.NEW);
        task.setId(1);
        historyManager.add(task);
        historyManager.add(task);
        assertEquals(1, historyManager.getHistory().size());
    }

    @Test
    void testRemoveBeginningMiddleEnd() {
        Task task1 = new Task("Task 1", "Description 1", TaskStatus.NEW);
        task1.setId(1);
        Task task2 = new Task("Task 2", "Description 2", TaskStatus.IN_PROGRESS);
        task2.setId(2);
        Task task3 = new Task("Task 3", "Description 3", TaskStatus.IN_PROGRESS);
        task3.setId(3);
        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);

        historyManager.remove(task1.getId());
        assertEquals(2, historyManager.getHistory().size());
        assertFalse(historyManager.getHistory().contains(task1));

        historyManager.remove(task2.getId());
        assertEquals(1, historyManager.getHistory().size());
        assertFalse(historyManager.getHistory().contains(task2));

        historyManager.remove(task3.getId());
        assertTrue(historyManager.getHistory().isEmpty());
    }
}