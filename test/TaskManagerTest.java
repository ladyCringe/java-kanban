import exceptions.NotFoundException;
import model.Epic;
import model.Subtask;
import model.Task;
import model.TaskStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import service.TaskManager;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public abstract class TaskManagerTest<T extends TaskManager> {

    protected T manager;
    protected Task task;
    protected Epic epic;

    protected abstract T createManager();

    @BeforeEach
    void beforeEach() {
        manager = createManager();
        task = new Task("Test model.Task", "model.Task Description", TaskStatus.NEW);
        epic = new Epic("Test model.Epic", "model.Epic Description");
    }

    @Test
    void subtasksMustBelongToEpic() {
        manager.createEpic(epic);

        Subtask sub = new Subtask("Sub", "desc", TaskStatus.NEW, epic.getId());
        manager.createSubtask(sub);

        assertTrue(manager.getEpicsSubtasksById(epic.getId()).contains(sub));
        assertTrue(manager.getEpicById(epic.getId()).getSubtasks().contains(sub));
    }

    @Test
    void epicStatusCalculatedBySubtasksNEW() {
        manager.createEpic(epic);

        Subtask sub1 = new Subtask("s1", "desc", TaskStatus.NEW, epic.getId());
        Subtask sub2 = new Subtask("s2", "desc", TaskStatus.NEW, epic.getId());

        manager.createSubtask(sub1);
        manager.createSubtask(sub2);

        assertEquals(2, manager.getEpicById(epic.getId()).getSubtasks().size());
        assertEquals(TaskStatus.NEW, manager.getEpicById(epic.getId()).getStatus());
    }

    @Test
    void epicStatusCalculatedBySubtasksIN_PROGRESS() {
        manager.createEpic(epic);

        Subtask sub1 = new Subtask("s1", "desc", TaskStatus.NEW, epic.getId());
        Subtask sub2 = new Subtask("s2", "desc", TaskStatus.DONE, epic.getId());
        manager.createSubtask(sub1);
        manager.createSubtask(sub2);

        assertEquals(2, manager.getEpicById(epic.getId()).getSubtasks().size());
        assertEquals(TaskStatus.IN_PROGRESS, manager.getEpicById(epic.getId()).getStatus());
    }

    @Test
    void epicStatusCalculatedBySubtasksDONE() {
        manager.createEpic(epic);

        Subtask sub1 = new Subtask("s1", "desc", TaskStatus.DONE, epic.getId());
        Subtask sub2 = new Subtask("s2", "desc", TaskStatus.DONE, epic.getId());
        manager.createSubtask(sub1);
        manager.createSubtask(sub2);

        assertEquals(2, manager.getEpicById(epic.getId()).getSubtasks().size());
        assertEquals(TaskStatus.DONE, manager.getEpicById(epic.getId()).getStatus());
    }

    @Test
    void detectTimeOverlapBetweenTasks() {
        Task task1 = new Task("Task1", "desc", TaskStatus.NEW,
                LocalDateTime.of(2023, 1, 1, 10, 0), Duration.ofMinutes(60));
        manager.createTask(task1);

        Task task2 = new Task("Task2", "desc", TaskStatus.NEW,
                LocalDateTime.of(2023, 1, 1, 10, 30), Duration.ofMinutes(30));

        Exception ex = assertThrows(IllegalArgumentException.class,
                () -> manager.createTask(task2),
                "Overlapping tasks must throw exception");

        assertTrue(ex.getMessage().contains("intersects"));
    }

    @Test
    void nonOverlappingTasks() {
        Task task1 = new Task("Task1", "desc", TaskStatus.NEW,
                LocalDateTime.of(2023, 1, 1, 10, 0), Duration.ofMinutes(30));
        manager.createTask(task1);

        Task task2 = new Task("Task2", "desc", TaskStatus.NEW,
                LocalDateTime.of(2023, 1, 1, 10, 30), Duration.ofMinutes(30));
        assertDoesNotThrow(() -> manager.createTask(task2));
    }

    //---------------------------------------------------
    //блок тестов для tasks
    //---------------------------------------------------
    @Test
    void getTasks() {
        manager.createTask(task);
        Task task2 = new Task("New model.Task Name", "New model.Task Description", TaskStatus.NEW);
        manager.createTask(task2);
        ArrayList<Task> tasks = new ArrayList<>();
        tasks.add(task);
        tasks.add(task2);

        assertEquals(tasks, manager.getTasks());
    }

    @Test
    void deleteAllTasks() {
        manager.createTask(task);
        Task task2 = new Task("New model.Task Name", "New model.Task Description", TaskStatus.NEW);
        manager.createTask(task2);
        manager.deleteAllTasks();

        assertEquals(0, manager.getTasks().size());
    }

    @Test
    void deleteAllTasksHistory() {
        manager.createTask(task);
        Task task2 = new Task("New model.Task Name", "New model.Task Description", TaskStatus.NEW);
        manager.createTask(task2);

        assertEquals(task, manager.getTaskById(task.getId()));
        assertEquals(task2, manager.getTaskById(task2.getId()));
        assertEquals(2, manager.getHistory().size());

        manager.deleteAllTasks();

        assertTrue(manager.getHistory().isEmpty());
    }

    @Test
    void testCreateTask() {
        manager.createTask(task);
        assertThrows(NotFoundException.class, () -> manager.createTask(task));

        assertEquals(1, manager.getTasks().size());

        task.setId(2);
        manager.createTask(task);
        assertEquals(2, manager.getTasks().size());

        task.setId(3);
        assertEquals(1, manager.getTaskById(1).getId());
        assertEquals(2, manager.getTaskById(2).getId());
    }

    @Test
    void testGetTaskById() {
        manager.createTask(task);
        Task task2 = manager.getTaskById(1);

        assertNotNull(task2);
        assertEquals(task, task2);

        Task task3 = new Task("Test model.Task", "model.Task Description", TaskStatus.NEW);
        task3.setId(2);

        assertThrows(NotFoundException.class, () -> manager.getTaskById(2));
    }

    @Test
    void updateTask() {
        manager.createTask(task);

        assertEquals(task, manager.getTaskById(task.getId()));
        assertEquals(1, manager.getHistory().size());

        task.setName("Updated model.Task Name");
        task.setDescription("Updated model.Task Description");
        task.setStatus(TaskStatus.DONE);
        manager.updateTask(task);
        Task task2 = manager.getTaskById(task.getId());

        assertEquals("Updated model.Task Name", task2.getName());
        assertEquals("Updated model.Task Description", task2.getDescription());
        assertEquals(TaskStatus.DONE, task2.getStatus());

        assertEquals(task2, manager.getTaskById(task.getId()));
        assertEquals(1, manager.getHistory().size());

        Task task3 = new Task("New model.Task Name", "New model.Task Description", TaskStatus.NEW);
        task2.setId(10);
        assertThrows(NotFoundException.class, () -> manager.updateTask(task3));
    }

    @Test
    void updateTaskNewStartTimeAndDuration() {
        manager.createTask(task);
        task.setStartTime(LocalDateTime.of(2023, 1, 1, 12, 0));
        task.setDuration(Duration.ofMinutes(30));
        manager.updateTask(task);
        Task updated = manager.getTaskById(task.getId());

        assertNotNull(updated.getStartTime());
        assertNotNull(updated.getDuration());
        assertEquals(LocalDateTime.of(2023, 1, 1, 12, 0), updated.getStartTime());
        assertEquals(Duration.ofMinutes(30), updated.getDuration());

        task.setStartTime(null);
        task.setDuration(null);

        manager.updateTask(task);
        Task updated1 = manager.getTaskById(task.getId());

        assertNull(updated1.getStartTime());
        assertNull(updated1.getDuration());
    }

    @Test
    void updateTaskOverlaps() {
        manager.createTask(task);
        Task another = new Task("Another", "Conflict", TaskStatus.NEW,
                LocalDateTime.of(2023, 1, 1, 12, 15),
                Duration.ofMinutes(30));
        manager.createTask(another);

        task.setStartTime(LocalDateTime.of(2023, 1, 1, 12, 20));
        task.setDuration(Duration.ofMinutes(30));

        assertThrows(IllegalArgumentException.class, () -> manager.updateTask(task));
    }

    @Test
    void updateTaskIfTaskNotExists() {
        manager.createTask(task);
        Task unknown = new Task("Ghost", "No such task", TaskStatus.NEW);
        unknown.setId(999);
        assertThrows(NotFoundException.class, () -> manager.updateTask(unknown));
    }

    @Test
    void deleteTaskById() {
        manager.createTask(task);
        assertEquals(task, manager.getTaskById(task.getId()));
        Task task2 = new Task("New model.Task Name", "New model.Task Description", TaskStatus.NEW);
        task2.setId(2);
        assertThrows(NotFoundException.class, () -> manager.deleteTaskById(2));

        assertEquals(1, manager.getTasks().size());
        assertEquals(1, manager.getHistory().size());

        manager.deleteTaskById(1);

        assertEquals(0, manager.getTasks().size());
        assertTrue(manager.getHistory().isEmpty());
    }

    //---------------------------------------------------
    //блок тестов для subtasks
    //---------------------------------------------------
    @Test
    void deleteAllSubtasks() {
        manager.createEpic(epic);

        Subtask subtask1 = new Subtask("model.Subtask 1", "Description model.Subtask 1",
                TaskStatus.NEW, epic.getId());
        Subtask subtask2 = new Subtask("model.Subtask 2", "Description model.Subtask 2",
                TaskStatus.IN_PROGRESS, epic.getId());

        manager.createSubtask(subtask1);
        manager.createSubtask(subtask2);

        assertEquals(subtask1, manager.getSubtaskById(subtask1.getId()));
        assertEquals(subtask2, manager.getSubtaskById(subtask2.getId()));
        assertEquals(2, manager.getHistory().size());

        manager.deleteAllSubtasks();

        assertTrue(manager.getSubtasks().isEmpty());
        assertTrue(manager.getEpicsSubtasksById(epic.getId()).isEmpty());
        assertTrue(manager.getHistory().isEmpty());
    }

    @Test
    void deleteAllSubtasksHistory() {
        manager.createEpic(epic);

        Subtask subtask1 = new Subtask("model.Subtask 1", "Description model.Subtask 1",
                TaskStatus.NEW, epic.getId());
        Subtask subtask2 = new Subtask("model.Subtask 2", "Description model.Subtask 2",
                TaskStatus.IN_PROGRESS, epic.getId());

        manager.createSubtask(subtask1);
        manager.createSubtask(subtask2);

        assertEquals(subtask1, manager.getSubtaskById(subtask1.getId()));
        assertEquals(subtask2, manager.getSubtaskById(subtask2.getId()));
        assertEquals(2, manager.getHistory().size());
        assertEquals(2, manager.getSubtasks().size());

        manager.deleteAllSubtasks();

        assertTrue(manager.getSubtasks().isEmpty());
        assertTrue(manager.getEpicsSubtasksById(epic.getId()).isEmpty());
        assertTrue(manager.getHistory().isEmpty());
    }

    @Test
    void testCreateSubtask() {
        manager.createEpic(epic);

        Subtask subtask = new Subtask("model.Subtask 1", "model.Subtask Description",
                TaskStatus.NEW, epic.getId());
        manager.createSubtask(subtask);
        assertThrows(NotFoundException.class, () -> manager.createSubtask(subtask));

        assertEquals(1, manager.getSubtasks().size());

        Epic newEpic = new Epic("New model.Epic Description", "New model.Epic Description");
        newEpic.setId(2);
        Subtask subtask2 = new Subtask("model.Subtask 2", "model.Subtask Description",
                TaskStatus.NEW, newEpic.getId());
        assertThrows(NotFoundException.class, () -> manager.createSubtask(subtask2));

        assertEquals(1, manager.getSubtasks().size());

        Subtask subtask3 = new Subtask("model.Subtask 2", "model.Subtask Description",
                TaskStatus.NEW, epic.getId());
        subtask3.setId(epic.getId());
        assertThrows(NotFoundException.class, () -> manager.createSubtask(subtask3));
    }

    @Test
    void сreateSubtaskWithoutEpic() {
        Epic fakeEpic = new Epic("Fake", "No such epic");
        fakeEpic.setId(999);

        Subtask subtask = new Subtask("Orphan", "Has no parent",
                TaskStatus.NEW, fakeEpic.getId());

        assertThrows(NotFoundException.class, () -> manager.createSubtask(subtask));

        assertTrue(manager.getSubtasks().isEmpty());
    }

    @Test
    void createSubtaskOverlaps() {
        manager.createEpic(epic);
        Subtask sub1 = new Subtask("Sub1", "desc", TaskStatus.NEW,
                LocalDateTime.of(2024, 3, 1, 10, 0),
                Duration.ofMinutes(60),
                epic.getId());
        manager.createSubtask(sub1);

        Subtask sub2 = new Subtask("Sub2", "desc", TaskStatus.NEW,
                LocalDateTime.of(2024, 3, 1, 10, 30),
                Duration.ofMinutes(30),
                epic.getId());

        assertThrows(IllegalArgumentException.class, () -> manager.createSubtask(sub2));
    }

    @Test
    void testGetSubtaskById() {
        manager.createEpic(epic);

        Subtask subtask = new Subtask("model.Subtask 1", "model.Subtask Description",
                TaskStatus.NEW, epic.getId());
        manager.createSubtask(subtask);

        Subtask subtask2 = manager.getSubtaskById(2);

        assertNotNull(subtask2, "model.Subtask should be retrievable after creation");
        assertEquals(subtask, subtask2, "Retrieved subtask should match the created subtask");

        Subtask subtask3 = new Subtask("model.Subtask 2", "model.Subtask Description",
                TaskStatus.NEW, epic.getId());
        subtask3.setId(3);

        assertThrows(NotFoundException.class, () -> manager.getSubtaskById(3));
    }

    @Test
    void testUpdateSubtask() {
        manager.createEpic(epic);

        Subtask subtask = new Subtask("model.Subtask 1", "model.Subtask Description",
                TaskStatus.NEW, epic.getId());
        manager.createSubtask(subtask);

        assertEquals(subtask, manager.getSubtaskById(subtask.getId()));
        assertEquals(1, manager.getHistory().size());

        subtask.setName("Updated model.Subtask Name");
        subtask.setDescription("Updated model.Subtask Description");
        subtask.setStatus(TaskStatus.IN_PROGRESS);
        manager.updateSubtask(subtask);

        Subtask updatedSubtask = manager.getSubtaskById(2);
        assertEquals("Updated model.Subtask Name", updatedSubtask.getName());
        assertEquals("Updated model.Subtask Description", updatedSubtask.getDescription());
        assertEquals(TaskStatus.IN_PROGRESS, updatedSubtask.getStatus());
        assertEquals(TaskStatus.IN_PROGRESS, manager.getEpicById(epic.getId()).getStatus());

        assertEquals(updatedSubtask, manager.getSubtaskById(subtask.getId()));
        assertEquals(2, manager.getHistory().size());

        Subtask subtask2 = new Subtask("model.Subtask 22", "model.Subtask Description",
                TaskStatus.NEW, epic.getId());
        subtask2.setId(10);
        assertThrows(NotFoundException.class, () -> manager.updateSubtask(subtask2));

        assertThrows(NotFoundException.class, () -> assertNull(manager.getSubtaskById(10)));

        Epic newEpic = new Epic("New model.Epic Description", "New model.Epic Description");
        newEpic.setId(2);
        Subtask subtask3 = new Subtask("model.Subtask 3", "model.Subtask Description",
                TaskStatus.NEW, newEpic.getId());
        subtask3.setId(subtask.getId());
        assertThrows(NotFoundException.class, () -> manager.updateSubtask(subtask3));

        assertEquals("Updated model.Subtask Name", updatedSubtask.getName());
        assertEquals(TaskStatus.IN_PROGRESS, updatedSubtask.getStatus());
        assertEquals(TaskStatus.IN_PROGRESS, manager.getEpicById(epic.getId()).getStatus());
    }

    @Test
    void updateSubtaskNewStartTimeAndDuration() {
        manager.createEpic(epic);
        Subtask subtask = new Subtask("Subtask", "desc", TaskStatus.NEW, epic.getId());
        manager.createSubtask(subtask);

        subtask.setStartTime(LocalDateTime.of(2023, 1, 1, 14, 0));
        subtask.setDuration(Duration.ofMinutes(45));
        manager.updateSubtask(subtask);

        Subtask updated = manager.getSubtaskById(subtask.getId());

        assertNotNull(updated.getStartTime());
        assertNotNull(updated.getDuration());
        assertEquals(LocalDateTime.of(2023, 1, 1, 14, 0), updated.getStartTime());
        assertEquals(Duration.ofMinutes(45), updated.getDuration());

        subtask.setStartTime(null);
        subtask.setDuration(null);
        manager.updateSubtask(subtask);

        Subtask updated2 = manager.getSubtaskById(subtask.getId());

        assertNull(updated2.getStartTime());
        assertNull(updated2.getDuration());
    }

    @Test
    void updateSubtaskOverlaps() {
        manager.createEpic(epic);
        Subtask sub1 = new Subtask("Sub1", "desc", TaskStatus.NEW,
                LocalDateTime.of(2023, 1, 1, 10, 0),
                Duration.ofMinutes(30), epic.getId());
        manager.createSubtask(sub1);

        Subtask sub2 = new Subtask("Sub2", "desc", TaskStatus.NEW,
                LocalDateTime.of(2023, 1, 1, 11, 0),
                Duration.ofMinutes(30), epic.getId());
        manager.createSubtask(sub2);

        sub2.setStartTime(LocalDateTime.of(2023, 1, 1, 10, 15));
        sub2.setDuration(Duration.ofMinutes(30));

        assertThrows(IllegalArgumentException.class, () -> manager.updateSubtask(sub2));
    }

    @Test
    void updateSubtaskIfNotExists() {
        manager.createEpic(epic);
        Subtask unknown = new Subtask("Ghost", "desc", TaskStatus.NEW, epic.getId());
        unknown.setId(999);
        assertThrows(NotFoundException.class, () -> manager.updateSubtask(unknown));
    }

    @Test
    void deleteSubtaskById() {
        manager.createEpic(epic);

        Subtask subtask1 = new Subtask("model.Subtask 1", "Description model.Subtask 1",
                TaskStatus.NEW, epic.getId());
        Subtask subtask2 = new Subtask("model.Subtask 2", "Description model.Subtask 2",
                TaskStatus.IN_PROGRESS, epic.getId());

        manager.createSubtask(subtask1);
        manager.createSubtask(subtask2);

        assertEquals(subtask1, manager.getSubtaskById(subtask1.getId()));
        assertEquals(subtask2, manager.getSubtaskById(subtask2.getId()));
        assertEquals(2, manager.getSubtasks().size());
        assertEquals(2, manager.getHistory().size());

        manager.deleteSubtaskById(subtask1.getId());

        assertEquals(1, manager.getSubtasks().size());
        assertEquals(subtask2, manager.getSubtasks().getFirst());
        assertEquals(1, manager.getHistory().size());
        assertFalse(manager.getEpicsSubtasksById(epic.getId()).contains(subtask1));
        assertThrows(NotFoundException.class, () -> manager.getSubtaskById(subtask1.getId()));

        assertThrows(NotFoundException.class, () -> manager.deleteSubtaskById(subtask1.getId()));

        assertEquals(1, manager.getSubtasks().size());
        assertEquals(subtask2, manager.getSubtasks().getFirst());
        assertEquals(1, manager.getHistory().size());
        assertTrue(manager.getEpicsSubtasksById(epic.getId()).contains(subtask2));

        manager.deleteSubtaskById(subtask2.getId());

        assertEquals(0, manager.getSubtasks().size());
        assertTrue(manager.getHistory().isEmpty());
        assertThrows(NotFoundException.class, () -> manager.getSubtaskById(subtask2.getId()));
    }

    //---------------------------------------------------
    //блок тестов для epics
    //---------------------------------------------------
    @Test
    void deleteAllEpics() {
        Epic epic2 = new Epic("model.Epic 2", "Description model.Epic 2");
        manager.createEpic(epic);
        manager.createEpic(epic2);

        assertEquals(2, manager.getEpics().size());

        manager.deleteAllEpics();

        assertTrue(manager.getEpics().isEmpty());
        assertTrue(manager.getSubtasks().isEmpty());
    }

    @Test
    void deleteAllEpicsHistory() {
        Epic epic2 = new Epic("model.Epic 2", "Description model.Epic 2");
        manager.createEpic(epic);
        manager.createEpic(epic2);

        assertEquals(2, manager.getEpics().size());
        assertEquals(epic, manager.getEpicById(epic.getId()));
        assertEquals(epic2, manager.getEpicById(epic2.getId()));
        assertEquals(2, manager.getHistory().size());

        Subtask subtask1 = new Subtask("model.Subtask 1", "Description model.Subtask 1",
                TaskStatus.NEW, epic.getId());
        Subtask subtask2 = new Subtask("model.Subtask 2", "Description model.Subtask 2",
                TaskStatus.IN_PROGRESS, epic2.getId());

        manager.createSubtask(subtask1);
        manager.createSubtask(subtask2);

        assertEquals(subtask1, manager.getSubtaskById(subtask1.getId()));
        assertEquals(subtask2, manager.getSubtaskById(subtask2.getId()));
        assertEquals(4, manager.getHistory().size());
        assertEquals(2, manager.getSubtasks().size());

        manager.deleteAllEpics();

        assertTrue(manager.getEpics().isEmpty());
        assertTrue(manager.getSubtasks().isEmpty());
        assertThrows(NotFoundException.class, () -> manager.getEpicsSubtasksById(epic.getId()));
        assertThrows(NotFoundException.class, () -> manager.getEpicsSubtasksById(epic2.getId()));
        assertTrue(manager.getHistory().isEmpty());
    }

    @Test
    void testCreateEpic() {
        manager.createEpic(epic);
        assertThrows(NotFoundException.class, () -> manager.createEpic(epic));

        assertEquals(1, manager.getEpics().size());
    }

    @Test
    void testCreatGetEpicById() {
        manager.createEpic(epic);
        Epic newEpic = manager.getEpicById(1);

        assertNotNull(newEpic);
        assertEquals(epic, newEpic);

        Epic newEpic2 = new Epic("New model.Epic Description", "New model.Epic Description");
        newEpic2.setId(2);
        assertThrows(NotFoundException.class, () -> manager.getEpicById(2));
    }

    @Test
    void createEpicWithIntersections() {
        Task existing = new Task("Task1", "Desc", TaskStatus.NEW,
                LocalDateTime.of(2023, 3, 1, 10, 0),
                Duration.ofMinutes(60));
        manager.createTask(existing);

        Epic intersectingEpic = new Epic("Epic1", "Desc");
        manager.createEpic(intersectingEpic);
        Subtask fakeSub = new Subtask("Sub", "desc", TaskStatus.NEW,
                LocalDateTime.of(2023, 3, 1, 10, 30),
                Duration.ofMinutes(30), intersectingEpic.getId());
        assertThrows(IllegalArgumentException.class, () -> manager.createSubtask(fakeSub));
    }

    @Test
    void updateEpic() {
        manager.createEpic(epic);

        assertEquals(epic, manager.getEpicById(epic.getId()));
        assertEquals(1, manager.getHistory().size());

        epic.setName("Updated model.Epic 1");
        epic.setDescription("Updated Description model.Epic 1");
        manager.updateEpic(epic);

        Epic updatedEpic = manager.getEpicById(1);
        assertEquals("Updated model.Epic 1", updatedEpic.getName());
        assertEquals("Updated Description model.Epic 1", updatedEpic.getDescription());

        assertEquals(updatedEpic, manager.getEpicById(epic.getId()));
        assertEquals(1, manager.getHistory().size());

        Epic epic2 = new Epic("New model.Epic Description", "New model.Epic Description");
        epic2.setId(2);
        assertThrows(NotFoundException.class, () -> manager.updateEpic(epic2));

        assertThrows(NotFoundException.class, () -> manager.getEpicById(2));
    }

    @Test
    void deleteEpicById() {
        manager.createEpic(epic);
        Subtask subtask = new Subtask("model.Subtask", "Description", TaskStatus.NEW, epic.getId());
        manager.createSubtask(subtask);

        assertEquals(epic, manager.getEpicById(epic.getId()));
        assertEquals(subtask, manager.getSubtaskById(subtask.getId()));
        assertEquals(1, manager.getEpics().size());
        assertEquals(1, manager.getSubtasks().size());
        assertEquals(2, manager.getHistory().size());

        Epic epic2 = new Epic("model.Epic 2", "Description model.Epic 2");
        epic2.setId(3);
        assertThrows(NotFoundException.class, () -> manager.deleteEpicById(epic2.getId()));

        assertEquals(1, manager.getEpics().size());

        manager.deleteEpicById(epic.getId());

        assertFalse(manager.getSubtasks().contains(subtask));
        assertEquals(0, manager.getEpics().size());
        assertEquals(0, manager.getSubtasks().size());
        assertTrue(manager.getHistory().isEmpty());
    }

    @Test
    void getEpicsSubtasksById() {
        manager.createEpic(epic);

        Subtask subtask1 = new Subtask("model.Subtask 1", "Description model.Subtask 1",
                TaskStatus.NEW, epic.getId());
        Subtask subtask2 = new Subtask("model.Subtask 2", "Description model.Subtask 2",
                TaskStatus.IN_PROGRESS, epic.getId());

        manager.createSubtask(subtask1);
        manager.createSubtask(subtask2);

        List<Subtask> subtasks = manager.getEpicsSubtasksById(epic.getId());

        assertEquals(2, subtasks.size());
        assertTrue(subtasks.contains(subtask1));
        assertTrue(subtasks.contains(subtask2));
        assertEquals(TaskStatus.IN_PROGRESS, manager.getEpicById(epic.getId()).getStatus());

        Epic epic2 = new Epic("model.Epic 2", "Description model.Epic 2");
        epic2.setId(3);
        Subtask subtask3 = new Subtask("model.Subtask 2", "Description model.Subtask 2",
                TaskStatus.IN_PROGRESS, epic2.getId());
        epic2.addSubtask(subtask3);
        assertThrows(NotFoundException.class, () -> manager.getEpicsSubtasksById(epic2.getId()));
    }

    @Test
    void noConflictBetweenTasksWithGeneratedAndGivenId() {
        manager.createTask(task);
        Task newTask = new Task("Updated task", "Updated description", TaskStatus.DONE);
        newTask.setId(task.getId());
        manager.updateTask(newTask);

        assertEquals(1, manager.getTasks().size());
        assertEquals("Updated task", manager.getTaskById(task.getId()).getName());
        assertEquals("Updated description", manager.getTaskById(task.getId()).getDescription());
        assertEquals(TaskStatus.DONE, manager.getTaskById(task.getId()).getStatus());

        manager.createEpic(epic);
        Epic epic2 = new Epic("model.Epic 2", "Description model.Epic 2");
        epic2.setId(epic.getId());
        manager.updateEpic(epic2);

        assertEquals(1, manager.getEpics().size());
        assertEquals("model.Epic 2", manager.getEpicById(epic.getId()).getName());
        assertEquals("Description model.Epic 2", manager.getEpicById(epic.getId()).getDescription());

        Subtask subtask1 = new Subtask("model.Subtask 1", "Description model.Subtask 1",
                TaskStatus.NEW, epic.getId());
        manager.createSubtask(subtask1);
        Subtask subtask2 = new Subtask("model.Subtask 2", "Description model.Subtask 2",
                TaskStatus.IN_PROGRESS, epic.getId());
        subtask2.setId(subtask1.getId());
        manager.updateSubtask(subtask2);

        assertEquals(1, manager.getSubtasks().size());
        assertEquals("model.Subtask 2", manager.getSubtaskById(subtask1.getId()).getName());
        assertEquals("Description model.Subtask 2", manager.getSubtaskById(subtask1.getId()).getDescription());
        assertEquals(TaskStatus.IN_PROGRESS, manager.getEpicById(epic.getId()).getStatus());
        assertEquals(TaskStatus.IN_PROGRESS, manager.getSubtaskById(subtask1.getId()).getStatus());
    }

    @Test
    void testSubtaskDeletionRemovesItFromEpic() {
        manager.createEpic(epic);

        Subtask subtask1 = new Subtask("Subtask 1", "Description 1", TaskStatus.NEW, epic.getId());
        manager.createSubtask(subtask1);
        int subId = subtask1.getId();

        assertEquals(1, manager.getEpicById(epic.getId()).getSubtasks().size());

        manager.deleteSubtaskById(subId);

        assertFalse(epic.getSubtasks().contains(subtask1));
        assertEquals(0, epic.getSubtasks().size());
        assertThrows(NotFoundException.class, () -> manager.getSubtaskById(subtask1.getId()));
    }

    @Test
    void getDurationWithSubtasks() {
        manager.createEpic(epic);

        Subtask sub1 = new Subtask("Sub1", "Desc1", TaskStatus.NEW,
                LocalDateTime.of(2024, 1, 1, 10, 0),
                Duration.ofMinutes(30), epic.getId());
        Subtask sub2 = new Subtask("Sub2", "Desc2", TaskStatus.NEW,
                LocalDateTime.of(2024, 1, 1, 11, 0),
                Duration.ofMinutes(90), epic.getId());
        manager.createSubtask(sub1);
        manager.createSubtask(sub2);

        Duration expected = Duration.ofMinutes(120);
        assertEquals(expected, manager.getEpicById(epic.getId()).getDuration());
    }

    @Test
    void getEndTimeWithSubtasks() {
        manager.createEpic(epic);

        Subtask sub1 = new Subtask("Sub 1", "Desc 1", TaskStatus.NEW,
                LocalDateTime.of(2024, 3, 30, 10, 0),
                Duration.ofMinutes(30), epic.getId());
        Subtask sub2 = new Subtask("Sub 2", "Desc 2", TaskStatus.DONE,
                LocalDateTime.of(2024, 3, 30, 12, 0),
                Duration.ofMinutes(45), epic.getId());
        Subtask sub3 = new Subtask("Sub 3", "Desc 3", TaskStatus.DONE,
                LocalDateTime.of(2024, 3, 30, 9, 0),
                Duration.ofMinutes(15), epic.getId());
        manager.createSubtask(sub1);
        manager.createSubtask(sub2);
        manager.createSubtask(sub3);

        LocalDateTime expectedEnd = sub2.getStartTime().plus(sub2.getDuration());
        LocalDateTime expectedStart = sub3.getStartTime();

        assertEquals(3, manager.getEpicById(epic.getId()).getSubtasks().size());
        assertEquals(expectedEnd, manager.getEpicById(epic.getId()).getEndTime());
        assertEquals(expectedStart, manager.getEpicById(epic.getId()).getStartTime());
    }
}
