package ru.yandex.practicum.java.devext.kanban;

import ru.yandex.practicum.java.devext.kanban.task.management.TaskManager;
import ru.yandex.practicum.java.devext.kanban.task.*;
import ru.yandex.practicum.java.devext.kanban.task.management.InMemoryTaskManager;

import static ru.yandex.practicum.java.devext.kanban.task.Status.*;

public class Main {

    public static void main(String[] args) {

        System.out.println("Поехали!");
        TaskManager tm = new InMemoryTaskManager();

        /*=== Задачи ===*/

        System.out.println("\nДобавляем задачи:");
        Task t1 = new Task(tm.getNextId(),"Задача 1");
        tm.addTask(t1);
        Task t2 = new Task(tm.getNextId(),"Задача 2");
        tm.addTask(t2);
        System.out.println(tm.getTasks());

        System.out.println("\nМеняем статус задач:");
        t1.setStatus(DONE);
        tm.updateTask(t1);
        t2.setStatus(IN_PROGRESS);
        tm.updateTask(t2);
        System.out.println(tm.getTasks());

        System.out.println("\nУдаляем созданную ранее Задачу 1:");
        tm.removeTask(t1.getId());
        System.out.println(tm.getTasks());

        /*=== Эпики и подзадачи ===*/

        System.out.println("\nСоздаём два эпика и добавляем в них подзадачи:");
        Epic e1 = new Epic(tm.getNextId(),"Эпик 1");
        tm.addEpic(e1);
        SubTask st1 = new SubTask(tm.getNextId(),"Подзадача 1");
        tm.addSubTask(st1, e1);
        Epic e2 = new Epic(tm.getNextId(),"Эпик 2");
        SubTask st2 = new SubTask(tm.getNextId(),"Подзадача 2");
        tm.addSubTask(st2, e2);
        SubTask st3 = new SubTask(tm.getNextId(),"Подзадача 3");
        tm.addSubTask(st3, e2);
        System.out.println(tm.getEpics());
        System.out.println(tm.getSubTasks());

        System.out.println("\nМеняем статус Подзадачи 3 (Эпик 2): Новая -> В работе");
        st3.setStatus(IN_PROGRESS);
        tm.updateSubTask(st3);
        System.out.println(tm.getEpics());
        System.out.println(tm.getSubTasks());

        System.out.println("\nМеняем статус Подзадачи 3 (Эпик 2): В работе -> Завершена");
        st3.setStatus(DONE);
        tm.updateSubTask(st3);
        System.out.println(tm.getEpics());
        System.out.println(tm.getSubTasks());

        System.out.println("\nМеняем статус Подзадачи 2 (Эпик 2): Новая -> В работе");
        st2.setStatus(IN_PROGRESS);
        tm.updateSubTask(st2);
        System.out.println(tm.getEpics());
        System.out.println(tm.getSubTasks());

        System.out.println("\nУдаление Подзадачи 2 (Эпик 2):");
        tm.removeSubTask(st2.getId());
        System.out.println(tm.getEpics());

        System.out.println("\nПопытка удаления Эпика 1:");
        tm.removeEpic(e1.getId());
        System.out.println(tm.getEpics());
    }
}
