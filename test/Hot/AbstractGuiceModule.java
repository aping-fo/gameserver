package Hot;

/**
 * Created by lucky on 2017/7/24.
 */
public abstract class AbstractGuiceModule {
    public boolean shutdown(){
        return true;
    }
    public boolean onStart(){
        return true;
    }
}
