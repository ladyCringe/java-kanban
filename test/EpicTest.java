import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class EpicTest {

    private Epic epic;
    private TaskManager manager;
    private Subtask subtask;

    @BeforeEach
    public void beforeEach() {
        epic = new Epic("testName", "testDescription");
        manager = Managers.getDefault();
        manager.createEpic(epic);
        subtask = new Subtask("SubtaskName", "SubtaskDescription",TaskStatus.NEW, epic.getId());
    }

    @Test
    void addSubtask() {
        epic.addSubtask(subtask);
        assertEquals(subtask, epic.getSubtasks().getLast());
    }

    @Test
    void removeSubtask() {
        manager.createSubtask(subtask);
        assertEquals(1, epic.getSubtasks().size());

        Subtask subtask1 = new Subtask("SubtaskName1",
                "SubtaskDescription1", TaskStatus.DONE, epic.getId());
        epic.removeSubtask(subtask1);
        assertEquals(1, epic.getSubtasks().size());

        epic.removeSubtask(subtask);
        assertEquals(0, epic.getSubtasks().size());
    }

    @Test
    void getSubtasks() {
        epic.addSubtask(subtask);
        ArrayList<Subtask> testSubtasks = new ArrayList<>();
        testSubtasks.add(subtask);
        assertEquals(testSubtasks, epic.getSubtasks());
    }

    @Test
    void deleteAllSubtasks() {
        epic.addSubtask(subtask);
        Subtask subtask1 = new Subtask("SubtaskName1",
                "SubtaskDescription1", TaskStatus.DONE, epic.getId());
        epic.addSubtask(subtask1);
        epic.deleteAllSubtasks();
        assertEquals(0, epic.getSubtasks().size());
    }

    @Test
    void replaceSubtask() {
        manager.createSubtask(subtask);
        int index = epic.getSubtasks().indexOf(subtask);
        Subtask subtask1 = new Subtask("SubtaskName1",
                "SubtaskDescription1", TaskStatus.DONE, epic.getId());
        subtask1.setId(subtask.getId());

        epic.replaceSubtask(subtask, subtask1);
        assertEquals(subtask1, epic.getSubtasks().get(index));

        Epic newEpic = new Epic("testName", "testDescription");
        manager.createEpic(newEpic);
        Subtask subtask2 = new Subtask("SubtaskName2",
                "SubtaskDescription2", TaskStatus.DONE, newEpic.getId());
        manager.createSubtask(subtask2);
        epic.replaceSubtask(subtask1, subtask2);
        assertNotEquals(subtask2, epic.getSubtasks().get(index));

        epic.replaceSubtask(subtask2, subtask1);
        assertNotEquals(subtask2, epic.getSubtasks().get(index));
    }

    @Test
    void testEpicCannotContainItself() {
        subtask.setId(epic.getId());
        epic.addSubtask(subtask);

        assertEquals(0, epic.getSubtasks().size());
    }

}