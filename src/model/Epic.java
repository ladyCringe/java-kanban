package model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Objects;

import static model.TaskType.EPIC;

public class Epic extends Task {

    private final ArrayList<Subtask> subtasks;
    private LocalDateTime endTime;

    //---------------------------------------------------
    //конструктор
    //---------------------------------------------------
    public Epic(String name, String description) {
        super(name, description, TaskStatus.NEW);
        this.subtasks = new ArrayList<>();
    }

    //---------------------------------------------------
    //блок геттеров и сеттеров
    //---------------------------------------------------
    public TaskType getType() {
        return EPIC;
    }

    @Override
    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    //---------------------------------------------------
    //блок методов для Subtask
    //---------------------------------------------------
    public void addSubtask(Subtask subtask) {
        if (subtasks.contains(subtask)) {
            System.out.println("model.Subtask already exists here");
            System.out.println("method addSubtask in model.Epic");
            return;
        }

        if (this.getId().equals(subtask.getId())) {
            System.out.println("epic has the same id");
            System.out.println("method addSubtask in model.Epic");
            return;
        }

        subtasks.add(subtask);

        setEndTime(getSubtasks().stream()
                .map(Subtask::getEndTime)
                .filter(Objects::nonNull)
                .max(LocalDateTime::compareTo)
                .orElse(null));
    }

    public void removeSubtask(Subtask subtask) {
        if (!subtasks.contains(subtask)) {
            System.out.println("model.Subtask doesn't exists here");
            System.out.println("method removeSubtask in model.Epic");
            return;
        }
        subtasks.remove(subtask);
    }

    public ArrayList<Subtask> getSubtasks() {
        return new ArrayList<>(subtasks);
    }

    public void deleteAllSubtasks() {
        subtasks.clear();
    }

    public void replaceSubtask(Subtask subtaskBefore, Subtask subtaskAfter) {
        if (!subtasks.contains(subtaskBefore)) {
            System.out.println("There is no subtask with id = " + subtaskBefore.getId() + " in subtasks");
            System.out.println("method replaceSubtask in model.Epic");
            return;
        }

        if (!subtaskBefore.getId().equals(subtaskAfter.getId())) {
            System.out.println("Please make sure both subtasks have the same id");
            System.out.println("method replaceSubtask in model.Epic");
            return;
        }
        subtasks.set(subtasks.indexOf(subtaskBefore), subtaskAfter);
    }

    //---------------------------------------------------
    // создание копии
    //---------------------------------------------------
    @Override
    public Task cloneTask() {
        Epic newTask = new Epic(getName(), getDescription());
        newTask.setId(getId());
        newTask.setStatus(getStatus());
        newTask.setDuration(getDuration());
        newTask.setStartTime(getStartTime());
        newTask.setEndTime(getEndTime());
        newTask.subtasks.addAll(subtasks);
        return newTask;
    }

    public Epic cloneEpic() {
        Epic newTask = new Epic(getName(), getDescription());
        newTask.setId(getId());
        newTask.setStatus(getStatus());
        newTask.setDuration(getDuration());
        newTask.setStartTime(getStartTime());
        newTask.setEndTime(getEndTime());
        newTask.subtasks.addAll(subtasks);
        return newTask;
    }
}
