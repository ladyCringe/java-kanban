package service;

import model.Epic;
import model.Subtask;
import model.Task;
import model.TaskStatus;

import java.util.*;

public class InMemoryTaskManager implements TaskManager {

    private Integer id = 0;
    private final Map<Integer, Task> tasks;
    private final Map<Integer, Subtask> subtasks;
    private final Map<Integer, Epic> epics;
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

    protected void setId(int id) {
        this.id = id;
    }

    //---------------------------------------------------
    //блок методов для tasks
    //---------------------------------------------------
    @Override
    public List<Task> getTasks() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public void deleteAllTasks() {
        for (Task t : tasks.values()) {
            historyManager.remove(t.getId());
        }

        tasks.clear();
    }

    @Override
    public Task getTaskById(Integer id) {
        if (!tasks.containsKey(id)) {
            System.out.println("There's no task with id " + id);
            System.out.println("method getTaskById in service.InMemoryTaskManager");
            return null;
        }

        historyManager.add(tasks.get(id));

        return tasks.get(id).cloneTask();
    }

    @Override
    public void createTask(Task task) {
        if (tasks.containsKey(task.getId())) {
            System.out.println("task already exists with id = " + task.getId());
            System.out.println("method createTask in service.InMemoryTaskManager");
            return;
        }

        updateId();
        task.setId(id);
        tasks.put(id, task.cloneTask());
    }

    @Override
    public void updateTask(Task task) {
        if (Objects.isNull(task.getId()) || !tasks.containsKey(task.getId())) {
            System.out.println("task does not exist with id = " + task.getId() + ". Please, check if id is correct.");
            System.out.println("method updateTask in service.InMemoryTaskManager");
            return;
        }

        tasks.put(task.getId(), task.cloneTask());
        historyManager.update(task.getId(), task.cloneTask());
    }

    @Override
    public void deleteTaskById(Integer id) {
        if (!tasks.containsKey(id)) {
            System.out.println("There is no task with id = " + id + " in tasks");
            System.out.println("method deleteTaskById in service.InMemoryTaskManager");
            return;
        }

        historyManager.remove(id);
        tasks.remove(id);
    }

    protected void loadTask(Task task) {
        tasks.put(task.getId(), task);
    }

    //---------------------------------------------------
    //блок методов для subtasks
    //---------------------------------------------------
    @Override
    public List<Subtask> getSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    @Override
    public void deleteAllSubtasks() {
        for (Subtask s : subtasks.values()) {
            historyManager.remove(s.getId());
        }
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
            System.out.println("method getSubtaskById in service.InMemoryTaskManager");
            return null;
        }

        historyManager.add(subtasks.get(id));

