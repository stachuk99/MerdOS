import java.util.ArrayDeque;
import java.util.Queue;

public class Semafor {
    //wartość semafora
    private int value;
    public int current_PID;

    public Queue<Integer>kolejka;
    public Semafor(int v)
    {
        this.value = v;
        this.kolejka = new ArrayDeque<>();
    }

    public int S_get_value()
    {
        return this.value;
    }
    public void S_inc()
    {
        this.value++;
    }
    public void S_dec()
    {
        this.value--;
    }
    public void S_print_queue()
    {
        System.out.println("Kolejka semafora");
        for(Integer i : kolejka)
        {
            Shell.kontener.display_proc(i);
        }
    }
    public void S_print_value()
    {
        System.out.println(value);
    }
    public void S_wait(int nr_p)
    {
        this.value--;
        if(value<0)
        {
            //semafor pełny
            kolejka.offer(nr_p);
            Shell.kontener.find_proc(nr_p).state = 3;   //waiting
            //zmien stan na czekający
        }
        else
        {
            current_PID = nr_p;
        }
    }
    public int S_signal()
    {
        this.value++;
        if(value<=0)
        {
            //w semaforze czekają inne procesy
            int pid = kolejka.poll();
            Shell.kontener.find_proc(pid).state = 1;    //ready
            current_PID = pid;
            return pid;
            //zmien stanu procesu u numerze "pid" na gotowy
        }
        return -1;
    }
    public void delete_waiting(int pid)
    {
        if(this.kolejka.remove(pid))
        {
            this.S_inc();
        }
        else if(pid==current_PID)
        {
            this.S_signal();
        }
    }
}
