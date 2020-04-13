
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;


public class VirtualMemory
{
    //------------- używane struktury danych -----------------------------

    //-------------------------Plik stronnicowania----------------------------------------------------
    //-------PID procesu, stronnice procesu
    private Map<Integer, List<Page>> PageFile;
    

    //--------------kolejka do algorytmu drugiej szansy
    public Vector<nr_reference> pages_queue;
    int pointer = 0;


    // ------lista wolnych ramek
    public FFL ffl;

    public VirtualMemory()
    {
        this.PageFile = new HashMap<>();
        //this.pages_queue = new LinkedList<>();
        this.pages_queue = new Vector<>();
        this.ffl = new FFL();
    }

    public class Page
    {
        public char[] data = new char[16];
        public Page(char [] data )
        {
            for (int i = 0; i < 16; i++)
            {
                this.data[i] = data[i];
            }
        }
        public Page()
        {
            data = new char[16];
        }

    }


    // klasa której vector znajduje się w PCB procesu
    public class PageTableData
    {
        public boolean reference_bit;  //Wartość bool'owska sprawdzająca czy ramka znajduje się w pamięci RAM
        public int frame; //Numer ramki w której znajduje się stronica

        PageTableData()
        {
            this.reference_bit = false;
            this.frame = -1;
        }
        PageTableData(boolean bit, int frame)
        {
            this.reference_bit = bit;
            this.frame = frame;
        }
    }

    // pomocnia klasa do kolejki stronnic
    public class nr_reference
    {
        private int page_number;
        private boolean reference_bit;

        public nr_reference()
        {
            page_number = -1;
            reference_bit = false;
        }
        public nr_reference(int p, boolean b)
        {
            page_number = p;
            reference_bit = b;
        }
    }

    private int find_victim()
    {
        int victim = -1;
        while (victim < 0) {
            nr_reference x = this.pages_queue.get(pointer);
            if (x.reference_bit)
            {
                x.reference_bit = false;
                //this.pages_queue.add(x);
            }
            else
            {
                victim = x.page_number;
                this.pages_queue.remove(pointer);
                if(pages_queue.size()>0)
                {
                    pointer--;
                }
            }
            pointer++;
            if(pointer>=16)
            {
                pointer=0;
            }
        }
        int pid = ffl.get_PID__FF(victim); // znaduje PID procesu do którego należy ramka w RAMie
        Vector<PageTableData> temp = Shell.kontener.find_proc(pid).page_table; // pobiera tablice stronnic danego procesu
        for (int i = 0; i < temp.size(); i++)
        {
            if (temp.get(i).frame == victim)   // szuka pod jakim indexem w tablicy znajduje się ofiara
            {
                List<Page> temp_data = PageFile.get(pid); // pobiera dane obecne w pliku wymiany
                Page p = new Page(Shell.memory.choose_frame(victim));
                temp_data.set(i, p); // zmienia dane na wypadek gdyby uległy zmianie
                PageFile.put(pid, temp_data); // nadpisuje plik wymiany
                ffl.reset_FF(victim);  // oznacza ramkę jako wolną
                Shell.kontener.find_proc(pid).page_table.get(i).frame = -1;
                Shell.kontener.find_proc(pid).page_table.get(i).reference_bit = false;
            }
        }
        System.out.println("ofiara="+victim);
        return victim;
    }

    public void ref_to_frame(int frame) // aktualizacja kolejki drugiej szansy
    {
        boolean in = false;
        for (nr_reference n : pages_queue)
        {
            if (n.page_number == frame)
            {
                n.reference_bit = true;
                in = true;
                break;
            }
        }
        if (!in)
        {
            pages_queue.add( new nr_reference(frame, false));
        }
    }

    public char[] get_frame(int pid, int frame)
    {
        return PageFile.get(pid).get(frame).data;
    }


    //-------------------Lista wolnych ramek( Free Frame List-------------//
    public class FFL
    {
        private class Pair
        {
            private boolean isFree = true;
            private int PID = -1;
        }
        private Pair[] FreeFrames = new Pair[16];
        public FFL() {
            for (int i = 0; i < 16; i++)
            {
                FreeFrames[i] = new Pair();
            }
        }
        public int get_FF_index()
        {
            for (int i = 0; i < 16 ; i++)
            {
                if (FreeFrames[i].isFree)
                {
                    return i;
                }
            }
            return find_victim();
        }
        public int get_PID__FF(int frame)
        {
            return FreeFrames[frame].PID;
        }
        public void set_FF(int frame, int PID)
        {
            FreeFrames[frame].isFree = false;
            FreeFrames[frame].PID = PID;
        }
        public void reset_FF(int frame)
        {
            FreeFrames[frame].isFree = true;
            FreeFrames[frame].PID = -1;
        }
    }

