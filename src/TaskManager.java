import java.util.ArrayList;

public interface TaskManager {

    //---------------------------------------------------
    //блок методов для tasks
    //---------------------------------------------------
    ArrayList<Task> getTasks();

    void deleteAllTasks();

    Task getTaskById(Integer id);

    void createTask(Task task);

    void updateTask(Task task);

    void deleteTaskById(Integer id);

    //---------------------------------------------------
    //блок методов для subtasks
    //---------------------------------------------------
    ArrayList<Subtask> getSubtasks();

    void deleteAllSubtasks();

    Subtask getSubtaskById(Integer id);

    void createSubtask(Subtask subtask);

    void updateSubtask(Subtask subtask);

    void deleteSubtaskById(Integer id);

    //---------------------------------------------------
    //блок методов для epics
    //---------------------------------------------------
    ArrayList<Epic> getEpics();

    void deleteAllEpics();

    Epic getEpicById(Integer id);

    void createEpic(Epic epic);

    void updateEpic(Epic epic);

    void deleteEpicById(Integer id);

    ArrayList<Subtask> getEpicsSubtasksById(int epicId);

    //---------------------------------------------------
    //История просмотров задач
    //---------------------------------------------------
    ArrayList<Task> getHistory();
}
