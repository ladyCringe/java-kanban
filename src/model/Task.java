package model;

import java.util.Objects;

public class Task {
    private String name;
    private String description;
    private TaskStatus status;
    private Integer id;

    //---------------------------------------------------
    //конструктор
    //---------------------------------------------------

    public Task(String name, String description, TaskStatus status) {
        this.description = description;
        this.name = name;
        this.status = status;
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

    //---------------------------------------------------
    //создание копии
    //---------------------------------------------------
    public Task cloneTask() {
        Task newTask =  new Task(name, description, status);
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
                '}';
    }
}
