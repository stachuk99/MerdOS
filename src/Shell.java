import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
public class Shell
{
    public static PCB_Container kontener = new PCB_Container();         //procesy
    public static Memory memory = new Memory();                         //RAM
    public static Scheduler sheduler = new Scheduler();                 //SKEDŻULER
    public static VirtualMemory virtual_memory = new VirtualMemory();   //pamięc wirtualna
    public static Interpreter interpreter = new Interpreter();          //interpreter
    public static Directory katalog = new Directory();
    public static Management pliki = new Management();
    public static List<String>O_pliki = new ArrayList<String>();
    //pliki



    boolean running;
    List<String> commands = new ArrayList<String>();
    Scanner scanner = new Scanner(System.in);

    Shell()
    {
        running = true;
    }

    public void exec_command(String command, List args){}

    public String get_line()
    {
        String line = scanner.nextLine();
        return line;
    }

    public String parse_command_for_its_name(String line)
    {
        List<String> matches = new ArrayList<String>();
        Matcher m = Pattern.compile("([A-Z]*)\\s*(\\S*)\\s*(\\S*)\\s*(\\S*)").matcher(line);
        if (m.matches())
        {
            for (int i = 1; i <= m.groupCount(); i++)
            {
                matches.add(m.group(i));
            }
        }
        if(matches.size()==0)
        {
            return "";
        }

        return matches.get(0);
    }

    public List<String> parse_command_for_its_arguments(String line)
    {
        List<String> args = new ArrayList<String>();
        Matcher m = Pattern.compile("([A-Z]*)\\s*(\\S*)\\s*(\\S*)\\s*(\\S*)").matcher(line);
        if (m.matches()) {
            for (int i = 2; i <= m.groupCount(); i++)
            {
                if (!m.group(i).isEmpty())
                {
                    args.add(m.group(i));
                }

            }
        }
        return args;
    }

