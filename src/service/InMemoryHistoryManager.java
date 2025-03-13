package service;

import model.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InMemoryHistoryManager implements HistoryManager {

    private final Map<Integer, Node> historyMap = new HashMap<>();
    private Node head;
    private Node tail;

    private static class Node {
        Task task;
        Node prev;
        Node next;

        Node(Node prev, Task task, Node next) {
            this.task = task;
            this.next = next;
            this.prev = prev;
        }
    }

    @Override
    public void add(Task task) {
        if (task.getId() == null) {
            System.out.println("task id is null");
            System.out.println("method add in InMemoryHistoryManager");
            return;
        }

        if (historyMap.containsKey(task.getId())) {
            remove(task.getId());
        }

        Node newNode = new Node(tail, task, null);
        linkLast(newNode);

        historyMap.put(task.getId(), newNode);
    }

    @Override
    public void remove(int id) {
        if (!historyMap.containsKey(id)) {
            System.out.println("Task with id " + id + " not found in history");
            System.out.println("method remove in InMemoryHistoryManager");
            return;
        }

        removeNode(historyMap.get(id));
        historyMap.remove(id);
    }

    @Override
    public List<Task> getHistory() {
        List<Task> historyList = new ArrayList<>();
        Node current = head;
        while (current != null) {
            historyList.add(current.task);
            current = current.next;
        }
        return historyList;
    }

    private void linkLast(Node newNode) {
        final Node oldTail = tail;

        if (oldTail != null) {
            oldTail.next = newNode;
        } else {
            head = newNode;
        }

        tail = newNode;
    }

    private void removeNode(Node node) {
        if (node == null) return;

        if (node.prev != null) {
            node.prev.next = node.next;
        } else {
            head = node.next;
        }

        if (node.next != null) {
            node.next.prev = node.prev;
        } else {
            tail = node.prev;
        }
    }

    @Override
    public void update(int id, Task task) {
        if (!historyMap.containsKey(id)) {
            System.out.println("Task with id " + id + " not found in history");
            System.out.println("method remove in InMemoryHistoryManager");
            return;
        }

        historyMap.get(id).task = task;
    }
}
