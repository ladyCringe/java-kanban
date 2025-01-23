public class Main {

    public static void main(String[] args) {
        System.out.println("Поехали!");

        //---------------------------------------------------
        //шаг 1: тестирование создания задач
        //---------------------------------------------------
        Task task1 = new Task("task 1", "task description 1");
        Task task2 = new Task("task 2", "task description 2");

        Epic epic1 = new Epic("epic 1", "epic description 1");
        Subtask subtask1_1 = new Subtask("subtask 1", "of epic 1", epic1);
        Subtask subtask1_2 = new Subtask("subtask 2", "of epic 1", epic1);
        epic1.addSubtask(subtask1_1);
        epic1.getSubtasks().add(subtask1_2);

        Epic epic2 = new Epic("epic 2", "epic description 2");
        Subtask subtask2_1 = new Subtask("subtask 1", "of epic 2", epic2);
        epic2.getSubtasks().add(subtask2_1);

        System.out.println(task1);
        System.out.println(task2);
        System.out.println(epic1);
        System.out.println(subtask1_1);
        System.out.println(subtask1_2);
        System.out.println(epic2);
        System.out.println(subtask2_1);

        System.out.println("-".repeat(20));

        //---------------------------------------------------
        //шаг 1 тестов работает
        //---------------------------------------------------
        //---------------------------------------------------
        //шаг 2: тестирование создания объекта TaskManager
        //---------------------------------------------------
        TaskManager manager = new TaskManager();

        manager.createTask(task1);
        manager.createTask(task2);
        manager.createEpic(epic1);
        manager.createSubtask(subtask1_1);
        manager.createSubtask(subtask1_2);
        manager.createEpic(epic2);
        manager.createSubtask(subtask2_1);

        System.out.println(manager);
        System.out.println(manager.getTasks());
        System.out.println(manager.getEpics());
        System.out.println(manager.getSubtasks());

        System.out.println("-".repeat(20));

        //---------------------------------------------------
        //шаг 2 тестов работает
        //---------------------------------------------------
        //---------------------------------------------------
        //шаг 3: тестирование изменения статусов (обновления задач)
        //---------------------------------------------------
        Task task1upd = new Task("task 1upd", "task description 1upd");
        task1upd.setId(task1.getId());
        task1upd.setStatus(TaskStatus.IN_PROGRESS);
        manager.updateTask(task1upd);

        Task task2upd = new Task("task 2upd", "task description 2dup");
        task2upd.setId(task2.getId());
        task2upd.setStatus(TaskStatus.DONE);
        manager.updateTask(task2upd);

        System.out.println(manager.getTasks());

        System.out.println("-".repeat(10));

        Subtask subtask1_1upd = new Subtask("subtask1_1upd", "of epic 1", epic1);
        subtask1_1upd.setId(subtask1_1.getId());
        subtask1_1upd.setStatus(TaskStatus.IN_PROGRESS);
        manager.updateSubtask(subtask1_1upd);

        Subtask subtask2_1upd = new Subtask("subtask2_1upd", "of epic 2", epic2);
        subtask2_1upd.setId(subtask2_1.getId());
        subtask2_1upd.setStatus(TaskStatus.IN_PROGRESS);
        manager.updateSubtask(subtask2_1upd);

        System.out.println(manager.getEpics());
        System.out.println(manager.getSubtasks());

        System.out.println("-".repeat(10));

        Subtask subtask1_2upd = new Subtask("subtask1_2upd", "of epic 1", epic1);
        subtask1_2upd.setId(subtask1_2.getId());
        subtask1_2upd.setStatus(TaskStatus.DONE);
        manager.updateSubtask(subtask1_2upd);

        Subtask subtask2_1upd2 = new Subtask("subtask2_1upd2", "of epic 2", epic2);
        subtask2_1upd2.setId(subtask2_1.getId());
        subtask2_1upd2.setStatus(TaskStatus.DONE);
        manager.updateSubtask(subtask2_1upd2);

        System.out.println(manager.getEpics());
        System.out.println(manager.getSubtasks());

        System.out.println("-".repeat(10));

        Subtask subtask1_1upd2 = new Subtask("subtask1_1upd2", "of epic 1", epic1);
        subtask1_1upd2.setId(subtask1_1.getId());
        subtask1_1upd2.setStatus(TaskStatus.DONE);
        manager.updateSubtask(subtask1_1upd2);

        System.out.println(manager.getEpics());

        System.out.println("-".repeat(10));

        Subtask subtask2_1upd3 = new Subtask("subtask2_1upd3", "of epic 2", epic2);
        subtask2_1upd3.setId(subtask2_1.getId());
        subtask2_1upd3.setStatus(TaskStatus.NEW);
        manager.updateSubtask(subtask2_1upd3);

        System.out.println(manager.getEpics());

        System.out.println("-".repeat(10));

        Epic epic1upd = new Epic("epic 1upd", "epic description 1upd");
        epic1upd.setId(epic1.getId());
        manager.updateEpic(epic1upd);

        System.out.println(manager.getEpics());
        System.out.println(manager.getSubtasks());

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
        manager.deleteAllEpics();
        manager.deleteAllSubtasks();

        System.out.println(manager);
        //---------------------------------------------------
        //шаг 4 тестов работает
        //---------------------------------------------------
    }
}