    public void start_shell()
    {
        virtual_memory.load_program("dummy.txt",1);

        while(running) {
            try {
                System.out.print("SYSTEM:");
                String line = get_line();
                String command = parse_command_for_its_name(line);
                List<String> args = parse_command_for_its_arguments(line);

                if (command.equals("LS"))//
                {
                    if (args.size() == 0) //Sprawdzanie ilości argumentów:
                    {
                        katalog.printFiles();
                    } else {
                        System.out.println("Błędne argumenty. Wpisz HELP aby sprawdzić składnie");
                    }
                } else if (command.equals("CP"))//
                {
                    if (args.size() == 2) //Sprawdzanie ilości argumentów:
                    {
                        kontener.create_proc(args.get(0), args.get(1), kontener.find_proc("systemd"));
                        boolean t = Shell.virtual_memory.load_program(args.get(1), kontener.find_proc(args.get(0)).PID);
                        if (!t) {
                            kontener.kill_proc(kontener.find_proc(args.get(0)).PID);
                        }
                    } else {
                        System.out.println("Błędne argumenty. Wpisz HELP aby sprawdzić składnie");
                    }
                } else if (command.equals("CF"))//
                {
                    if (args.size() == 1) //Sprawdzanie ilości argumentów:
                    {
                        pliki.createFile(args.get(0));
                    } else {
                        System.out.println("Błędne argumenty. Wpisz HELP aby sprawdzić składnie");
                    }
                } else if (command.equals("OF"))//
                {
                    //open file
                    if (args.size() == 2) {
                        pliki.openFile(args.get(0), kontener.find_proc(args.get(1)));
                    } else {
                        System.out.println("Błędna komenda. Wpisz HELP aby wyświetlić polecenia");
                    }
                } else if (command.equals("WF"))//
                {
                    //write file
                    if (args.size() == 2) {
                        pliki.writeDataFile(args.get(0), args.get(1));
                    } else {
                        System.out.println("Błędna komenda. Wpisz HELP aby wyświetlić polecenia");
                    }
                } else if (command.equals("RF"))//
                {
                    if (args.size() == 2) {
                        System.out.println(pliki.read(args.get(0), Integer.parseInt(args.get(1))));
                    } else {
                        System.out.println("Błędna komenda. Wpisz HELP aby wyświetlić polecenia");
                    }
                } else if (command.equals("XF"))//
                {
                    if (args.size() == 2) {
                        pliki.closeFile(args.get(0), kontener.find_proc(args.get(1)));
                    } else {
                        System.out.println("Błędna komenda. Wpisz HELP aby wyświetlić polecenia");
                    }
                } else if (command.equals("LP"))//
                {
                    if (args.size() == 0) //Sprawdzanie ilości argumentów:
                    {
                        kontener.display_all_proc();
                    } else {
                        System.out.println("Błędne argumenty. Wpisz HELP aby sprawdzić składnie");
                    }
                } else if (command.equals("KP"))//
                {
                    if (args.size() == 1) //Sprawdzanie ilości argumentów:
                    {
                        kontener.kill_proc(kontener.find_proc(args.get(0)).PID);
                    } else {
                        System.out.println("Błędne argumenty. Wpisz HELP aby sprawdzić składnie");
                    }
                } else if (command.equals("END"))//
                {
                    if (args.size() == 0) //Sprawdzanie ilości argumentów:
                    {
                        running = false;
                    } else {
                        System.out.println("Błędne argumenty. Wpisz HELP aby sprawdzić składnie");
                    }
                } else if (command.equals("PV"))//
                {
                    if (args.size() == 1) {
                        virtual_memory.display_page_file(kontener.find_proc(args.get(0)).PID);

                    } else {
                        System.out.println("Błędna komenda. Wpisz HELP aby wyświetlić polecenia");

                    }

                } else if (command.equals("DQ")) {
                    if (args.size() > 0) {
                        System.out.println("Błędna komenda. Wpisz HELP aby wyświetlić polecenia");
                        continue;
                    }
                    virtual_memory.display_sc_queue();
                } else if (command.equals("DFL")) {
                    if (args.size() > 0) {
                        System.out.println("Błędna komenda. Wpisz HELP aby wyświetlić polecenia");
                        continue;
                    }
                    virtual_memory.display_FFL();
                } else if (command.equals("PT")) {
                    if (args.size() == 1) {
                        virtual_memory.display_page_table(Integer.parseInt(args.get(0)));
                    } else {
                        System.out.println("Błędna komenda. Wpisz HELP aby wyświetlić polecenia");
                    }

                } else if (command.equals("GO"))//
                {
                    /*if(args.size()>0)
                    {
                        System.out.println("Błędna komenda. Wpisz HELP aby wyświetlić polecenia");
                        continue;
                    }*/
                    if (args.size() == 1) {
                        for (int i = 0; i < Integer.parseInt(args.get(0)); i++) {
                            try {
                                sheduler.nextProc();
                            } catch (Exception e) {
                                System.out.println("ERROR " + e.getMessage());
                            }
                        }
                    } else if (args.size() == 0) {
                        try {
                            sheduler.nextProc();
                        } catch (Exception e) {
                            System.out.println("ERROR " + e.getMessage());
                        }
                    }
                    //virtual_memory.display_page_table(sheduler.first_ready().PID);
                    //memory.display_all();
                } else if (command.equals("DM"))//
                {
                    if (args.size() > 0) {
                        System.out.println("Błędna komenda. Wpisz HELP aby wyświetlić polecenia");
                    } else {
                        memory.display_all();
                    }
                } else if (command.equals("SF"))//
                {
                    if (args.size() == 1) {
                        memory.display_frame(Integer.parseInt(args.get(0)));
                    } else {
                        System.out.println("Błędna komenda. Wpisz HELP aby wyświetlić polecenia");

                    }
                } else if (command.equals("RM"))//read_memory
                {
                    if (args.size() == 2) {
                        System.out.println(memory.load_data(Integer.parseInt(args.get(0)), Integer.parseInt(args.get(1))));
                    } else {
                        System.out.println("Błędna komenda. Wpisz HELP aby wyświetlić polecenia");

                    }
                } else if (command.equals("RS")) {
                    if (args.size() == 3) {
                        System.out.println(memory.read_string(Integer.parseInt(args.get(0)), Integer.parseInt(args.get(1)), Integer.parseInt(args.get(2))));
                    } else {
                        System.out.println("Błędna komenda. Wpisz HELP aby wyświetlić polecenia");

                    }
                } else if (command.equals("SV")) {
                    if (args.size() == 1) {
                        katalog.getFile(args.get(0)).inode.semafor.S_print_value();
                    } else {
                        System.out.println("Błędna komenda. Wpisz HELP aby wyświetlić polecenia");

                    }
                } else if (command.equals("SQ")) {
                    if (args.size() == 1) {
                        katalog.getFile(args.get(0)).inode.semafor.S_print_queue();
                    } else {
                        System.out.println("Błędna komenda. Wpisz HELP aby wyświetlić polecenia");

                    }
                } else if (command.equals("DF")) {
                    if (args.size() == 1) {
                        pliki.delete(args.get(0));
                    } else {
                        System.out.println("Błędna komenda. Wpisz HELP aby wyświetlić polecenia");

                    }
                } else if (command.equals("DA"))//
                {
                    if (args.size() > 0) {
                        System.out.println("Błędna komenda. Wpisz HELP aby wyświetlić polecenia");
                    } else {
                        sheduler.displayActive();
                    }
                } else if (command.equals("DE"))//
                {
                    if (args.size() > 0) {
                        System.out.println("Błędna komenda. Wpisz HELP aby wyświetlić polecenia");
                    } else {
                        sheduler.displayExpired();
                    }
                } else if (command.equals("BD"))//
                {
                    if (args.size() > 0) {
                        System.out.println("Błędna komenda. Wpisz HELP aby wyświetlić polecenia");
                    } else {
                        pliki.display();
                    }
                } else if (command.equals("HELP")) {
                    System.out.println("LS                      - Listowanie plików w katalogu");
                    System.out.println("CP  [nazwa] [program]   - Tworzenie procesu");
                    System.out.println("CF  [nazwa]             - Tworzenie pliku");
                    System.out.println("DF  [nazwa]             - Usuwanie pliku");
                    System.out.println("LP                      - Listowanie wszystkich procesów");
                    System.out.println("KP  [nazwa]             - Zabijanie procesu");
                    System.out.println("END                     - Wyłaczenie interfejsu");

                    System.out.println("OF  [nazwa pliku] [nazwa_procesu]  - Otwarcie pliku");
                    System.out.println("WF  [nazwa pliku] [ilosc_znakow]      - zapisanie  wskazanej ilosci znakow do pliku");
                    System.out.println("RF  [nazwa pliku] [ilosc_znakow]      - odczytanie wskazanej ilosci znakow z pliku");
                    System.out.println("XF  [nazwa pliku] [nazwa_procesu]      - Wyjscie z pliku");

                    System.out.println("PV  [nazwa procesu]     - Wyświetlanie pliku wymiany dla danego procesu");
                    System.out.println("DQ                      - Wyświetlanie kolejki algorytmu drugiej szansy");
                    System.out.println("DFL                     - Wyświetlanie listy wolnych ramek");
                    System.out.println("PT  [PID]               - Wyświetlanie tablicy stronnic danego procesu");

                    System.out.println("GO                      - Wykonanie kolejnego rozkazu programu");

                    System.out.println("DM                      - Wyswietlanie calej pamieci RAM");
                    System.out.println("SF  [nr_ramki]          - Wyswietlanie ramki opodanym numerze");
                    System.out.println("RM  [nr_komorki] [PID]  - Wyswietlanie podanej komorki o danym adresie");
                    System.out.println("RS  [dlg_slowa] [nr_komorki] [PID] - Czytanie podanej ilosci slow zaczynajac od podanej komorki adresu");

                    System.out.println("SV  [plik]              - Wyświetlenie wartości semafora pliku");
                    System.out.println("SQ  [plik]              - Wyświetlenie kolejki semafora pliku");

                    System.out.println("DA                      - Wyświetlenie vectora procesow aktywnych");
                    System.out.println("DE                      - Wyświetlenie vectora procesow czekajacych na kolejna ture");

                    System.out.println("BD                      - Wyświetlenie blokow dyskowych");


                } else {
                    System.out.println("Błędna komenda. Wpisz HELP aby wyświetlić polecenia");
                }
            }
            catch (Exception e)
            {
                System.out.println("Błędna komenda. Wpisz HELP aby wyświetlić polecenia");
            }
        }
    }
}
