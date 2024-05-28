package ru.yandex.practicum.java.devext.kanban.task;


import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class SubTask extends Task {

    private int epicId;

    public SubTask(int id, String name) {
        super(id, name);
    }
}
