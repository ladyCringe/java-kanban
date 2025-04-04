import exceptions.ManagerSaveException;
import exceptions.NotFoundException;
import model.Epic;
import model.Subtask;
import model.Task;
import model.TaskStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import service.FileBackedTaskManager;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class FileBackedTaskManagerTest extends TaskManagerTest<FileBackedTaskManager> {

    private File file;

    @Override
    protected FileBackedTaskManager createManager() {
        Assertions.assertDoesNotThrow(() -> {
            file = File.createTempFile("test", ".csv");
        });

        return new FileBackedTaskManager(file);
    }

    //---------------------------------------------------
    //блок тестов методов для Task
    //---------------------------------------------------
    @Test
    void shouldSaveAndLoadEmptyFile() {
        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(file);
        assertTrue(loaded.getTasks().isEmpty());
        assertTrue(loaded.getEpics().isEmpty());
        assertTrue(loaded.getSubtasks().isEmpty());
    }

    @Test
    void shouldSaveAndLoadTasks() {
        Task task = new Task("Task1", "Description", TaskStatus.NEW);
        manager.createTask(task);

        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(file);
        assertEquals(1, loaded.getTasks().size());
        assertEquals(task.getName(), loaded.getTasks().getFirst().getName());
    }

    @Test
    void shouldSaveAndLoadMultipleTasks() {
        Task task1 = new Task("Task1", "Description1", TaskStatus.NEW);
        Task task2 = new Task("Task2", "Description2", TaskStatus.DONE);
        manager.createTask(task1);
        manager.createTask(task2);

        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(file);
        assertEquals(2, loaded.getTasks().size());
        assertEquals(task1.getName(), loaded.getTasks().getFirst().getName());
        assertEquals(task2.getName(), loaded.getTasks().getLast().getName());
    }

    @Test
    void updateTaskAndLoad() {
        Task task = new Task("Task1", "Description1", TaskStatus.NEW);
        manager.createTask(task);
        task.setDescription("Updated");
        task.setStatus(TaskStatus.IN_PROGRESS);
        manager.updateTask(task);

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(file);
        Task loaded = loadedManager.getTaskById(task.getId());

        assertEquals("Updated", loaded.getDescription());
        assertEquals(TaskStatus.IN_PROGRESS, loaded.getStatus());
    }

    @Test
    void testRemoveTaskAndSave() {
        Task task = new Task("Task1", "Description1", TaskStatus.NEW);
        manager.createTask(task);
        manager.deleteTaskById(task.getId());

        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(file);
        assertThrows(NotFoundException.class, () -> loaded.getTaskById(task.getId()));
        assertEquals(0, loaded.getTasks().size());
    }

    @Test
    void deleteAllTasks() {
        Task task1 = new Task("Task 1", "Description 1", TaskStatus.NEW);
        Task task2 = new Task("Task 2", "Description 2", TaskStatus.DONE);

        manager.createTask(task1);
        manager.createTask(task2);

        assertEquals(2, manager.getTasks().size());
        assertEquals(task1, manager.getTasks().getFirst());
        assertEquals(task2, manager.getTasks().getLast());

        manager.deleteAllTasks();

        assertTrue(manager.getTasks().isEmpty());

        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(file);
        List<Task> loadedTasks = loaded.getTasks();

        assertTrue(loadedTasks.isEmpty());
    }

    @Test
    void loadTaskIntersects() {
        Assertions.assertDoesNotThrow(() -> {
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            writer.append("1,TASK,Task 1,NEW,first,2023-01-01T10:00,60").append("\n");
            writer.append("2,TASK,Task 2,NEW,Conflict,2023-01-01T10:30,45");
            writer.close();
        });

        assertThrows(IllegalArgumentException.class, () -> FileBackedTaskManager.loadFromFile(file));
    }

    @Test
    void loadTaskPrioritized() {
        Task task = new Task("Scheduled Task", "Desc", TaskStatus.NEW,
                LocalDateTime.of(2023, 1, 2, 14, 0), Duration.ofMinutes(45));
        task.setId(1);

        manager.createTask(task);
        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(file);
        assertThrows(NotFoundException.class, () -> loaded.createTask(task));

        assertEquals(1, loaded.getPrioritizedTasks().size());
        assertEquals(task, loaded.getPrioritizedTasks().getFirst());
    }

    //---------------------------------------------------
    //блок тестов методов для Subtask
    //---------------------------------------------------
    @Test
    void saveAndLoadEmptyFile() {
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(file);
        assertTrue(loadedManager.getSubtasks().isEmpty());
    }

    @Test
    void saveAndLoadSingleSubtaskAndEpic() {
        Epic epic = new Epic("Epic", "Epic description");
        manager.createEpic(epic);

        Subtask subtask = new Subtask("Subtask", "Subtask description", TaskStatus.NEW, epic.getId());
        manager.createSubtask(subtask);

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(file);
        List<Subtask> loadedSub = loadedManager.getSubtasks();
        List<Epic> loadedEp = loadedManager.getEpics();

        assertEquals(1, loadedEp.size());
        Epic loadedEpic = loadedEp.getFirst();
        assertEquals(epic.getName(), loadedEpic.getName());
        assertEquals(epic.getDescription(), loadedEpic.getDescription());
        assertEquals(epic.getStatus(), loadedEpic.getStatus());

        assertEquals(1, loadedSub.size());
        Subtask loadedSubtask = loadedSub.getFirst();
        assertEquals(subtask.getName(), loadedSubtask.getName());
        assertEquals(subtask.getDescription(), loadedSubtask.getDescription());
        assertEquals(subtask.getStatus(), loadedSubtask.getStatus());
        assertEquals(subtask.getEpicId(), loadedSubtask.getEpicId());
    }

    @Test
    void saveAndLoadMultipleSubtasks() {
        Epic epic1 = new Epic("Epic", "Epic1 description");
        manager.createEpic(epic1);

        Subtask subtask1 = new Subtask("Subtask1", "Description1", TaskStatus.NEW, epic1.getId());
        Subtask subtask2 = new Subtask("Subtask2", "Description2", TaskStatus.DONE, epic1.getId());
        manager.createSubtask(subtask1);
        manager.createSubtask(subtask2);

        Epic epic2 = new Epic("Epic2", "Epic2 description");
        manager.createEpic(epic2);

        Subtask subtask3 = new Subtask("Subtask3", "Description3", TaskStatus.NEW, epic2.getId());
        Subtask subtask4 = new Subtask("Subtask4", "Description4", TaskStatus.DONE, epic2.getId());
        manager.createSubtask(subtask3);
        manager.createSubtask(subtask4);

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(file);
        List<Subtask> loadedSubs = loadedManager.getSubtasks();

        assertEquals(4, loadedSubs.size());
        assertEquals(subtask1.getName(), loadedSubs.getFirst().getName());
        assertEquals(subtask1.getDescription(), loadedSubs.getFirst().getDescription());
        assertEquals(subtask1.getStatus(), loadedSubs.getFirst().getStatus());
        assertEquals(subtask1.getEpicId(), loadedSubs.getFirst().getEpicId());

        assertEquals(subtask2.getName(), loadedSubs.get(1).getName());
        assertEquals(subtask2.getDescription(), loadedSubs.get(1).getDescription());
        assertEquals(subtask2.getStatus(), loadedSubs.get(1).getStatus());
        assertEquals(subtask2.getEpicId(), loadedSubs.get(1).getEpicId());

        assertEquals(subtask3.getName(), loadedSubs.get(2).getName());
        assertEquals(subtask3.getDescription(), loadedSubs.get(2).getDescription());
        assertEquals(subtask3.getStatus(), loadedSubs.get(2).getStatus());
        assertEquals(subtask3.getEpicId(), loadedSubs.get(2).getEpicId());

        assertEquals(subtask4.getName(), loadedSubs.getLast().getName());
        assertEquals(subtask4.getDescription(), loadedSubs.getLast().getDescription());
        assertEquals(subtask4.getStatus(), loadedSubs.getLast().getStatus());
        assertEquals(subtask4.getEpicId(), loadedSubs.getLast().getEpicId());

        List<Epic> loadedEpics = loadedManager.getEpics();

        assertEquals(2, loadedEpics.size());
        assertEquals(manager.getEpicById(epic1.getId()).getName(), loadedEpics.getFirst().getName());
        assertEquals(manager.getEpicById(epic1.getId()).getDescription(), loadedEpics.getFirst().getDescription());
        assertEquals(manager.getEpicById(epic1.getId()).getStatus(), loadedEpics.getFirst().getStatus());
        assertEquals(manager.getEpicById(epic2.getId()).getName(), loadedEpics.getLast().getName());
        assertEquals(manager.getEpicById(epic2.getId()).getDescription(), loadedEpics.getLast().getDescription());
        assertEquals(manager.getEpicById(epic2.getId()).getStatus(), loadedEpics.getLast().getStatus());
    }

    @Test
    void updateSubtaskAndLoad() {
        Epic epic = new Epic("Epic", "Epic description");
        manager.createEpic(epic);

        Subtask subtask = new Subtask("Subtask", "Description", TaskStatus.NEW, epic.getId());
        manager.createSubtask(subtask);
        subtask.setDescription("Updated");
        subtask.setStatus(TaskStatus.IN_PROGRESS);
        manager.updateSubtask(subtask);

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(file);
        Subtask loaded = loadedManager.getSubtaskById(subtask.getId());

        assertEquals("Updated", loaded.getDescription());
        assertEquals(TaskStatus.IN_PROGRESS, loaded.getStatus());
    }

    @Test
    void deleteSubtaskAndLoad() {
        Epic epic = new Epic("Epic", "Epic description");
        manager.createEpic(epic);

        Subtask subtask = new Subtask("Subtask", "Description", TaskStatus.NEW, epic.getId());
        manager.createSubtask(subtask);
        manager.deleteSubtaskById(subtask.getId());

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(file);
        assertThrows(NotFoundException.class, () -> loadedManager.getSubtaskById(subtask.getId()));
        assertEquals(0, loadedManager.getSubtasks().size());
        assertEquals(0, manager.getEpicById(epic.getId()).getSubtasks().size());
    }

    @Test
    void deleteAllSubtasks() {
        Epic epic = new Epic("Epic", "Description");
        manager.createEpic(epic);

        Subtask sub1 = new Subtask("Sub 1", "Desc 1", TaskStatus.NEW, epic.getId());
        Subtask sub2 = new Subtask("Sub 2", "Desc 2", TaskStatus.DONE, epic.getId());
        manager.createSubtask(sub1);
        manager.createSubtask(sub2);

        assertEquals(2, manager.getSubtasks().size());
        assertEquals(1, manager.getEpics().size());

        manager.deleteAllSubtasks();

        assertTrue(manager.getSubtasks().isEmpty());

        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(file);
        assertTrue(loaded.getSubtasks().isEmpty());
        assertEquals(1, manager.getEpics().size());
        assertEquals(loaded.getEpicById(epic.getId()), manager.getEpicById(epic.getId()));
    }

    @Test
    void loadSubtaskEpicNotExists() {
        Subtask subtask = new Subtask("Subtask", "Desc", TaskStatus.NEW,
                LocalDateTime.of(2023, 1, 1, 12, 0),
                Duration.ofMinutes(30), 999);
        subtask.setId(1);

        assertThrows(NotFoundException.class, () -> manager.createSubtask(subtask));

        assertThrows(NotFoundException.class, () -> assertNull(manager.getSubtaskById(1)));
        assertEquals(0, manager.getPrioritizedTasks().size());

        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(file);

        assertEquals(0, loaded.getPrioritizedTasks().size());
        assertThrows(NotFoundException.class, () -> loaded.getSubtaskById(1));
    }

    @Test
    void loadSubtaskEpicExists() {
        manager.createEpic(epic);

        Subtask subtask = new Subtask("Subtask", "Desc", TaskStatus.NEW,
                LocalDateTime.of(2023, 1, 1, 12, 0),
                Duration.ofMinutes(30), epic.getId());
        subtask.setId(2);

        manager.createSubtask(subtask);

        assertEquals(subtask, manager.getSubtaskById(2));
        assertEquals(1, manager.getEpicsSubtasksById(epic.getId()).size());
        assertEquals(1, manager.getPrioritizedTasks().size());
        assertEquals(subtask, manager.getPrioritizedTasks().getFirst());

        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(file);

        assertEquals(subtask, loaded.getSubtaskById(2));
        assertEquals(1, loaded.getEpicsSubtasksById(epic.getId()).size());
        assertEquals(1, loaded.getPrioritizedTasks().size());
        assertEquals(subtask, loaded.getPrioritizedTasks().getFirst());
    }

    @Test
    void loadSubtaskIntersects() {
        Assertions.assertDoesNotThrow(() -> {
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            writer.append("1,EPIC,epic 1,NEW,first,null,null").append("\n");
            writer.append("2,SUBTASK,Task 2,NEW,Conflict,2023-01-01T12:00,60,1").append("\n");
            writer.append("3,SUBTASK,Task 3,NEW,Conflict,2023-01-01T12:30,45,1");
            writer.close();
        });

        assertThrows(IllegalArgumentException.class, () -> FileBackedTaskManager.loadFromFile(file));
    }

    @Test
    void loadSubtaskPrioritized() {
        manager.createEpic(epic);

        Subtask subtask = new Subtask("Prioritized", "Desc", TaskStatus.NEW,
                LocalDateTime.of(2023, 1, 2, 15, 0),
                Duration.ofMinutes(45), epic.getId());
        subtask.setId(2);

        manager.createSubtask(subtask);

        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(file);

        assertTrue(loaded.getPrioritizedTasks().contains(subtask));
    }

    //---------------------------------------------------
    //блок тестов методов для Epic
    //---------------------------------------------------
    @Test
    void testSaveAndLoadEmptyEpicList() {
        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(file);
        assertTrue(loaded.getEpics().isEmpty());
    }

    @Test
    void testSaveAndLoadEpicWithSubtasks() {
        Epic epic = new Epic("Epic 1", "Description");
        manager.createEpic(epic);

        Subtask sub1 = new Subtask("Sub 1", "Desc 1", TaskStatus.NEW, epic.getId());
        Subtask sub2 = new Subtask("Sub 2", "Desc 2", TaskStatus.DONE, epic.getId());
        manager.createSubtask(sub1);
        manager.createSubtask(sub2);

        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(file);
        Epic loadedEpic = loaded.getEpicById(epic.getId());
        assertNotNull(loadedEpic);
        assertEquals(2, loaded.getEpicsSubtasksById(epic.getId()).size());
    }

    @Test
    void testUpdateEpicAndSave() {
        Epic epic = new Epic("Epic 1", "Description");
        manager.createEpic(epic);
        epic.setName("Updated Epic");
        epic.setDescription("Updated Desc");
        manager.updateEpic(epic);

        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(file);
        Epic loadedEpic = loaded.getEpicById(epic.getId());
        assertEquals("Updated Epic", loadedEpic.getName());
        assertEquals("Updated Desc", loadedEpic.getDescription());
    }

    @Test
    void testRemoveEpicAndSave() {
        Epic epic = new Epic("Epic 1", "Description");
        manager.createEpic(epic);
        manager.deleteEpicById(epic.getId());

        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(file);
        assertThrows(NotFoundException.class, () -> loaded.getEpicById(epic.getId()));
        assertEquals(0, loaded.getEpics().size());
    }

    @Test
    void testEpicWithNoSubtasks() {
        Epic epic = new Epic("Epic No Subs", "No subtasks");
        manager.createEpic(epic);

        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(file);
        Epic loadedEpic = loaded.getEpicById(epic.getId());
        assertNotNull(loadedEpic);
        assertTrue(loaded.getEpicById(epic.getId()).getSubtasks().isEmpty());
        assertEquals(manager.getEpicById(epic.getId()), loaded.getEpicById(epic.getId()));
    }

    @Test
    void testEpicStatusCalculationAfterLoading() {
        Epic epic = new Epic("Status Epic", "Mix statuses");
        manager.createEpic(epic);

        Subtask sub1 = new Subtask("Sub 1", "Desc", TaskStatus.NEW, epic.getId());
        Subtask sub2 = new Subtask("Sub 2", "Desc", TaskStatus.DONE, epic.getId());
        manager.createSubtask(sub1);
        manager.createSubtask(sub2);

        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(file);
        Epic loadedEpic = loaded.getEpicById(epic.getId());
        assertEquals(TaskStatus.IN_PROGRESS, loadedEpic.getStatus());
    }

    @Test
    void shouldThrowManagerSaveExceptionOnReadError() {
        File fakeFile = new File("nonexistent-folder/fake.csv");

        ManagerSaveException exception = assertThrows(
                ManagerSaveException.class,
                () -> FileBackedTaskManager.loadFromFile(fakeFile)
        );

        assertTrue(exception.getMessage().contains("Load file error: "));
    }

    @Test
    void deleteAllEpics() {
        Epic epic1 = new Epic("Epic1", "Description1");
        manager.createEpic(epic1);

        Subtask sub1 = new Subtask("Sub 1", "Desc 1", TaskStatus.NEW, epic1.getId());
        Subtask sub2 = new Subtask("Sub 2", "Desc 2", TaskStatus.DONE, epic1.getId());
        manager.createSubtask(sub1);
        manager.createSubtask(sub2);

        Epic epic2 = new Epic("Epic2", "Description2");
        manager.createEpic(epic2);

        Subtask sub3 = new Subtask("Sub 3", "Desc 3", TaskStatus.NEW, epic2.getId());
        Subtask sub4 = new Subtask("Sub 4", "Desc 4", TaskStatus.DONE, epic2.getId());
        manager.createSubtask(sub3);
        manager.createSubtask(sub4);

        assertEquals(4, manager.getSubtasks().size());
        assertEquals(2, manager.getEpics().size());

        manager.deleteAllSubtasks();

        assertTrue(manager.getSubtasks().isEmpty());

        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(file);

        assertTrue(loaded.getSubtasks().isEmpty());
        assertEquals(2, loaded.getEpics().size());
        assertEquals(loaded.getEpicById(epic1.getId()), manager.getEpicById(epic1.getId()));

        manager.deleteAllEpics();

        assertTrue(manager.getEpics().isEmpty());

        FileBackedTaskManager loaded2 = FileBackedTaskManager.loadFromFile(file);

        assertTrue(loaded2.getEpics().isEmpty());
    }

    @Test
    void shouldThrowManagerSaveExceptionOnSave() {
        manager = new FileBackedTaskManager(new File("D://incorrectFile.txt"));

        Epic epic1 = new Epic("Epic1", "Description1");

        assertThrows(ManagerSaveException.class, () -> manager.createEpic(epic1));
    }

    @Test
    void loadEpicShouldBindSubtasks() {
        manager.createEpic(epic);

        Subtask sub = new Subtask("Subtask", "Desc", TaskStatus.NEW,
                LocalDateTime.of(2023, 1, 1, 10, 0),
                Duration.ofMinutes(30), 1);
        manager.createSubtask(sub);

        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(file);

        assertEquals(1, loaded.getEpicById(1).getSubtasks().size());
        assertEquals(sub, loaded.getEpicById(1).getSubtasks().getFirst());
        assertEquals(1, loaded.getPrioritizedTasks().size());
        assertEquals(sub, loaded.getPrioritizedTasks().getLast());
    }

    @Test
    void loadEpicPrioritized() {
        manager.createEpic(epic);

        Subtask subtask = new Subtask("Subtask", "Desc", TaskStatus.NEW,
                LocalDateTime.of(2023, 1, 1, 12, 0),
                Duration.ofMinutes(30), epic.getId());
        subtask.setId(2);

        manager.createSubtask(subtask);

        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(file);

        assertFalse(loaded.getPrioritizedTasks().contains(epic));
    }
}
