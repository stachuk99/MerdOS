import java.util.Vector;

public class Scheduler {

    PCB Running;                    // proces mający dostęp do procesora
    int time_slice;                 // czas przez który proces ma dostęp do procesora
    int priorityRunning;            // priorytet wykonywanego procesu
    Vector<PCB> ActiveVector;       // kolejka procesów aktywnych
    Vector<PCB> ExpiredVector;      // kolejka procesów czekających na kolejną turę

    // KONSTRUKTOR
    public Scheduler(){
        this.Running = new PCB();   // na początku tworzymy PCB które wygląda jak system, ale pote jest zmieniany
        this.priorityRunning = 0;
        this.time_slice = 0;        // czas, przez który proces ma dostęp do procesora
        this.ActiveVector = new Vector<PCB>();  // tworzymy vector procesów aktywnych
        this.ExpiredVector = new Vector<PCB>(); // tworzymy vector procesów czekających na kolejną turę
    }

    // DODAWANIE NOWEGO PROCESU DO ODPOWIEDNIEGO Z VECTORÓW
    public void addNew(PCB proc){
        if(ActiveVector.size() == 0)
        {
            ActiveVector.add(proc);
        }
        else {
            ExpiredVector.add(proc);        // żeby zapobiec głodzeniu procesów
            vectorSorting(ExpiredVector);   // sortowanie vectora
        }
    }

    // USUWANIE PROCESU Z ODPOWIEDNIEGO VECTORA
    public void removeProcessFromMaps(PCB proc){
        for(int i = 0; i < ActiveVector.size(); i++){
            if(proc == ActiveVector.get(i))
            {
                ActiveVector.remove(i);     // usuwanie procesu z ActiveVector
                this.time_slice = 0;
                break;
            }
        }
        for(int i = 0; i < ExpiredVector.size(); i++) {
            if (proc == ExpiredVector.get(i)) {
                ExpiredVector.remove(i);    // usuwanie procesu z ExpiredVector
                this.time_slice = 0;
                break;
            }
        }
    }

    // SORTOWANIE VECTORA PO PRIORYTECIE PROCESU
    void vectorSorting(Vector<PCB> vec){
        boolean swapped;
        PCB proc1, proc2;
        do {
            swapped = false;
            for(int i = 0; i < vec.size()-1; i++){
                if(vec.get(i).priority > vec.get(i+1).priority ){
                    proc1 = vec.get(i+1);
                    proc2 = vec.get(i);
                    vec.setElementAt(proc1, i);
                    vec.setElementAt(proc2, i+1);
                    swapped = true;
                }
            }
        } while (swapped);
    }

    //POMOCNICZE
    PCB first_ready()
    {
        return ActiveVector.get(0);
    }

    // OBLICZANIE KWANTU CZASU PRZYDZIELONEGO DO DANEGO PROCESU
    void calculateTimeslice(PCB proc){
        if(proc.priority >= 100 && proc.priority <=109)
        {
            this.time_slice = 10;
        }
        else if(proc.priority >= 110 && proc.priority <=119)
        {
            this.time_slice = 8;
        }
        else if(proc.priority >= 120 && proc.priority <=129)
        {
            this.time_slice = 6;
        }
        else if(proc.priority >= 130 && proc.priority <=139)
        {
            this.time_slice = 4;
        }
        else
        {
            this.time_slice = 1;
        }
    }

    // DODANIE DO VECTORA PROCESÓW AKTYWNYCH
    void addActive(PCB proc){
        ActiveVector.add(proc);
    }

    // DODANIE DO VECTORA PROCESÓW CZEKAJĄCYCH NA KOLEJNĄ TURĘ
    void addExpired(PCB proc){
        ExpiredVector.add(proc);
    }

    // ZAMIANA VECTORÓW
    void changeVectors(){
        Vector<PCB> extraVec = new Vector<PCB>();
        extraVec = ActiveVector;
        ActiveVector = ExpiredVector;
        ExpiredVector = extraVec;
        vectorSorting(ActiveVector);
    }

    // PRZYDZIELANIE PROCESU DO WYKONANIA
    void nextProc(){
        if(time_slice != 0 && Running == ActiveVector.get(0)){
            this.time_slice = time_slice -1;    // zmniejszamy czas przez, który proces ma dostęp do procesora
            Shell.interpreter.execute_instruction(Running);     // wysłanie procesu do interpretera
           // System.out.println(this.time_slice);
           // System.out.println(Running.name);
        }
        else
        {
            if(ActiveVector.size()>0)
            {
                if (time_slice == 0 && Running == ActiveVector.get(0)) {
                    Running.state = 1;      // zmiana stanu procesu na ready
                    addExpired(Running);    // dodanie procesu do ExpiredVector
                    ActiveVector.remove(priorityRunning);   // usunięcie procesu z ActiveVector
                    Running = new PCB();
                }
            }

            if (ActiveVector.size() == 0 && ExpiredVector.size() != 0) {
                changeVectors();
            }

            if (ActiveVector.size() != 0) {
                Running = ActiveVector.get(0);  // znalezienie procesu o najwyższym priorytecie
                Running.state = 2;              // zmieniamy stan procesu na running
                calculateTimeslice(Running);    // obliczamy czas dla procesu (przez któy ma się wykonywać)
                this.time_slice = time_slice - 1;
               // System.out.println(this.time_slice);
               // System.out.println(Running.name);

                Shell.interpreter.execute_instruction(Running);     // wysłanie procesu do interpretera
            }
            else if (ActiveVector.size() == 0 && ExpiredVector.size() == 0) {
                Shell.interpreter.execute_instruction(Shell.kontener.systemd);
            }
        }
    }


    /* DO WYŚWIETLANIA */

    void displayProcess(PCB proc)
    {
        String state;
        if(proc.state == 1){
            state = "ready";
        } else {
            state = "running";
        }
        System.out.println("id: " + proc.PID + " nazwa: " + proc.name + " stan: " + state
                + " priorytet: " + proc.priority);
    }

    // WYŚWIETLANIE VECTORA PROCESÓW AKTYWNYCH
    void displayActive(){
        System.out.println("\nActiveVector"); //  + " rozmiar: " + ActiveVector.size()
        PCB currentProcess;
        if(ActiveVector.size()==0){
            System.out.println("Vector procesów aktywnych jest pusty.");
        } else {
            for (int i = 0; i < ActiveVector.size(); i++) {
                currentProcess = ActiveVector.get(i);
                displayProcess(currentProcess);
            }
            System.out.println("Koniec ActiveVector" + "\n");
        }
    }

    // WYŚWIETLANIE VECTORA PROCESÓW CZEKAJĄCYCH NA KOLEJNĄ TURĘ
    void displayExpired(){
        System.out.println("\nExpiredVector"); // + " rozmiar: " + ExpiredVector.size()
        PCB currentProcess;
        if(ExpiredVector.size()==0){
            System.out.println("Vector procesów czekających na kolejną turę jest pusty.");
        }
        else {
            for (int i = 0; i < ExpiredVector.size(); i++) {
                currentProcess = ExpiredVector.get(i);
                displayProcess(currentProcess);
            }
            System.out.println("Koniec ExpiredVector" + "\n");
        }
    }
}
