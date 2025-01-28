import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class InMemoryTaskManager implements TaskManager {

    private Integer id = 0;
    private final HashMap<Integer,Task> tasks;
    private final HashMap<Integer,Subtask> subtasks;
    private final HashMap<Integer,Epic> epics;
    private final HistoryManager historyManager;

    public InMemoryTaskManager() {
        this.tasks = new HashMap<>();
        this.subtasks = new HashMap<>();
        this.epics = new HashMap<>();
        this.historyManager = Managers.getDefaultHistory();
    }

    private void updateId() {
        id++;
    }

    //---------------------------------------------------
    //блок методов для tasks
    //---------------------------------------------------
    @Override
    public ArrayList<Task> getTasks() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public void deleteAllTasks() {
        tasks.clear();
    }

    @Override
    public Task getTaskById(Integer id) {
        if (!tasks.containsKey(id)) {
            System.out.println("There's no task with id " + id);
            System.out.println("method getTaskById in InMemoryTaskManager");
            return null;
        }

        historyManager.add(tasks.get(id));

        return tasks.get(id);
    }

    @Override
    public void createTask(Task task) {
        if (tasks.containsKey(task.getId())) {
            System.out.println("task already exists with id = " + task.getId());
            System.out.println("method createTask in InMemoryTaskManager");
            return;
        }

        updateId();
        task.setId(id);
        tasks.put(id, task);
    }

    @Override
    public void updateTask(Task task) {
        if (Objects.isNull(task.getId()) || !tasks.containsKey(task.getId())) {
            System.out.println("task does not exist with id = " + task.getId() + ". Please, check if id is correct.");
            System.out.println("method updateTask in InMemoryTaskManager");
            return;
        }

        tasks.put(task.getId(), task);
    }

    @Override
    public void deleteTaskById(Integer id) {
        if (!tasks.containsKey(id)) {
            System.out.println("There is no task with id = " + id + " in tasks");
            System.out.println("method deleteTaskById in InMemoryTaskManager");
            return;
        }

        tasks.remove(id);
    }

    //---------------------------------------------------
    //блок методов для subtasks
    //---------------------------------------------------
    @Override
    public ArrayList<Subtask> getSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    @Override
    public void deleteAllSubtasks() {
        subtasks.clear();

        for (Epic epic : epics.values()) {
            epic.deleteAllSubtasks();
            checkEpicStatus(epic.getId());
        }
    }

    @Override
    public Subtask getSubtaskById(Integer id) {
        if (!subtasks.containsKey(id)) {
            System.out.println("There's no subtask with id " + id);
            System.out.println("method getSubtaskById in InMemoryTaskManager");
            return null;
        }

        historyManager.add(subtasks.get(id));

        return subtasks.get(id);
    }

    @Override
    public void createSubtask(Subtask subtask) {
        if(!epics.containsKey(subtask.getEpicId())) {
            System.out.println("for this subtask doesn't exist epic with id = " + subtask.getEpicId());
            System.out.println("method createSubtask in InMemoryTaskManager");
            return;
        }

        if (subtasks.containsKey(subtask.getId())) {
            System.out.println("subtask already exists with id = " + subtask.getId());
            System.out.println("method createSubtask in InMemoryTaskManager");
            return;
        }

        if (epics.containsKey(subtask.getId()) || tasks.containsKey(subtask.getId())) {
            System.out.println("already exists another task with id = " + subtask.getId());
            System.out.println("method createSubtask in InMemoryTaskManager");
            return;
        }

        updateId();
        subtask.setId(id);
        subtasks.put(id, subtask);
        epics.get(subtask.getEpicId()).addSubtask(subtask);
        checkEpicStatus(subtask.getEpicId());
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        if (Objects.isNull(subtask.getId()) || !subtasks.containsKey(subtask.getId())) {
            System.out.println("task does not exist with id = " + subtask.getId() +
                    ". Please, check if id is correct.");
            System.out.println("method updateSubtask in InMemoryTaskManager");
            return;
        }

        if (Objects.isNull(subtask.getEpicId()) ||
                !subtask.getEpicId().equals(subtasks.get(subtask.getId()).getEpicId())) {
            System.out.println("Epic id is incorrect");
            System.out.println("method updateSubtask in InMemoryTaskManager");
            return;
        }

        epics.get(subtask.getEpicId()).replaceSubtask(subtasks.get(subtask.getId()), subtask);
        subtasks.put(subtask.getId(), subtask);
        checkEpicStatus(subtask.getEpicId());
    }

    @Override
    public void deleteSubtaskById(Integer id) {
        if (!subtasks.containsKey(id)) {
            System.out.println("There is no subtask with id = " + id + " in subtasks");
            System.out.println("method deleteSubtaskById in InMemoryTaskManager");
            return;
        }

        epics.get(subtasks.get(id).getEpicId()).removeSubtask(subtasks.get(id));
        checkEpicStatus(subtasks.get(id).getEpicId());
        subtasks.remove(id);
    }

    //---------------------------------------------------
    //блок методов для epics
    //---------------------------------------------------
    @Override
    public ArrayList<Epic> getEpics() {
        return new ArrayList<>(epics.values());
    }

    @Override
    public void deleteAllEpics() {
        subtasks.clear();
        epics.clear();
    }

    @Override
    public Epic getEpicById(Integer id) {
        if (!epics.containsKey(id)) {
            System.out.println("There's no epic with id " + id);
            System.out.println("method getEpicById in InMemoryTaskManager");
            return null;
        }

        historyManager.add(epics.get(id));

        return epics.get(id);
    }

    @Override
    public void createEpic(Epic epic) {
        if (epics.containsKey(epic.getId())) {
            System.out.println("task already exists with id = " + epic.getId());
            System.out.println("method createEpic in InMemoryTaskManager");
            return;
        }

        updateId();
        epic.setId(id);
        epics.put(id, epic);
    }

    @Override
    public void updateEpic(Epic epic) {
        if (Objects.isNull(epic.getId()) || !epics.containsKey(epic.getId())) {
            System.out.println("task does not exist with id = " + epic.getId() + ". Please, check if id is correct.");
            System.out.println("method updateEpic in InMemoryTaskManager");
            return;
        }

        epics.get(epic.getId()).setDescription(epic.getDescription());
        epics.get(epic.getId()).setName(epic.getName());
    }

    @Override
    public void deleteEpicById(Integer id) {
        if (!epics.containsKey(id)) {
            System.out.println("There is no epic with id = " + id + " in epics");
            System.out.println("method deleteEpicById in InMemoryTaskManager");
            return;
        }

        for (Subtask subtask : epics.get(id).getSubtasks()) {
           subtasks.remove(subtask.getId());
        }

        epics.remove(id);
    }

    @Override
    public ArrayList<Subtask> getEpicsSubtasksById(int epicId) {
        if (!epics.containsKey(epicId)) {
            System.out.println("There is no epic with id = " + epicId + " in epics");
            System.out.println("method getEpicsSubtasks in InMemoryTaskManager");
            return null;
        }

        return epics.get(epicId).getSubtasks();
    }

    //---------------------------------------------------
    //История просмотров задач
    //---------------------------------------------------
    public ArrayList<Task> getHistory() {
        return historyManager.getHistory();
    }

    //---------------------------------------------------
    //изменение статусов эпиков
    //---------------------------------------------------
    private void checkEpicStatus(Integer id) {
        if (!epics.containsKey(id)) {
            System.out.println("Epic with id=" + id + " does not exist");
            System.out.println("method checkEpicStatus in InMemoryTaskManager");
            return;
        }

        TaskStatus allNew = TaskStatus.NEW;
        TaskStatus allDone = TaskStatus.DONE;

        for (Subtask subtask : epics.get(id).getSubtasks()) {
            if (subtask.getStatus() != TaskStatus.NEW) {
                allNew = subtask.getStatus();
            }
            if (subtask.getStatus() != TaskStatus.DONE) {
                allDone = subtask.getStatus();
            }
        }

        if (allNew == TaskStatus.NEW) {
            epics.get(id).setStatus(TaskStatus.NEW);
        } else if (allDone == TaskStatus.DONE) {
            epics.get(id).setStatus(TaskStatus.DONE);
        } else {
            epics.get(id).setStatus(TaskStatus.IN_PROGRESS);
        }
    }

    //---------------------------------------------------
    //переопределение toString
    //---------------------------------------------------
    @Override
    public String toString() {
        return "TaskManager{" +
                "id=" + id +
                ", tasks=" + tasks +
                ", subtasks=" + subtasks +
                ", epics=" + epics +
                '}';
    }
}
