package service;

import model.Epic;
import model.Subtask;
import model.Task;
import model.TaskStatus;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

public class InMemoryTaskManager implements TaskManager {

    private Integer id = 0;
    private final Map<Integer, Task> tasks;
    private final Map<Integer, Subtask> subtasks;
    private final Map<Integer, Epic> epics;
    private final HistoryManager historyManager;
    private final Set<Task> prioritizedTasks = new TreeSet<>(
            Comparator.comparing(Task::getStartTime)
                    .thenComparing(Task::getId)
    );

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
        tasks.values().forEach(t -> historyManager.remove(t.getId()));
        prioritizedTasks.removeAll(tasks.values());
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

        if (hasIntersections(task)) {
            throw new IllegalArgumentException("Task intersects with existing task: "
                    + "method createTask in service.InMemoryTaskManager");
        }

        updateId();
        task.setId(id);

        tasks.put(id, task.cloneTask());
        if (task.getStartTime() != null) {
            prioritizedTasks.add(tasks.get(task.getId()));
        }
    }

    @Override
    public void updateTask(Task task) {
        if (Objects.isNull(task.getId()) || !tasks.containsKey(task.getId())) {
            System.out.println("task does not exist with id = " + task.getId() + ". Please, check if id is correct.");
            System.out.println("method updateTask in service.InMemoryTaskManager");
            return;
        }

        if (task.getStartTime() == null && tasks.get(task.getId()).getStartTime() != null) {
            task.setStartTime(tasks.get(task.getId()).getStartTime());
        }
        if (task.getDuration() == null && tasks.get(task.getId()).getDuration() != null) {
            task.setDuration(tasks.get(task.getId()).getDuration());
        }
        if (hasIntersections(task)) {
            throw new IllegalArgumentException("Task intersects with existing task: "
                    + "method updateTask in service.InMemoryTaskManager");
        }

        tasks.put(task.getId(), task.cloneTask());
        historyManager.update(task.getId(), task.cloneTask());
        if (task.getStartTime() != null) {
            prioritizedTasks.add(tasks.get(task.getId()));
        }
    }

    @Override
    public void deleteTaskById(Integer id) {
        if (!tasks.containsKey(id)) {
            System.out.println("There is no task with id = " + id + " in tasks");
            System.out.println("method deleteTaskById in service.InMemoryTaskManager");
            return;
        }

        historyManager.remove(id);
        prioritizedTasks.remove(tasks.get(id));
        tasks.remove(id);
    }

    protected void loadTask(Task task) {
        if (hasIntersections(task)) {
            throw new IllegalArgumentException("Task intersects with existing task: "
                    + "method loadTask in service.InMemoryTaskManager");
        }

        tasks.put(task.getId(), task);
        if (task.getStartTime() != null) {
            prioritizedTasks.add(tasks.get(task.getId()));
        }
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
        subtasks.values().forEach(s -> historyManager.remove(s.getId()));
        subtasks.clear();

        epics.values().forEach(epic -> {
            epic.deleteAllSubtasks();
            checkEpicStatus(epic.getId());
        });

        prioritizedTasks.removeAll(subtasks.values());
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

        if (hasIntersections(subtask)) {
            throw new IllegalArgumentException("Subtask intersects with existing task: "
                    + "method createSubtask in service.InMemoryTaskManager");
        }

        updateId();
        subtask.setId(id);

        subtasks.put(id, subtask.cloneSubtask());
        epics.get(subtask.getEpicId()).addSubtask(subtask.cloneSubtask());
        checkEpicStatus(subtask.getEpicId());
        checkEpicTime(subtask.getEpicId());

        if (subtask.getStartTime() != null) {
            prioritizedTasks.add(subtasks.get(subtask.getId()));
        }
        if (epics.get(subtask.getEpicId()).getStartTime() != null) {
            prioritizedTasks.add(epics.get(subtask.getEpicId()));
        }
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

        if (subtask.getStartTime() == null && subtasks.get(subtask.getId()).getStartTime() != null) {
            subtask.setStartTime(subtasks.get(subtask.getId()).getStartTime());
        }
        if (subtask.getDuration() == null && subtasks.get(subtask.getId()).getDuration() != null) {
            subtask.setDuration(subtasks.get(subtask.getId()).getDuration());
        }
        if (hasIntersections(subtask)) {
            throw new IllegalArgumentException("Subtask intersects with existing task: "
                    + "method updateSubtask in service.InMemoryTaskManager");
        }

        epics.get(subtask.getEpicId()).replaceSubtask(subtasks.get(subtask.getId()), subtask);
        subtasks.put(subtask.getId(), subtask.cloneSubtask());
        checkEpicStatus(subtask.getEpicId());
        checkEpicTime(subtask.getEpicId());
        historyManager.update(subtask.getId(), subtask.cloneSubtask());
        if (subtask.getStartTime() != null) {
            prioritizedTasks.add(subtasks.get(subtask.getId()));
        }
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
        checkEpicTime(subtasks.get(id).getEpicId());
        historyManager.remove(id);
        prioritizedTasks.remove(subtasks.get(id));
        subtasks.remove(id);
    }

    protected void loadSubtask(Subtask subtask) {
        if (!epics.containsKey(subtask.getEpicId())) {
            System.out.println("for this subtask doesn't exist epic with id = " + subtask.getEpicId());
            System.out.println("method loadSubtask in service.InMemoryTaskManager");
            return;
        }

        subtasks.put(subtask.getId(), subtask);
        Epic epic = epics.get(subtask.getEpicId());
        if (epic != null) {
            epic.addSubtask(subtask);
            checkEpicStatus(epic.getId());
            checkEpicTime(subtask.getEpicId());
        }

        if (hasIntersections(subtask)) {
            throw new IllegalArgumentException("Task intersects with existing task: "
                    + "method loadSubtask in service.InMemoryTaskManager");
        }

        if (subtask.getStartTime() != null) {
            prioritizedTasks.add(subtasks.get(subtask.getId()));
            prioritizedTasks.add(epics.get(subtask.getEpicId()));
        }

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
        subtasks.values().forEach(sub -> historyManager.remove(sub.getId()));
        epics.values().forEach(epic -> historyManager.remove(epic.getId()));
        prioritizedTasks.removeAll(epics.values());
        prioritizedTasks.removeAll(subtasks.values());
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
        if (hasIntersections(epic)) {
            throw new IllegalArgumentException("Epic intersects with existing task: "
                    + "method createEpic in service.InMemoryTaskManager");
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
        if (epics.get(epic.getId()).getStartTime() != null) {
            prioritizedTasks.add(epics.get(epic.getId()));
        }
    }

    @Override
    public void deleteEpicById(Integer id) {
        if (!epics.containsKey(id)) {
            System.out.println("There is no epic with id = " + id + " in epics");
            System.out.println("method deleteEpicById in service.InMemoryTaskManager");
            return;
        }

        epics.get(id).getSubtasks().forEach(subtask -> {
            historyManager.remove(subtask.getId());
            subtasks.remove(subtask.getId());
            prioritizedTasks.remove(subtask);
        });

        prioritizedTasks.remove(epics.get(id));
        historyManager.remove(id);
        epics.remove(id);
    }

    protected void loadEpic(Epic epic) {
        epics.put(epic.getId(), epic);

        subtasks.values().stream()
                .filter(subtask -> subtask.getEpicId().equals(epic.getId()))
                .forEach(subtask -> epics.get(epic.getId()).addSubtask(subtask));

        if (hasIntersections(epic)) {
            throw new IllegalArgumentException("Task intersects with existing task: "
                    + "method loadEpic in service.InMemoryTaskManager");
        }

        if (epic.getStartTime() != null) {
            prioritizedTasks.add(epics.get(epic.getId()));
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

    private void checkEpicTime(Integer id) {
        if (!epics.containsKey(id)) {
            System.out.println("model.Epic with id=" + id + " does not exist");
            System.out.println("method checkEpicStatus in service.InMemoryTaskManager");
            return;
        }

        epics.get(id).setStartTime(getSubtasks().stream()
                .filter(subtask -> subtask.getStartTime() != null)
                .map(Task::getStartTime)
                .min(Comparator.naturalOrder())
                .orElse(null));

        epics.get(id).setDuration(getSubtasks().stream()
                .filter(subtask -> subtask.getDuration() != null)
                .map(Task::getDuration)
                .reduce(Duration.ZERO, Duration::plus));

        if (epics.get(id).getStartTime() == null) {
            return;
        }
        epics.get(id).setEndTime(epics.get(id).getSubtasks().stream()
                .map(Subtask::getEndTime)
                .filter(Objects::nonNull)
                .max(LocalDateTime::compareTo)
                .orElse(null));

    }

    //---------------------------------------------------
    //блок проверки на пересечение
    //---------------------------------------------------
    public ArrayList<Task> getPrioritizedTasks() {
        return new ArrayList<>(prioritizedTasks);
    }

    private boolean areOverlapping(Task a, Task b) {
        if (a.getStartTime() == null || b.getStartTime() == null) {
            return false;
        }

        LocalDateTime startOfA = a.getStartTime();
        LocalDateTime startOfB = b.getStartTime();
        LocalDateTime endOfA = a.getEndTime();
        LocalDateTime endOfB = b.getEndTime();
        return startOfA.isBefore(endOfB) && startOfB.isBefore(endOfA);
    }

    private boolean hasIntersections(Task task) {
        return getPrioritizedTasks().stream()
                .filter(t -> !t.getId().equals(task.getId()))
                .anyMatch(t -> areOverlapping(task, t));
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
