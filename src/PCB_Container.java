
import java.util.Random;
import java.util.Vector;

public class PCB_Container {
    public int counter = 2; //licznik PID
    public Vector<String> names = new Vector<String>(); //Vector przydzielonych nazw
    PCB systemd = new PCB(); //proces systemu, bedzie traktowany rowniez jako dummy.txt

    public PCB_Container() {
        names.add("systemd");
        //Shell.virtual_memory.load_program("dummy.txt",1);
    }

    //funkcje kontenera wykonujemy gdy chcemy cos zrobic wzgledem calego zbioru, np. szukanie procesu w calym drzewie
    //zabicie procesu, ktorego rodzica nie znamy, wyswietlenie procesow itp.


    public void create_proc(String name, String fileName, PCB parent) //do tworzenia procesow
    {
        Random r = new Random();
        int priority = r.nextInt((140-100) + 1) + 100;
        boolean nameTaken = false;
        for (int i = 0; i < names.size(); i++)
        {
            if (names.get(i).equals(name))
            {
                nameTaken = true;
            }
        }
        if (nameTaken == false)
        {
            PCB child = new PCB(this.counter, name, priority, fileName, parent);
            parent.children.add(child);
            counter++;
            names.add(name);
            //System.out.println("Utworzono proces "+name);
        }
        else
        {
            System.out.println("ERROR#Nie utworzono procesu - istnieje proces o tej nazwie");
        }
    }

    public PCB find_proc(int PID)
    {
        return systemd.find_kid(PID);
    } //do znalezienia procesu po PID

    public PCB find_proc(String name)
    {
        return systemd.find_kid(name);
    } //do znalezienia procesu po nazwie

    public void kill_proc(int PID) //do zabicia procesu o PID
    {
        if(PID!=1)
        {
            PCB to_kill = systemd.find_kid(PID);
            for(int i=0;i<names.size();i++)
            {
                if(to_kill.name.equals(names.get(i)))
                {
                    names.remove(i);
                }
            }
            to_kill.kill();
        }
    }

    public void display_proc(int PID)
    {
        systemd.display(PID);
    } //do wyswietlenia procesu po PID

    public void display_all_proc()
    {
        systemd.display_all();
    } //do wyswietlenia wszystkich procesow
}


