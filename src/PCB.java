
import java.util.Vector;

public class PCB {

    public int PID; //PID
    public String name; //nazwa procesu
    public int state; //0 - new, 1 - ready, 2 - running, 3 - waiting, 4 - terminated
    public int priority; //priorytet
    public String fileName; //plik procesu
    public int AX, BX, CX, DX; //rejestry
    public int ip; // licznik rozkazow
    public PCB parent; //rodzic
    public Vector<PCB> children; //dzieci
    public Vector<VirtualMemory.PageTableData> page_table;

    public PCB() //konstruktor dla systemu
    {
        //system jest procesem zbednym, ma po prostu byc jako rodzic, nikt z niego nie korzysta
        //dlatego ma wartosci spoza zakresu
        this.PID = 1; //numer ID systemu
        this.name = "systemd";
        this.state = 10;
        this.priority = 100;
        this.fileName = "dummy.txt";
        this.AX = 0;
        this.BX = 0;
        this.CX = 0;
        this.DX = 0;
        this.ip = 0;
        this.parent = null;
        this.children = new Vector<PCB>();
        this.page_table = new Vector<VirtualMemory.PageTableData>();
    }

    public PCB(int PID, String name, int priority, String fileName, PCB parent) {
        this.PID = PID;
        this.name = name;
        this.state = 1;
        this.priority = priority;
        this.fileName = fileName;
        this.AX = 0;
        this.BX = 0;
        this.CX = 0;
        this.DX = 0;
        this.ip = 0;
        this.parent = parent;
        this.children = new Vector<PCB>();
        this.page_table = new Vector<VirtualMemory.PageTableData>();
        Shell.sheduler.addNew(this);
      //  Shell.virtual_memory.load_program(fileName,PID); // poszło do szela
    }

    //funkcje PCB wykorzystuja albo procesy same na sobie, np. szukaja procesow wsrod swoich dzieci

    public PCB find_kid(int PID) //do znajdowania dziecka po PID
    {
        if (this.PID == PID)
        {
            return this;
        }
        else if (this.children.isEmpty() == false)
        {
            for (int i = 0; i < this.children.size(); i++)
            {
                PCB temp = children.get(i).find_kid(PID);
                if (temp != null)
                {
                    return temp;
                }
            }
        }
        return null;
    }

    public PCB find_kid(String name) //do znajdowania dziecka po nazwie
    {
        if (this.name.equals(name))
        {
            return this;
        }
        else if (this.children.isEmpty() == false)
        {
            for (int i = 0; i < this.children.size(); i++)
            {
                PCB temp = children.get(i).find_kid(name);
                if (temp != null)
                {
                    return temp;
                }
            }
        }
        return null;
    }

    //reszta funkcji przyjmuje tylko PID procesu, jesli go nie znacie, wykorzystajcie funkcje szukania po nazwie

    public void kill() //funkcja wywolywana na zabijanym procesie
    {
        if(PID!=1)
        {
            PCB temp = this.parent;
            while (temp.PID != 1)
            {
                temp = temp.parent;
            }

            for (int i = 0; i < this.children.size(); i++)
            {
                this.children.get(i).parent = temp;
                temp.children.add(this.children.get(i));
            }

            Shell.sheduler.removeProcessFromMaps(this);
            Shell.virtual_memory.clear_memory(this.PID);

            PCB temp2 = this.parent;

            for (int i = 0; i < temp2.children.size(); i++)
            {
                if (temp2.children.get(i).PID == this.PID)
                {
                    temp2.children.removeElementAt(i);
                }
            }
        }
    }

    /* halt() robi to samo co kill(), jednak Interpreter musi wiedziec kiedy proces sie konczy,
    tak wiec halt() uzywamy tylko wtedy gdy proces chce zakonczyc swoje dzialanie,
    dzieki temu Interpreter moze wiedziec ze pojawienie sie halt() w porownaniu do kill() oznacza, ze
    nie tylko zabijany jest proces, ale rowniez ze zakonczyl swoje dzialanie
     */

    public void halt()
    {
        this.kill();
    }

    public void display(int PID) //do wyswietlenia konkretnego procesu
    {
        if (this.PID == PID)
        {
            System.out.print("PID: " + this.PID);
            System.out.print("  Nazwa: " + this.name);
            if (PID != 1) {
                System.out.print("  Stan: ");
                switch (this.state) {
                    case 0:
                        System.out.print("  New");
                        break;
                    case 1:
                        System.out.print("  Ready");
                        break;
                    case 2:
                        System.out.print("  Running");
                        break;
                    case 3:
                        System.out.print("  Waiting");
                        break;
                    case 4:
                        System.out.print("  Terminated");
                        break;
                }
            }
            if (PID != 1)
            {
                System.out.print("    Priorytet: " + this.priority);
            }
            System.out.print("  AX: " + this.AX);
            System.out.print("    BX: " + this.BX);
            System.out.print("    CX: " + this.CX);
            System.out.print("    DX: " + this.DX);
            if (PID != 1)
            {
                System.out.print("  PID  Rodzica: " + this.parent.PID);
            }
            System.out.print("  PID Dzieci: ");
            for (int i = 0; i < this.children.size(); i++)
            {
                System.out.print(this.children.get(i).PID + " ");
            }
            System.out.print("");

        }
        else
        {
            for (int i = 0; i < this.children.size(); i++)
            {
                this.children.get(i).display(PID);
            }
        }
        System.out.print(" Licznik rozkazow: "+this.ip);
        System.out.print("\n");
    }

    public void display_all() //do wyswietlenia calego potomstwa
    {
        this.display(this.PID);
        if (this.children.isEmpty() != true)
        {
            for (int i = 0; i < this.children.size(); i++)
            {
                this.children.get(i).display_all();
            }
        }
    }

}