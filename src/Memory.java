import java.util.ArrayList;
import java.util.Scanner;

public class Memory {
    private
    char memory[] = new char[256];

    public Memory() {
        for(int i=0;i<256;i++)
        {
            memory[i]=' ';
        }
    }


    /*------Wyswietlanie calej pamieci RAM------*/
    char[] display_all() {
        for (int i = 0; i < memory.length; i++) {
            System.out.print(i + " [" + memory[i] + "] ");
            if(i>0&&i%15==0)
            {
                System.out.println();
            }
        }
        System.out.println();
        return memory;
    }

    /*------Wpisanie elementow do tablicy znakow------*/
    /*------Do sprawdzenia, czy funkcja display_all()
    zostala dobrze napisana------*/
    char[] type_all() {
        for (int i = 0; i < memory.length; i++) {
            memory[i] = ' ';

        }
        System.out.println();
        return memory;

    }

    /*------Wyswietlanie ramki,
    ktora zostala podana w argumencie funkcji------*/
    char[] display_frame(int frame) {
        if (frame >= 0 && frame <= 15) {
            for (int i = frame * 16; i < frame * 16 + 16; i++) {
                //if(memory[i]<256) {
                  //  System.out.print("[" + (int) memory[i] + "] ");
                //}
                //else {
                    System.out.print("[" + memory[i] + "] ");
                }
            //}
            System.out.println();
            return memory;
        } else {
            System.out.println("Wrong frame number. Enter new number from 0 to 125 ");
            Scanner scan = new Scanner(System.in);
            int new_frame=scan.nextInt(); //Ponowne wpisanie jaka ramka ma byc wyswietlona
            return display_frame(new_frame);
        }
    }
    /*------Wybieranie ramki,
 ktora zostala podana w argumencie funkcji------*/
    char[] choose_frame(int frame) {
        char[] f=new char[16];
        if (frame >= 0 && frame <= 15)
        {
            for (int i = 0 ; i <  16; i++) {
                f[i]= memory[i + frame *16];
        }
            return f;
        } else {
            System.out.println("Wrong frame number. Enter new number from 0 to 15 ");
            Scanner scan = new Scanner(System.in);
            int new_frame=scan.nextInt(); //Ponowne wpisanie jaka ramka ma byc wyswietlona
            return choose_frame(new_frame);
        }
    }
    /*------Do sprawdzenia, czy funkcja display_frame(int frame)
    zostala dobrze napisana------*/
    char[] type_into_frame()
    {
        for (int i=0; i<memory.length; i++)
        {
            if ((i>15)&&(i<32))
            {
                memory[i]='a';
            }
        }
        return memory;
    }
    /*------Zapisywanie do pamieci RAM danych wartosci------*/
    /*------W argumentach: co ma byc wpisane,
   z jakiego adresu logicznego podanego w [],
   pid numer dentyfikacyjny procesu)
    ------*/
    char[] enter_data(char data, int memory_location_adres, int pid ) //-> tak bedzie, po uzyciu tablicy stronic
    {   int x=memory_location_adres/16;
        int y=memory_location_adres%16;
        VirtualMemory.PageTableData p = Shell.kontener.find_proc(pid).page_table.get(x);
        if (p.reference_bit == false)
        {
            char [] t = Shell.virtual_memory.get_frame(pid, x);
            int free_frame=Shell.virtual_memory.ffl.get_FF_index();
            for (int i= 0; i <16; i++)
            {
                memory[i + free_frame*16] = t[i];
            }
            Shell.virtual_memory.ffl.set_FF(free_frame,pid);
            p.reference_bit=true;
            p.frame=free_frame;
        }
        Shell.virtual_memory.ref_to_frame(p.frame);
        int z=p.frame*16+y;
        memory[z]=data;

        return memory;

    }
    /*------Odczytywanie z pamieci RAM z podanej komorki adresu logicznego------*/
    /*------W argumentach: jaki jest indeks pamieci logicznej------*/
    char load_data(int memory_location_adres,int pid)
    {
        int x=memory_location_adres/16;
        //System.out.println("X: "+x);
        int y=memory_location_adres%16;

        VirtualMemory.PageTableData p = Shell.kontener.find_proc(pid).page_table.get(x);
        if (p.reference_bit == false)
        {
            char [] t = Shell.virtual_memory.get_frame(pid, x);
            int free_frame=Shell.virtual_memory.ffl.get_FF_index();
            for (int i= 0; i <16; i++)
            {
                memory[i + free_frame*16] = t[i];
            }
            Shell.virtual_memory.ffl.set_FF(free_frame,pid);
            p.reference_bit=true;
            p.frame=free_frame;
        }
        Shell.virtual_memory.ref_to_frame(p.frame);
        int z=p.frame*16+y;
        return memory[z];

    }
    /*------Usuwanie wszystkiego z pamieci------*/
    char []remove_all()
    {

        for (int i = 0; i < memory.length; i++) {
            memory[i] = '.';}
        return memory;
    }





    /*------Usuwanie danych z podanej ramki------*/
    char[] remove_frame(int frame)
    {
        for (int i = frame * 16; i < frame * 16 + 16; i++) {

            memory[i]='.';
        }
        return memory;
    }
    /*------Zapisywanie string'a do RAMu------*/
    char[] write_string(String string, int memory_location_adres, int pid )
    {
        char[] s=string.toCharArray();
        for (int i=0; i<string.length(); i++)
        {
           enter_data(s[i],memory_location_adres,pid);
        }

        enter_data(';',memory_location_adres+string.length(),pid);
        return memory;
    }
    /*------Czytanie podanej ilosci slow z RAMu------*/
    String read_string(int length_string_to_read,int memory_location_adres, int pid )
    {
        String string=new String();
        for (int i=0; i<length_string_to_read; i++)
        {
            string=string+load_data(memory_location_adres+i ,pid);

        }
        return string;
    }
    /*------Czytanie string'a z RAMu------*/
    String load_string(int memory_location_adres, int pid )
    {String string=new String();
        int i=0;
/*
        System.out.println("RAM :D");
        System.out.println(memory.length);

        for(int j=memory_location_adres; j<memory.length; j++){
            string += memory[j];
            System.out.println(j+" = "+memory[j]);
        }
        System.out.println(string);
*/
        while (true)
        {

            int frame= Shell.kontener.find_proc(pid).
                    page_table.get(memory_location_adres/16).frame;

            if(memory[frame*16+(memory_location_adres%16)]!=';') {
                string = string + load_data(memory_location_adres,pid);
                memory_location_adres++;
            }
            else break;
        }
        return string;
    }
}
