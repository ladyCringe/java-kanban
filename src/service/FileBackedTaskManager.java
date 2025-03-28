package service;

import exceptions.ManagerSaveException;
import model.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

public class FileBackedTaskManager extends InMemoryTaskManager {

    final File file;

    public FileBackedTaskManager(File file) {
        this.file = file;
    }

    //---------------------------------------------------
    // блок сериализации
    //---------------------------------------------------
    private void save() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write("id,type,name,status,description,epic");
            writer.newLine();
            for (Task task : this.getTasks()) {
                writer.write(toString(task));
                writer.newLine();
            }
            for (Epic epic : this.getEpics()) {
                writer.write(toString(epic));
                writer.newLine();
                for (Subtask subtask : epic.getSubtasks()) {
                    writer.write(toString(subtask));
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка записи в файл " + file.getName());
        }
    }

    private String toString(Task task) {
        StringBuilder result = new StringBuilder();
        result.append(task.getId()).append(",");
        result.append(task.getType()).append(",");
        result.append(task.getName()).append(",");
        result.append(task.getStatus()).append(",");
        result.append(task.getDescription());

        if (task.getType().equals(TaskType.SUBTASK)) {
            result.append(",").append(((Subtask) task).getEpicId());
        }

        return result.toString();
    }

    //---------------------------------------------------
    // блок десериализации
    //---------------------------------------------------
    public static FileBackedTaskManager loadFromFile(File file) {
        FileBackedTaskManager result = new FileBackedTaskManager(file);
        int idNext = 0;

        try {
            List<String> lines = Files.readAllLines(file.toPath());
            for (String line : lines) {
                if (!line.startsWith("id")) {
                    Task task = result.fromString(line);
                    if (task != null) {
                        idNext = Math.max(idNext, task.getId());

                        if (task.getType().equals(TaskType.TASK)) {
                            result.loadTask(task);
                        } else if (task.getType().equals(TaskType.EPIC)) {
                            result.loadEpic((Epic) task);
                        } else {
                            result.loadSubtask((Subtask) task);
                        }
                    }
                }
            }
            result.setId(idNext);
        } catch (IOException e) {
            throw new ManagerSaveException("Load file error: " + file.getName());
        }
        return result;
    }

    private Task fromString(String value) {
        String[] splitValue = value.split(",");
        int id = Integer.parseInt(splitValue[0]);
        TaskType type = TaskType.valueOf(splitValue[1]);
        String name = splitValue[2];
        TaskStatus status = TaskStatus.valueOf(splitValue[3]);
        String description = splitValue[4];

        switch (type) {
            case TASK:
                Task task = new Task(name, description, status);
                task.setId(id);
                return task;
            case EPIC:
                Epic epic = new Epic(name, description);
                epic.setId(id);
                epic.setStatus(status);
                return epic;
            case SUBTASK:
                int epicId = Integer.parseInt(splitValue[5]);
                Subtask subtask = new Subtask(name, description, status, epicId);
                subtask.setId(id);
                return subtask;
            default:
                return null;
        }
    }

    //---------------------------------------------------
    //блок переопределения методов для Task
    //---------------------------------------------------
    @Override
    public void deleteAllTasks() {
        super.deleteAllTasks();
        save();
    }

    @Override
    public void createTask(Task task) {
        super.createTask(task);
        save();
    }

    @Override
    public void updateTask(Task task) {
        super.updateTask(task);
        save();
    }

    @Override
    public void deleteTaskById(Integer id) {
        super.deleteTaskById(id);
        save();
    }

    //---------------------------------------------------
    //блок переопределения методов для Subtask
    //---------------------------------------------------
    @Override
    public void deleteAllSubtasks() {
        super.deleteAllSubtasks();
        save();
    }

    @Override
    public void createSubtask(Subtask subtask) {
        super.createSubtask(subtask);
        save();
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        super.updateSubtask(subtask);
        save();
    }

    @Override
    public void deleteSubtaskById(Integer id) {
        super.deleteSubtaskById(id);
        save();
    }

    //---------------------------------------------------
    //блок переопределения методов для Epic
    //---------------------------------------------------
    @Override
    public void deleteAllEpics() {
        super.deleteAllEpics();
        save();
    }

    @Override
    public void createEpic(Epic epic) {
        super.createEpic(epic);
        save();
    }

    @Override
    public void updateEpic(Epic epic) {
        super.updateEpic(epic);
        save();
    }

    @Override
    public void deleteEpicById(Integer id) {
        super.deleteEpicById(id);
        save();
    }

    public static void main(String[] args) throws IOException {
        File file = File.createTempFile("test", ".csv");
        FileBackedTaskManager manager = new FileBackedTaskManager(file);

        Task task1 = new Task("Task1", "Description1", TaskStatus.NEW);
        Task task2 = new Task("Task2", "Description2", TaskStatus.DONE);
        manager.createTask(task1);
        manager.createTask(task2);

        Epic epic1 = new Epic("Epic1", "Description1");
        manager.createEpic(epic1);

        Subtask sub1 = new Subtask("Sub 1", "Desc 1", TaskStatus.NEW, epic1.getId());
        Subtask sub2 = new Subtask("Sub 2", "Desc 2", TaskStatus.IN_PROGRESS, epic1.getId());
        manager.createSubtask(sub1);
        manager.createSubtask(sub2);

        Epic epic2 = new Epic("Epic2", "Description2");
        manager.createEpic(epic2);

        Subtask sub3 = new Subtask("Sub 3", "Desc 3", TaskStatus.NEW, epic2.getId());
        Subtask sub4 = new Subtask("Sub 4", "Desc 4", TaskStatus.DONE, epic2.getId());
        manager.createSubtask(sub3);
        manager.createSubtask(sub4);

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(file);

        System.out.println("Tasks:");
        for (Task t : loadedManager.getTasks()) {
            System.out.println(t);
            System.out.println(manager.getTaskById(t.getId()));
        }

        System.out.println("\nEpics:");
        for (Epic e : loadedManager.getEpics()) {
            System.out.println(e);
            System.out.println(manager.getEpicById(e.getId()));
        }

        System.out.println("\nSubtasks:");
        for (Subtask s : loadedManager.getSubtasks()) {
            System.out.println(s);
            System.out.println(manager.getSubtaskById(s.getId()));
        }
    }
}
