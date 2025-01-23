import java.util.ArrayList;

public class Epic extends Task{

    private ArrayList<Subtask> subtasks;

    public Epic(String name, String description) {
        super(name, description);
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
        return subtasks;
    }

    public void deleteAllSubtasks() {
        subtasks.clear();
    }

    public void replaceSubtask(Subtask subtask) {
        if (!subtasks.contains(subtask)) {
            System.out.println("There is no subtask with id = " + subtask.getId() + " in subtasks");
            System.out.println("method replaceSubtask in Epic");
            return;
        }
        subtasks.set(subtasks.indexOf(subtask), subtask);
    }

    public void setSubtasks(ArrayList<Subtask> subtasks) {
        this.subtasks = subtasks;
    }
}
