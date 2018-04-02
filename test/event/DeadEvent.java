package event;

/**
 * Created by lucky on 2017/12/11.
 */
public class DeadEvent {
    private int id;
    private int hp;

    public DeadEvent(int id, int hp) {
        this.id = id;
        this.hp = hp;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getHp() {
        return hp;
    }

    public void setHp(int hp) {
        this.hp = hp;
    }

    @Override
    public String toString() {
        return "DeadEvent{" +
                "id=" + id +
                ", hp=" + hp +
                '}';
    }
}