        return subtasks.get(id).cloneSubtask();
    }

    @Override
    public void createSubtask(Subtask subtask) {
        if (!epics.containsKey(subtask.getEpicId())) {
            System.out.println("for this subtask doesn't exist epic with id = " + subtask.getEpicId());
            System.out.println("method createSubtask in service.InMemoryTaskManager");
            return;
        }

        if (subtasks.containsKey(subtask.getId())) {
            System.out.println("subtask already exists with id = " + subtask.getId());
            System.out.println("method createSubtask in service.InMemoryTaskManager");
            return;
        }

        if (epics.containsKey(subtask.getId()) || tasks.containsKey(subtask.getId())) {
            System.out.println("already exists another task with id = " + subtask.getId());
            System.out.println("method createSubtask in service.InMemoryTaskManager");
            return;
        }

        updateId();
        subtask.setId(id);
        subtasks.put(id, subtask.cloneSubtask());
        epics.get(subtask.getEpicId()).addSubtask(subtask.cloneSubtask());
        checkEpicStatus(subtask.getEpicId());
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        if (Objects.isNull(subtask.getId()) || !subtasks.containsKey(subtask.getId())) {
            System.out.println("task does not exist with id = " + subtask.getId() +
                    ". Please, check if id is correct.");
            System.out.println("method updateSubtask in service.InMemoryTaskManager");
            return;
        }

        if (Objects.isNull(subtask.getEpicId()) ||
                !subtask.getEpicId().equals(subtasks.get(subtask.getId()).getEpicId())) {
            System.out.println("model.Epic id is incorrect");
            System.out.println("method updateSubtask in service.InMemoryTaskManager");
            return;
        }

        epics.get(subtask.getEpicId()).replaceSubtask(subtasks.get(subtask.getId()), subtask);
        subtasks.put(subtask.getId(), subtask.cloneSubtask());
        checkEpicStatus(subtask.getEpicId());
        historyManager.update(subtask.getId(), subtask.cloneSubtask());
    }

    @Override
    public void deleteSubtaskById(Integer id) {
        if (!subtasks.containsKey(id)) {
            System.out.println("There is no subtask with id = " + id + " in subtasks");
            System.out.println("method deleteSubtaskById in service.InMemoryTaskManager");
            return;
        }

        epics.get(subtasks.get(id).getEpicId()).removeSubtask(subtasks.get(id));
        checkEpicStatus(subtasks.get(id).getEpicId());
        historyManager.remove(id);
        subtasks.remove(id);
    }

    protected void loadSubtask(Subtask subtask) {
        subtasks.put(subtask.getId(), subtask);
        Epic epic = epics.get(subtask.getEpicId());

        if (epic != null) {
            epic.addSubtask(subtask);
            checkEpicStatus(epic.getId());
        }
    }

    //---------------------------------------------------
    //блок методов для epics
    //---------------------------------------------------
    @Override
    public List<Epic> getEpics() {
        return new ArrayList<>(epics.values());
    }

    @Override
    public void deleteAllEpics() {
        for (Subtask sub : subtasks.values()) {
            historyManager.remove(sub.getId());
        }

        for (Epic epic : epics.values()) {
            historyManager.remove(epic.getId());
        }

        subtasks.clear();
        epics.clear();
    }

    @Override
    public Epic getEpicById(Integer id) {
        if (!epics.containsKey(id)) {
            System.out.println("There's no epic with id " + id);
            System.out.println("method getEpicById in service.InMemoryTaskManager");
            return null;
        }

        historyManager.add(epics.get(id));

        return epics.get(id).cloneEpic();
    }

    @Override
    public void createEpic(Epic epic) {
        if (epics.containsKey(epic.getId())) {
            System.out.println("task already exists with id = " + epic.getId());
            System.out.println("method createEpic in service.InMemoryTaskManager");
            return;
        }

        updateId();
        epic.setId(id);
        epics.put(id, epic.cloneEpic());
    }

    @Override
    public void updateEpic(Epic epic) {
        if (Objects.isNull(epic.getId()) || !epics.containsKey(epic.getId())) {
            System.out.println("task does not exist with id = " + epic.getId() + ". Please, check if id is correct.");
            System.out.println("method updateEpic in service.InMemoryTaskManager");
            return;
        }

        epics.get(epic.getId()).setDescription(epic.getDescription());
        epics.get(epic.getId()).setName(epic.getName());
        historyManager.update(epic.getId(), epic.cloneTask());
    }

    @Override
    public void deleteEpicById(Integer id) {
        if (!epics.containsKey(id)) {
            System.out.println("There is no epic with id = " + id + " in epics");
            System.out.println("method deleteEpicById in service.InMemoryTaskManager");
            return;
        }

        for (Subtask subtask : epics.get(id).getSubtasks()) {
            historyManager.remove(subtask.getId());
            subtasks.remove(subtask.getId());
        }

        historyManager.remove(id);
        epics.remove(id);
    }

    protected void loadEpic(Epic epic) {
        epics.put(epic.getId(), epic);

        for (Subtask subtask : subtasks.values()) {
            if (subtask.getId().equals(epic.getId())) {
                epics.get(epic.getId()).addSubtask(subtask);
            }
        }
    }

    @Override
    public List<Subtask> getEpicsSubtasksById(int epicId) {
        if (!epics.containsKey(epicId)) {
            System.out.println("There is no epic with id = " + epicId + " in epics");
            System.out.println("method getEpicsSubtasks in service.InMemoryTaskManager");
            return null;
        }

        return new ArrayList<>(epics.get(epicId).getSubtasks());
    }

    //---------------------------------------------------
    //История просмотров задач
    //---------------------------------------------------
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    //---------------------------------------------------
    //изменение статусов эпиков
    //---------------------------------------------------
    private void checkEpicStatus(Integer id) {
        if (!epics.containsKey(id)) {
            System.out.println("model.Epic with id=" + id + " does not exist");
            System.out.println("method checkEpicStatus in service.InMemoryTaskManager");
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
        return "service.TaskManager{" +
                "id=" + id +
                ", tasks=" + tasks +
                ", subtasks=" + subtasks +
                ", epics=" + epics +
                '}';
    }
}
