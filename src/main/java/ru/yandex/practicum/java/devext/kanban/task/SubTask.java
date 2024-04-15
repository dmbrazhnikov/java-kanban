package ru.yandex.practicum.java.devext.kanban.task;


import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class SubTask extends Task {

    private int epicId;

    public SubTask(String name) {
        super(name);
    }

    @Override
    public String toString() {
        return "SubTask{id=" + id +
                ", epicId=" + epicId +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", status=" + status +
                '}';
    }
}
