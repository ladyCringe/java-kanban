import java.util.ArrayList;

public class InMemoryHistoryManager implements HistoryManager {

    static final int MAX_HISTORY_SIZE = 10;
    private final ArrayList<Task> history;

    public InMemoryHistoryManager() {
        this.history = new ArrayList<>(MAX_HISTORY_SIZE);
    }

    @Override
    public void add(Task task) {
        if (history.size() == MAX_HISTORY_SIZE){
            history.removeFirst();
        }

        history.add(task.cloneTask());
    }

    @Override
    public ArrayList<Task> getHistory() {
        return history;
    }
}
