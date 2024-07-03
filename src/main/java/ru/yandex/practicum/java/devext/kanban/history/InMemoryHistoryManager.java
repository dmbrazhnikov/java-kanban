package ru.yandex.practicum.java.devext.kanban.history;

import lombok.Getter;
import ru.yandex.practicum.java.devext.kanban.task.Task;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;


public class InMemoryHistoryManager implements HistoryManager {
    @Getter
    private int size = 0;
    private Node last;
    private final HashMap<Integer, Node> nodeByIdMap = new HashMap<>();

    @Override
    public void add(Task t) {
        if (t != null) {
            int taskId = t.getId();
            if (nodeByIdMap.containsKey(taskId))
                unlinkNode(nodeByIdMap.get(taskId));
            Node lastNode = linkLast(t);
            nodeByIdMap.put(taskId, lastNode);
        }
    }

    @Override
    public List<Task> getHistory() {
        List<Task> history = new LinkedList<>();
        Node l = last;
        while (l != null) {
            history.add(0, l.data);
            l = l.prev;
        }
        return history;
    }

    @Override
    public void remove(Task t) {
        int taskId = t.getId();
        Node n = nodeByIdMap.get(taskId);
        nodeByIdMap.remove(taskId);
        unlinkNode(n);
    }

    public void clear() {
        nodeByIdMap.forEach((id, node) -> {
            node.next = null;
            node.prev = null;
        });
        last = null;
        nodeByIdMap.clear();
    }

    private void unlinkNode(Node n) {
        if (n != null) {
            Node prev = n.prev, next = n.next;
            if (prev == null && next != null) // Первый элемент в истории
                next.prev = null;
            else if (prev != null && next == null) { // Последний элемент в истории
                prev.next = null;
                last = prev;
            } else if (prev != null && next != null) { // Все элементы, кроме первого и последнего. Второй предикат для ясности.
                prev.next = next;
                next.prev = prev;
            }
        }
    }

    private Node linkLast(Task t) {
        Node l = last;
        Node newNode = new Node(l, t, null);
        last = newNode;
        if (l != null)
            l.next = newNode;
        size++;
        return newNode;
    }

    private static class Node {
        Node prev;
        Task data;
        Node next;

        Node(Node prev, Task element, Node next) {
            this.data = element;
            this.next = next;
            this.prev = prev;
        }
    }
}
