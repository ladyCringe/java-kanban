import model.Epic;
import model.Subtask;
import model.Task;
import model.TaskStatus;
import service.Managers;
import service.TaskManager;

public class Main {

    public static void main(String[] args) {
        System.out.println("Поехали!");

        //---------------------------------------------------
        //шаги 1 и 2: тестирование создания задач и их вывода
        //---------------------------------------------------
        TaskManager manager = Managers.getDefault();

        Task task1 = new Task("task 1", "task description 1", TaskStatus.NEW);
        Task task2 = new Task("task 2", "task description 2", TaskStatus.NEW);
        manager.createTask(task1);
        manager.createTask(task2);

        Epic epic1 = new Epic("epic 1", "epic description 1");
        Epic epic2 = new Epic("epic 2", "epic description 2");
        manager.createEpic(epic1);
        manager.createEpic(epic2);

        Subtask subtask1_1 = new Subtask("subtask 1", "of epic 1", TaskStatus.NEW, epic1.getId());
        Subtask subtask1_2 = new Subtask("subtask 2", "of epic 1", TaskStatus.NEW, epic1.getId());
        Subtask subtask1_3 = new Subtask("subtask 3", "of epic 1", TaskStatus.NEW, epic1.getId());
        manager.createSubtask(subtask1_1);
        manager.createSubtask(subtask1_2);
        manager.createSubtask(subtask1_3);

        System.out.println(manager.getTaskById(task1.getId()));
        System.out.println(manager.getHistory());
        System.out.println("-".repeat(10));

        System.out.println(manager.getTaskById(task2.getId()));
        System.out.println(manager.getHistory());
        System.out.println("-".repeat(10));

        System.out.println(manager.getTaskById(task1.getId()));
        System.out.println(manager.getHistory());
        System.out.println("-".repeat(10));

        System.out.println(manager.getEpicById(epic1.getId()));
        System.out.println(manager.getEpicById(epic2.getId()));
        System.out.println(manager.getTaskById(task2.getId()));
        System.out.println(manager.getHistory());
        System.out.println("-".repeat(10));

        System.out.println(manager.getSubtaskById(subtask1_1.getId()));
        System.out.println(manager.getSubtaskById(subtask1_3.getId()));
        System.out.println(manager.getSubtaskById(subtask1_1.getId()));
        System.out.println(manager.getSubtaskById(subtask1_2.getId()));
        System.out.println(manager.getHistory());
        System.out.println("-".repeat(10));

        System.out.println(manager.getEpicById(epic2.getId()));
        System.out.println(manager.getHistory());
        System.out.println("-".repeat(10));
        printAllTasks(manager);
        System.out.println("-".repeat(20));

        manager.deleteTaskById(task1.getId());
        System.out.println(manager.getHistory());
        System.out.println("-".repeat(10));

        manager.deleteEpicById(epic1.getId());
        System.out.println(manager.getHistory());
        System.out.println("-".repeat(10));
        printAllTasks(manager);
    }

    private static void printAllTasks(TaskManager manager) {
        System.out.println("Задачи:");
        for (Task task : manager.getTasks()) {
            System.out.println(task);
        }
        System.out.println("Эпики:");
        for (Task epic : manager.getEpics()) {
            System.out.println(epic);

            for (Task task : manager.getEpicsSubtasksById(epic.getId())) {
                System.out.println("--> " + task);
            }
        }
        System.out.println("Подзадачи:");
        for (Task subtask : manager.getSubtasks()) {
            System.out.println(subtask);
        }

        System.out.println("История:");
        for (Task task : manager.getHistory()) {
            System.out.println(task);
        }
    }
}
