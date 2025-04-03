import model.Epic;
import model.Subtask;
import model.TaskStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import service.Managers;
import service.TaskManager;

import static org.junit.jupiter.api.Assertions.*;

public class EpicStatusTest {

    private TaskManager manager;
    private Epic epic;

    @BeforeEach
    void setUp() {
        manager = Managers.getDefault();
        epic = new Epic("Epic", "With subtasks");
        manager.createEpic(epic);
    }

    @Test
    void statusAllNew() {
        manager.createSubtask(new Subtask("sub1", "description", TaskStatus.NEW, epic.getId()));
        manager.createSubtask(new Subtask("sub2", "description", TaskStatus.NEW, epic.getId()));

        assertEquals(2, manager.getSubtasks().size());
        assertEquals(1, manager.getEpics().size());
        assertEquals(2, manager.getEpicById(epic.getId()).getSubtasks().size());
        assertEquals(TaskStatus.NEW, manager.getEpicById(epic.getId()).getStatus());
    }

    @Test
    void statusAllInProgress() {
        manager.createSubtask(new Subtask("sub1", "description", TaskStatus.IN_PROGRESS, epic.getId()));
        manager.createSubtask(new Subtask("sub2", "description", TaskStatus.IN_PROGRESS, epic.getId()));

        assertEquals(2, manager.getSubtasks().size());
        assertEquals(1, manager.getEpics().size());
        assertEquals(2, manager.getEpicById(epic.getId()).getSubtasks().size());
        assertEquals(TaskStatus.IN_PROGRESS, manager.getEpicById(epic.getId()).getStatus());
    }

    @Test
    void statusAllDone() {
        manager.createSubtask(new Subtask("sub1", "description", TaskStatus.DONE, epic.getId()));
        manager.createSubtask(new Subtask("sub2", "description", TaskStatus.DONE, epic.getId()));

        assertEquals(2, manager.getSubtasks().size());
        assertEquals(1, manager.getEpics().size());
        assertEquals(2, manager.getEpicById(epic.getId()).getSubtasks().size());
        assertEquals(TaskStatus.DONE, manager.getEpicById(epic.getId()).getStatus());
    }

    @Test
    void statusNewAndDone() {
        manager.createSubtask(new Subtask("sub1", "description", TaskStatus.NEW, epic.getId()));
        manager.createSubtask(new Subtask("sub2", "description", TaskStatus.DONE, epic.getId()));

        assertEquals(2, manager.getSubtasks().size());
        assertEquals(1, manager.getEpics().size());
        assertEquals(2, manager.getEpicById(epic.getId()).getSubtasks().size());
        assertEquals(TaskStatus.IN_PROGRESS, manager.getEpicById(epic.getId()).getStatus());
    }

    @Test
    void statusAtLeastOneInProgress() {
        manager.createSubtask(new Subtask("sub1", "description", TaskStatus.NEW, epic.getId()));
        manager.createSubtask(new Subtask("sub2", "description", TaskStatus.IN_PROGRESS, epic.getId()));
        manager.createSubtask(new Subtask("sub3", "description", TaskStatus.DONE, epic.getId()));

        assertEquals(3, manager.getSubtasks().size());
        assertEquals(1, manager.getEpics().size());
        assertEquals(3, manager.getEpicById(epic.getId()).getSubtasks().size());
        assertEquals(TaskStatus.IN_PROGRESS, manager.getEpicById(epic.getId()).getStatus());
    }
}
