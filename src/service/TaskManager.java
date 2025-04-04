package service;

import model.Epic;
import model.Subtask;
import model.Task;

import java.util.List;

public interface TaskManager {

    //---------------------------------------------------
    //блок методов для tasks
    //---------------------------------------------------
    List<Task> getTasks();

    void deleteAllTasks();

    Task getTaskById(Integer id);

    void createTask(Task task);

    void updateTask(Task task);

    void deleteTaskById(Integer id);

    //---------------------------------------------------
    //блок методов для subtasks
    //---------------------------------------------------
    List<Subtask> getSubtasks();

    void deleteAllSubtasks();

    Subtask getSubtaskById(Integer id);

    void createSubtask(Subtask subtask);

    void updateSubtask(Subtask subtask);

    void deleteSubtaskById(Integer id);

    //---------------------------------------------------
    //блок методов для epics
    //---------------------------------------------------
    List<Epic> getEpics();

    void deleteAllEpics();

    Epic getEpicById(Integer id);

    void createEpic(Epic epic);

    void updateEpic(Epic epic);

    void deleteEpicById(Integer id);

    List<Subtask> getEpicsSubtasksById(int epicId);

    //---------------------------------------------------
    //История просмотров задач
    //---------------------------------------------------
    List<Task> getHistory();

    //---------------------------------------------------
    //Получить список приоритета задач
    //---------------------------------------------------
    List<Task> getPrioritizedTasks();
}
