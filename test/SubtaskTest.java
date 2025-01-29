import model.Epic;
import model.Subtask;
import model.TaskStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SubtaskTest {

    private Subtask subtask;
    private Epic epic;

    @BeforeEach
    public void beforeEach() {
        epic = new Epic("testNameEpic", "testDescriptionEpic");
    }

    @Test
    void getEpicIdWhenNull() {
        subtask = new Subtask("testName", "testDescription", TaskStatus.IN_PROGRESS, epic.getId());
        assertNull(subtask.getEpicId());
    }

    @Test
    void getEpicIdWhenNotNull() {
        epic.setId(1);
        subtask = new Subtask("testName", "testDescription", TaskStatus.IN_PROGRESS, epic.getId());
        epic.addSubtask(subtask);
        assertNotNull(subtask.getEpicId());
    }

    @Test
    void testSubtaskEquality() {
        epic.setId(1);
        subtask = new Subtask("testName", "testDescription", TaskStatus.IN_PROGRESS, epic.getId());
        subtask.setId(2);
        epic.addSubtask(subtask);
        Subtask subtask2 = new Subtask("testName2", "testDescription2", TaskStatus.NEW, epic.getId());
        subtask2.setId(subtask.getId());

        assertEquals(subtask, subtask2);
        assertEquals(subtask, subtask);

        Epic epic2 = new Epic("testNameEpic", "testDescriptionEpic");
        epic2.setId(3);
        Subtask subtask3 = new Subtask("testName2", "testDescription2", TaskStatus.NEW, epic2.getId());
        subtask3.setId(4);
        epic2.addSubtask(subtask2);

        assertNotEquals(subtask, subtask3);
        assertNotEquals(subtask,null);
        assertNotEquals(subtask,epic);
    }

    @Test
    void testSubtaskCannotBeItsOwnEpic() {
        epic.setId(2);
        subtask = new Subtask("testName", "testDescription", TaskStatus.IN_PROGRESS, 2);
        subtask.setId(2);
        epic.addSubtask(subtask);

        assertEquals(0, epic.getSubtasks().size());
    }

}