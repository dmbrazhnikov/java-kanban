package ru.yandex.practicum.java.devext.kanban.task;


import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class SubTask extends Task {

    private int epicId;

    public SubTask(int id, String name) {
        super(id, name);
    }

    public SubTask(String name) {
        super(name);
    }

    @Override
    public String toString() {
        return "SubTask(id=" + this.getId() + ", duration=" + this.getDuration() + ", startDateTime=" +
                this.getStartDateTime() + ", name=" + this.getName() + ", description=" + this.getDescription()
                + ", status=" + this.getStatus() + ")";
    }
}