    //-----------czyści pamięć dla programu o podanym PID
    void clear_memory(int PID)
    {
        PageFile.remove(PID); // usuwa dane z pliku wymiany
        for (FFL.Pair p : ffl.FreeFrames) // oznacza ramki jako wolne
        {
            if (p.PID == PID)
            {
                p.PID = -1;
                p.isFree = true;
            }
        }
        Vector<PageTableData> temp = Shell.kontener.find_proc(PID).page_table; // usuwa z kolejki
        for (PageTableData p : temp)
        {
            if (p.reference_bit)
            {
                /*for (nr_reference n : pages_queue)
                {
                    if (p.frame == n.page_number)
                    {
                        pages_queue.remove(n);
                    }
                }*/
                for(int i=0;i< pages_queue.size();i++)
                {
                    if(p.frame == pages_queue.get(i).page_number)
                    {
                        pages_queue.remove(i);
                    }
                }
                Shell.memory.remove_frame(p.frame); // usuwa dane z RAMU
            }
        }


    }

    //-----------Wczytuje program do pliku wymiany
    boolean load_program(String path, int PID)
    {
        //System.out.println(path);
        try
        {
            BufferedReader bf = new BufferedReader(new FileReader(path));
            String line;
            StringBuilder program = new StringBuilder();
            Vector<Page> pages = new Vector<Page>();
            Page p = new Page();
            int end = 0 ;
            while (true)
            {
                line = bf.readLine();
                if (line == null)
                {
                    //System.out.println(line);
                    break;
                }
                line += " ";
                //System.out.println(line);
                program.append(line);
            }
            String s = program.toString();
            bf.close();
            //System.out.println(s.length());
            for (int  i = 0; i < s.length(); i++)
            {
                if (end < 15)
                {
                    p.data[end] = s.charAt(i);
                    end++;
                }
                else
                {
                    p.data[end] = s.charAt(i);
                    pages.add(p);
                    end = 0;
                    p = new Page();
                    Shell.kontener.find_proc(PID).page_table.add(new PageTableData());
                }

            }
            if(s.length()%16!=0)
            {
                pages.add(p);
                Shell.kontener.find_proc(PID).page_table.add(new PageTableData());
            }
            PageFile.put(PID, pages);
        }
        catch (IOException ex)
        {
            System.out.println("File reading problem");
            return false;
        }
        return true;

    }


    //---------------Interfejs pracy krokowej--------------

    void display_sc_queue() // wyświetla kolejke ramek do usunięcia
    {
        System.out.println("Zawartość kolejki drugiej szansy");
        for (nr_reference i : pages_queue)
        {
            System.out.print(i.page_number + " " + i.reference_bit + " |");
        }
        System.out.print("\n");
    }

    void display_page_file(int pid) // wyświetla fragment plik stronnicowania dla danego procesu
    {
        System.out.println("Zawartość pliku stronnicowania: ");
        List<Page> pages = PageFile.get(pid);
        for (int i = 0; i < pages.size(); i++)
        {
            System.out.print("ramka: " + i + ": ");
            for(int j =0;j<16;j++)
            {
                System.out.print(pages.get(i).data[j]);
            }
            System.out.print("\n");
        }
    }

    //void display_page()

    void display_FFL() // wyświetla wolne ramki
    {
        System.out.println("Wolne ramki: ");
        for (int i = 0; i < ffl.FreeFrames.length; i++)
        {
            if (ffl.FreeFrames[i].isFree)
            {
                System.out.print(i + " | ");
            }
        }
        System.out.print("\n");
    }

    void display_page_table(int pid) // wyświetla tablice stronnic procesu
    {
        System.out.println("Tablica stronnic procesu o PID = " + pid);
        Vector<PageTableData> temp = Shell.kontener.find_proc(pid).page_table;
        for (int i = 0; i <temp.size(); i++)
        {
            System.out.println("strona: " + i + " ramka: " +
                    temp.get(i).frame + " bit poprawności: " + temp.get(i).reference_bit);
        }
    }
}
