package model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

import static model.TaskType.TASK;

public class Task {
    private String name;
    private String description;
    private TaskStatus status;
    private Integer id;
    private Duration duration;
    private LocalDateTime startTime;

    //---------------------------------------------------
    //конструктор
    //---------------------------------------------------

    public Task(String name, String description, TaskStatus status) {
        this.description = description;
        this.name = name;
        this.status = status;
    }

    public Task(String name, String description, TaskStatus status, LocalDateTime startTime, Duration duration) {
        this.description = description;
        this.name = name;
        this.status = status;
        this.duration = duration;
        this.startTime = startTime;
    }

    //---------------------------------------------------
    //блок геттеров и сеттеров
    //---------------------------------------------------

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public TaskType getType() {
        return TASK;
    }

    public Duration getDuration() {
        return duration;
    }

    public void setDuration(Duration duration) {
        this.duration = duration;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        if (startTime == null || duration == null) {
            return null;
        }
        return startTime.plus(duration);
    }

    //---------------------------------------------------
    //создание копии
    //---------------------------------------------------
    public Task cloneTask() {
        Task newTask = new Task(name, description, status, startTime, duration);
        newTask.setId(id);
        return newTask;
    }

    //---------------------------------------------------
    //переопределение hashCode(), equals() и toString()
    //---------------------------------------------------
    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Task task = (Task) obj;
        return Objects.equals(id, task.id);
    }

    @Override
    public String toString() {
        return "model.Task{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", status=" + status +
                ", id=" + id +
                ", startTime=" + startTime +
                ", duration=" + duration +
                '}';
    }
}
