import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class TaskManager {

    private Integer id = 0;
    private final HashMap<Integer,Task> tasks;
    private final HashMap<Integer,Subtask> subtasks;
    private final HashMap<Integer,Epic> epics;

    public TaskManager() {
        this.tasks = new HashMap<>();
        this.subtasks = new HashMap<>();
        this.epics = new HashMap<>();
    }

    private void updateId() {
        id++;
    }

    //---------------------------------------------------
    //блок методов для tasks
    //---------------------------------------------------
    public ArrayList<Task> getTasks() {
        return new ArrayList<>(tasks.values());
    }

    public void deleteAllTasks() {
        tasks.clear();
    }

    public Task getTaskById(Integer id) {
        if (!tasks.containsKey(id)) {
            System.out.println("There's no task with id " + id);
            return null;
        }

        return tasks.get(id);
    }

    public void createTask(Task task) {
        if (tasks.containsKey(task.getId())) {
            System.out.println("task already exists with id = " + task.getId());
            System.out.println("method createTask in TaskManager");
            return;
        }

        updateId();
        task.setId(id);
        tasks.put(id, task);
    }

    public void updateTask(Task task) {
        if (Objects.isNull(task.getId()) || !tasks.containsKey(task.getId())) {
            System.out.println("task does not exist with id = " + task.getId() + ". Please, check if id is correct.");
            System.out.println("method updateTask in TaskManager");
        }

        tasks.put(task.getId(), task);
    }

    public void deleteTaskById(Integer id) {
        if (!tasks.containsKey(id)) {
            System.out.println("There is no task with id = " + id + " in tasks");
            System.out.println("method deleteTaskById in TaskManager");
            return;
        }

        tasks.remove(id);
    }

    //---------------------------------------------------
    //блок методов для subtasks
    //---------------------------------------------------
    public ArrayList<Subtask> getSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    public void deleteAllSubtasks() {
        subtasks.clear();

        for (Epic epic : epics.values()) {
            epic.deleteAllSubtasks();
            checkEpicStatus(epic.getId());
        }
    }

    public Subtask getSubtaskById(Integer id) {
        if (!subtasks.containsKey(id)) {
            System.out.println("There's no subtask with id " + id);
            System.out.println("method getSubtaskById in TaskManager");
            return null;
        }

        return subtasks.get(id);
    }

    public void createSubtask(Subtask subtask) {
        if(!epics.containsKey(subtask.getEpicId())) {
            System.out.println("for this subtask doesn't exist epic with id = " + subtask.getEpicId());
            System.out.println("method createSubtask in TaskManager");
        }

        if (subtasks.containsKey(subtask.getId())) {
            System.out.println("task already exists with id = " + subtask.getId());
            System.out.println("method createSubtask in TaskManager");
            return;
        }

        updateId();
        subtask.setId(id);
        subtasks.put(id, subtask);
        epics.get(subtask.getEpicId()).addSubtask(subtask);
        checkEpicStatus(subtask.getEpicId());
    }

    public void updateSubtask(Subtask subtask) {
        if (Objects.isNull(subtask.getId()) || !subtasks.containsKey(subtask.getId())) {
            System.out.println("task does not exist with id = " + subtask.getId() + ". Please, check if id is correct.");
            System.out.println("method updateSubtask in TaskManager");
            return;
        }

        if (Objects.isNull(subtask.getEpicId()) ||
                !subtask.getEpicId().equals(subtasks.get(subtask.getId()).getEpicId())) {
            System.out.println("Epic id is incorrect");
            System.out.println("method updateSubtask in TaskManager");
            return;
        }

        epics.get(subtask.getEpicId()).replaceSubtask(subtasks.get(subtask.getId()), subtask);
        subtasks.put(subtask.getId(), subtask);
        checkEpicStatus(subtask.getEpicId());
    }

    public void deleteSubtaskById(Integer id) {
        if (!subtasks.containsKey(id)) {
            System.out.println("There is no subtask with id = " + id + " in subtasks");
            System.out.println("method deleteSubtaskById in TaskManager");
            return;
        }

        epics.get(subtasks.get(id).getEpicId()).removeSubtask(subtasks.get(id));
        subtasks.remove(id);
        checkEpicStatus(subtasks.get(id).getEpicId());
    }

    //---------------------------------------------------
    //блок методов для epics
    //---------------------------------------------------
    public ArrayList<Epic> getEpics() {
        return new ArrayList<>(epics.values());
    }

    public void deleteAllEpics() {
        subtasks.clear();
        epics.clear();
    }

    public Epic getEpicById(Integer id) {
        if (!epics.containsKey(id)) {
            System.out.println("There's no epic with id " + id);
            System.out.println("method getEpicById in TaskManager");
            return null;
        }

        return epics.get(id);
    }

    public void createEpic(Epic epic) {
        if (epics.containsKey(epic.getId())) {
            System.out.println("task already exists with id = " + epic.getId());
            System.out.println("method createEpic in TaskManager");
            return;
        }

        updateId();
        epic.setId(id);
        epics.put(id, epic);
    }

    public void updateEpic(Epic epic) {
        if (Objects.isNull(epic.getId()) || !epics.containsKey(epic.getId())) {
            System.out.println("task does not exist with id = " + epic.getId() + ". Please, check if id is correct.");
            System.out.println("method updateEpic in TaskManager");
            return;
        }

        epics.get(epic.getId()).setDescription(epic.getDescription());
        epics.get(epic.getId()).setName(epic.getName());
    }

    public void deleteEpicById(Integer id) {
        if (!epics.containsKey(id)) {
            System.out.println("There is no subtask with id = " + id + " in subtasks");
            System.out.println("method deleteEpicById in TaskManager");
            return;
        }

        for (Subtask subtask : epics.get(id).getSubtasks()) {
           subtasks.remove(subtask.getId());
        }

        epics.remove(id);
    }

    public ArrayList<Subtask> getEpicsSubtasksById (int epicId) {
        if (!epics.containsKey(epicId)) {
            System.out.println("There is no epic with id = " + epicId + " in epics");
            System.out.println("method getEpicsSubtasks in TaskManager");
            return null;
        }

        return epics.get(epicId).getSubtasks();
    }

    //---------------------------------------------------
    //изменение статусов эпиков
    //---------------------------------------------------
    private void checkEpicStatus(Integer id) {
        if (!epics.containsKey(id)) {
            System.out.println("Epic with id=" + id + " does not exist");
            System.out.println("method checkEpicStatus in TaskManager");
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
