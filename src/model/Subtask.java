package model;

import java.time.Duration;
import java.time.LocalDateTime;

import static model.TaskType.SUBTASK;

public class Subtask extends Task {

    private final Integer epicId;

    //---------------------------------------------------
    //конструктор
    //---------------------------------------------------
    public Subtask(String name, String description, TaskStatus status, Integer epicId) {
        super(name, description, status);
        this.epicId = epicId;
    }

    public Subtask(String name, String description, TaskStatus status, LocalDateTime startTime, Duration duration, Integer epicId) {
        super(name, description, status, startTime, duration);
        this.epicId = epicId;
    }

    //---------------------------------------------------
    //блок геттеров и сеттеров
    //---------------------------------------------------
    public Integer getEpicId() {
        return epicId;
    }

    public TaskType getType() {
        return SUBTASK;
    }

    //---------------------------------------------------
    // создание копии
    //---------------------------------------------------
    @Override
    public Task cloneTask() {
        Subtask newTask = new Subtask(getName(), getDescription(), getStatus(), getStartTime(), getDuration(), epicId);
        newTask.setId(getId());
        return newTask;
    }

    public Subtask cloneSubtask() {
        Subtask newTask = new Subtask(getName(), getDescription(), getStatus(), getStartTime(), getDuration(), epicId);
        newTask.setId(getId());
        return newTask;
    }
}
