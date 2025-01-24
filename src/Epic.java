import java.util.ArrayList;

public class Epic extends Task{

    private final ArrayList<Subtask> subtasks;

    public Epic(String name, String description) {
        super(name, description, TaskStatus.NEW);
        this.subtasks = new ArrayList<>();
    }

    public void addSubtask(Subtask subtask) {
        if (subtasks.contains(subtask)) {
            System.out.println("Subtask already exists here");
            System.out.println("method addSubtask in Epic");
            return;
        }
        subtasks.add(subtask);
    }

    public void removeSubtask(Subtask subtask) {
        if (!subtasks.contains(subtask)) {
            System.out.println("Subtask doesn't exists here");
            System.out.println("method removeSubtask in Epic");
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

    public void replaceSubtask (Subtask subtaskBefore, Subtask subtaskAfter) {
        if (!subtasks.contains(subtaskBefore)) {
            System.out.println("There is no subtask with id = " + subtaskBefore.getId() + " in subtasks");
            System.out.println("method replaceSubtask in Epic");
            return;
        }

        if (!subtaskBefore.getId().equals(subtaskAfter.getId())) {
            System.out.println("Please make sure both subtasks have the same id");
            System.out.println("method replaceSubtask in Epic");
        }
        subtasks.set(subtasks.indexOf(subtaskBefore), subtaskAfter);
    }
}
