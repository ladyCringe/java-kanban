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
        Subtask subtask2_1 = new Subtask("subtask 1", "of epic 2", TaskStatus.NEW, epic2.getId());
        manager.createSubtask(subtask1_1);
        manager.createSubtask(subtask1_2);
        manager.createSubtask(subtask2_1);

        System.out.println(task1);
        System.out.println(task2);
        System.out.println(epic1);
        System.out.println(subtask1_1);
        System.out.println(subtask1_2);
        System.out.println(epic2);
        System.out.println(subtask2_1);

        System.out.println("-".repeat(10));

        System.out.println(manager);
        System.out.println(manager.getTasks());
        System.out.println(manager.getEpics());
        System.out.println(manager.getSubtasks());

        System.out.println("-".repeat(10));

        printAllTasks(manager);

        System.out.println("-".repeat(10));

        System.out.println(manager.getTaskById(task1.getId()));
        System.out.println(manager.getTaskById(task2.getId()));
        System.out.println(manager.getTaskById(task1.getId()));
        System.out.println(manager.getEpicById(epic2.getId()));
        System.out.println(manager.getSubtaskById(subtask2_1.getId()));
        System.out.println(manager.getEpicById(epic1.getId()));

        System.out.println("-".repeat(10));

        printAllTasks(manager);

        System.out.println("-".repeat(20));
        //---------------------------------------------------
        //шаги 1 и 2 тестов работают
        //---------------------------------------------------

        //---------------------------------------------------
        //шаг 3: тестирование изменения статусов (обновления задач)
        //---------------------------------------------------
        Task task1upd = new Task("task 1upd", "task description 1upd", TaskStatus.IN_PROGRESS);
        task1upd.setId(task1.getId());
        manager.updateTask(task1upd);

        Task task2upd = new Task("task 2upd", "task description 2dup", TaskStatus.DONE);
        task2upd.setId(task2.getId());
        manager.updateTask(task2upd);

        System.out.println(manager.getTasks());

        System.out.println("-".repeat(10));

        Subtask subtask1_1upd = new Subtask("subtask1_1upd", "of epic 1",
                TaskStatus.IN_PROGRESS, epic1.getId());
        subtask1_1upd.setId(subtask1_1.getId());
        manager.updateSubtask(subtask1_1upd);

        Subtask subtask2_1upd = new Subtask("subtask2_1upd", "of epic 2",
                TaskStatus.IN_PROGRESS, epic2.getId());
        subtask2_1upd.setId(subtask2_1.getId());
        manager.updateSubtask(subtask2_1upd);

        System.out.println(manager.getEpics());
        System.out.println(manager.getSubtasks());

        System.out.println("-".repeat(10));

        Subtask subtask1_2upd = new Subtask("subtask1_2upd", "of epic 1",
                TaskStatus.DONE, epic1.getId());
        subtask1_2upd.setId(subtask1_2.getId());
        manager.updateSubtask(subtask1_2upd);

        Subtask subtask2_1upd2 = new Subtask("subtask2_1upd2", "of epic 2",
                TaskStatus.DONE, epic2.getId());
        subtask2_1upd2.setId(subtask2_1.getId());
        manager.updateSubtask(subtask2_1upd2);

        System.out.println(manager.getEpics());
        System.out.println(manager.getSubtasks());

        System.out.println("-".repeat(10));

        Subtask subtask1_1upd2 = new Subtask("subtask1_1upd2", "of epic 1",
                TaskStatus.DONE, epic1.getId());
        subtask1_1upd2.setId(subtask1_1.getId());
        manager.updateSubtask(subtask1_1upd2);

        System.out.println(manager.getEpics());

        System.out.println("-".repeat(10));

        System.out.println(manager.getEpicById(epic2.getId()));
        System.out.println(manager.getSubtaskById(subtask2_1.getId()));
        System.out.println(manager.getEpicById(epic1.getId()));

        System.out.println("-".repeat(10));

        printAllTasks(manager);

        System.out.println("-".repeat(10));

        Subtask subtask2_1upd3 = new Subtask("subtask2_1upd3", "of epic 2",
                TaskStatus.NEW, epic2.getId());
        subtask2_1upd3.setId(subtask2_1.getId());
        manager.updateSubtask(subtask2_1upd3);

        System.out.println(manager.getEpics());

        System.out.println("-".repeat(10));

        Epic epic1upd = new Epic("epic 1upd", "epic description 1upd");
        epic1upd.setId(epic1.getId());
        manager.updateEpic(epic1upd);

        System.out.println(manager.getEpics());
        System.out.println(manager.getSubtasks());

        System.out.println("-".repeat(10));

        printAllTasks(manager);

        System.out.println("-".repeat(20));
        //---------------------------------------------------
        //шаг 3 тестов работает
        //---------------------------------------------------

        //---------------------------------------------------
        //шаг 4: тестрование удаления задач
        //---------------------------------------------------
        manager.deleteTaskById(task1.getId());
        manager.deleteEpicById(epic2.getId());
        System.out.println(manager.getTasks());
        System.out.println(manager.getEpics());
        System.out.println(manager.getSubtasks());

        System.out.println("-".repeat(20));

        manager.deleteAllTasks();

        System.out.println(manager.getTasks());
        System.out.println(manager.getEpics());
        System.out.println(manager.getSubtasks());

        System.out.println("-".repeat(10));

        manager.deleteAllSubtasks();

        System.out.println(manager.getTasks());
        System.out.println(manager.getEpics());
        System.out.println(manager.getSubtasks());

        System.out.println("-".repeat(10));

        manager.deleteAllEpics();

        System.out.println(manager);

        System.out.println("-".repeat(10));

        printAllTasks(manager);
        //---------------------------------------------------
        //шаг 4 тестов работает
        //---------------------------------------------------
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
