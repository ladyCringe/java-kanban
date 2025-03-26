package model;

import java.util.ArrayList;

import static model.TaskType.EPIC;

public class Epic extends Task {

    private final ArrayList<Subtask> subtasks;

    public Epic(String name, String description) {
        super(name, description, TaskStatus.NEW);
        this.subtasks = new ArrayList<>();
    }

    public TaskType getType() {
        return EPIC;
    }

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

    @Override
    public Task cloneTask() {
        Epic newTask = new Epic(getName(), getDescription());
        newTask.setId(getId());
        newTask.setStatus(getStatus());
        newTask.subtasks.addAll(subtasks);
        return newTask;
    }

    public Epic cloneEpic() {
        Epic newTask = new Epic(getName(), getDescription());
        newTask.setId(getId());
        newTask.setStatus(getStatus());
        newTask.subtasks.addAll(subtasks);
        return newTask;
    }
}
