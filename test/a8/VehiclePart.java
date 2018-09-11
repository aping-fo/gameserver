package a8;

/**
 * Created by lucky on 2018/5/2.
 */

public class VehiclePart {
    private int tier;
    private int stars;
    private long experience;
    private int type;
    private double rd;

    public int getTier() {
        return tier;
    }

    public void setTier(int tier) {
        this.tier = tier;
    }

    public int getStars() {
        return stars;
    }

    public void setStars(int stars) {
        this.stars = stars;
    }

    public long getExperience() {
        return experience;
    }

    public void setExperience(long experience) {
        this.experience = experience;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public double getRd() {
        return rd;
    }

    public void setRd(double rd) {
        this.rd = rd;
    }

    @Override
    public String toString() {
        return "VehiclePart{" +
                "tier=" + tier +
                ", rd=" + rd +
                ", type=" + stars +
                ", stars=" + type +
                ", experience=" + experience +
                '}';
    }
}
