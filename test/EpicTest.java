import model.Epic;
import model.Subtask;
import model.TaskStatus;
import model.TaskType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import service.Managers;
import service.TaskManager;

import java.time.Duration;
import java.time.LocalDateTime;
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
        subtask = new Subtask("SubtaskName", "SubtaskDescription", TaskStatus.NEW, epic.getId());
    }

    @Test
    void addSubtask() {
        epic.addSubtask(subtask);
        assertEquals(subtask, epic.getSubtasks().getLast());
    }

    @Test
    void removeSubtask() {
        manager.createSubtask(subtask);
        assertEquals(1, manager.getEpicById(epic.getId()).getSubtasks().size());

        Subtask subtask1 = new Subtask("SubtaskName1",
                "SubtaskDescription1", TaskStatus.DONE, epic.getId());
        epic = manager.getEpicById(epic.getId());
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
        int index = manager.getEpicById(epic.getId()).getSubtasks().indexOf(subtask);
        Subtask subtask1 = new Subtask("SubtaskName1",
                "SubtaskDescription1", TaskStatus.DONE, epic.getId());
        subtask1.setId(subtask.getId());

        epic.replaceSubtask(subtask, subtask1);
        assertEquals(subtask1, manager.getEpicById(epic.getId()).getSubtasks().get(index));

        Epic newEpic = new Epic("testName", "testDescription");
        manager.createEpic(newEpic);
        Subtask subtask2 = new Subtask("SubtaskName2",
                "SubtaskDescription2", TaskStatus.DONE, newEpic.getId());
        manager.createSubtask(subtask2);
        epic = manager.getEpicById(epic.getId());
        epic.replaceSubtask(subtask1, subtask2);
        assertNotEquals(subtask2, manager.getEpicById(epic.getId()).getSubtasks().get(index));

        epic.replaceSubtask(subtask2, subtask1);
        assertNotEquals(subtask2, epic.getSubtasks().get(index));
    }

    @Test
    void testEpicCannotContainItself() {
        subtask.setId(epic.getId());
        epic.addSubtask(subtask);

        assertEquals(0, epic.getSubtasks().size());
    }

    @Test
    void getEndTimeWithNoSubtasks() {
        assertNull(epic.getEndTime());
    }

    @Test
    void getEndTimeWithSubtasks() {
        Subtask sub1 = new Subtask("Sub 1", "Desc 1", TaskStatus.NEW,
                LocalDateTime.of(2024, 3, 30, 10, 0),
                Duration.ofMinutes(30), epic.getId());
        sub1.setId(2);

        Subtask sub2 = new Subtask("Sub 2", "Desc 2", TaskStatus.DONE,
                LocalDateTime.of(2024, 3, 30, 12, 0),
                Duration.ofMinutes(45), epic.getId());
        sub2.setId(3);

        Subtask sub3 = new Subtask("Sub 2", "Desc 2", TaskStatus.DONE,
                LocalDateTime.of(2024, 3, 30, 9, 0),
                Duration.ofMinutes(15), epic.getId());
        sub2.setId(4);

        epic.addSubtask(sub1);
        epic.addSubtask(sub2);
        epic.addSubtask(sub2);
        epic.addSubtask(sub3);

        LocalDateTime expectedEnd = sub2.getStartTime().plus(sub2.getDuration());
        LocalDateTime expectedStart = sub3.getStartTime();

        assertEquals(3, epic.getSubtasks().size());
        assertEquals(expectedEnd, epic.getEndTime());
        assertEquals(expectedStart, epic.getStartTime());
    }

    @Test
    void getEndTimeSkipsNulls() {
        Subtask sub1 = new Subtask("Sub 1", "Desc 1", TaskStatus.NEW,
                null, Duration.ofMinutes(30), epic.getId());
        sub1.setId(2);

        Subtask sub2 = new Subtask("Sub 2", "Desc 2", TaskStatus.NEW,
                LocalDateTime.of(2024, 3, 31, 10, 0),
                Duration.ofMinutes(90), epic.getId());
        sub2.setId(3);

        epic.addSubtask(sub1);
        epic.addSubtask(sub2);

        assertEquals(sub2.getEndTime(), epic.getEndTime());
    }

    @Test
    void testSetEndTime() {
        LocalDateTime manualEnd = LocalDateTime.of(2030, 1, 1, 0, 0);
        epic.setEndTime(manualEnd);

        assertNotNull(epic.getEndTime());
        assertEquals(manualEnd, epic.getEndTime());
    }

    @Test
    void getType() {
        assertEquals(TaskType.EPIC, epic.getType());
    }

    @Test
    void getDurationWithoutSubtasks() {
        assertEquals(Duration.ZERO, epic.getDuration());
    }

    @Test
    void getDurationWithSubtasks() {
        Subtask sub1 = new Subtask("Sub1", "Desc1", TaskStatus.NEW,
                LocalDateTime.of(2024, 1, 1, 10, 0),
                Duration.ofMinutes(30), epic.getId());
        sub1.setId(2);

        Subtask sub2 = new Subtask("Sub2", "Desc2", TaskStatus.NEW,
                LocalDateTime.of(2024, 1, 1, 11, 0),
                Duration.ofMinutes(90), epic.getId());
        sub2.setId(3);

        epic.addSubtask(sub1);
        epic.addSubtask(sub2);

        Duration expected = Duration.ofMinutes(120);
        assertEquals(expected, epic.getDuration());
    }
}