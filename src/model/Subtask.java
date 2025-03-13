package model;

public class Subtask extends Task {

    private final Integer epicId;

    public Subtask(String name, String description, TaskStatus status, Integer epicId) {
        super(name, description, status);
        this.epicId = epicId;
    }

    public Integer getEpicId() {
        return epicId;
    }

    @Override
    public Task cloneTask() {
        Subtask newTask = new Subtask(getName(), getDescription(), getStatus(), epicId);
        newTask.setId(getId());
        return newTask;
    }
}
