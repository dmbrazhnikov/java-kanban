import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;


public class Epic extends Task {

    private final Set<Integer> subTaskIds;

    public Epic(String name) {
        super(name);
        subTaskIds = new HashSet<>();
    }

    void bindSubTask(SubTask subTask) {
        subTaskIds.add(subTask.getId());
    }

    void unbindSubTask(SubTask subTask) {
        subTaskIds.remove(subTask.getId());
    }

    public Set<Integer> getSubTaskIds() {
        return subTaskIds;
    }

    @Override
    public String toString() {
        return "Epic{id=" + id +
                ", subTaskIds=" + subTaskIds +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", status=" + status +
                '}';
    }
}
