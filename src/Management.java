import java.util.ArrayList;
import java.util.List;

public class Management
{
    private static Disk disk;
    Directory dir;
    int counter = 0;

    public Management()
    {
        disk = new Disk();
        //dir = new Directory();
        dir = Shell.katalog;
    }

    public Directory getDir()
    {
        return dir;
    }

    private static boolean correctFilename(String name)
    {
        if (name.isEmpty() || name.length() < 0) return false;
        else return true;
    }


    public boolean waitFile(String name, int nr_p) {
        if (!dir.checkIfExist(name)) {
            System.out.println("Pliku nie znaleziono w katalogu");
            return false;
        }
        Plik plikWait = dir.getFile(name);
        plikWait.inode.semafor.S_wait(nr_p);
        if(plikWait.pcb.equals(""))
        {
            plikWait.pcb = new String(Shell.kontener.find_proc(nr_p).name);
        }
        System.out.println("Dodano do kolejki semafora " + name + " proces o PID " + nr_p);
        return true;
    }

    public boolean signalFile(String name) {
        if (!dir.checkIfExist(name)) {
            System.out.println("Pliku nie znaleziono w katalogu");
            return false;
        }
        Plik plikWait = dir.getFile(name);
//        plikWait.inode.semafor.S_signal();
        int poms = plikWait.inode.semafor.S_signal();
        if(poms>-1)
        {
            plikWait.pcb = new String(Shell.kontener.find_proc(poms).name);
        }
        else
        {
            plikWait.pcb = new String("");
        }
        System.out.println("signalFile");
        return true;
    }

    public boolean printSem(String name) {
        if (!dir.checkIfExist(name)) {
            System.out.println("Pliku nie znaleziono w katalogu");
            return false;
        }
        Plik plikWait = dir.getFile(name);
        plikWait.inode.semafor.S_print_queue();
        return true;
    }

    public void savefile(String data, int block, int pos) {
        for (int i = pos; i - pos < data.length(); i++) {
            disk.diskData[(block * 32) + i] = data.charAt(i - pos);
        }
    }


    public boolean createFile(String name)
    {
        int idInode = counter + 1;
        if (!correctFilename(name))
        {
            System.out.println("Niewlasciwa nazwa pliku!");
            return false;
        }
        if (dir.checkIfExist(name))
        {
            System.out.println("Plik o tej nazwie istnieje juz w katalogu");
            return false;
        }
        else
        {
            Plik p = new Plik(name, idInode);
            counter++;
            p.Name();    //wyswietla nazwę pliku
            System.out.println("Stworzono plik o nazwie " + name);
            dir.addToDirectory(p);
            List<Integer> listofFreeBlocks = new ArrayList<Integer>();
            return true;
        }
    }

    public void display()
    {
        disk.display();
    }

    public String writeDataFile(String name, String sizeChar)
    {
        if (!dir.checkIfExist(name))
        {
            System.out.println("Pliku o tej nazwie nie znaleziono w katalogu");
        }
        Plik tymczasow = dir.getFile(name);
        List<Integer> listofFreeBlocks = new ArrayList<Integer>();

// pierwszy zapis pliku
        if (tymczasow.inode.size == 0 && tymczasow.pozZapis == 0)
        {
            int sizeFile = sizeChar.length();
            tymczasow.inode.setSize(sizeFile);

            int ileb = 0;
            if (sizeFile > 64)
            {
                ileb = sizeFile / 32;
                ileb++;
                if (sizeFile % 32 > 0) ileb++;
            }
            else if (sizeFile > 0)
            {
                ileb = sizeFile / 32;
                if (sizeFile % 32 > 0) ileb++;
            } else
            {
                ileb = 0;
            }
            tymczasow.inode.block_count = ileb;
            if (tymczasow.inode.block_count == 0)
            {
                System.out.println("Brak danych do wprowadzenia");
            }

//przydzielanie nr blokow dyskowych
            listofFreeBlocks = disk.findFreeBlock(tymczasow.inode.block_count);

            if (listofFreeBlocks.size() != tymczasow.inode.block_count)
            {
                System.out.println("Brak wymaganej ilości wolnych bloków");
            }

            if (tymczasow.inode.getSize() < 64 || tymczasow.inode.getSize() == 64)
            {
                if (sizeChar.length() > 32)
                {
                    tymczasow.inode.bb1 = listofFreeBlocks.get(0);
                    tymczasow.inode.bb2 = listofFreeBlocks.get(1);
                    tymczasow.inode.bi = -1;
                }
                else
                {
                    tymczasow.inode.bb1 = listofFreeBlocks.get(0);
                    tymczasow.inode.bb2 = -1;
                    tymczasow.inode.bi = -1;
                }
            }
            else if (tymczasow.inode.getSize() > 64)
            {
                tymczasow.inode.bi = listofFreeBlocks.get(2);

                for (int i = 0; i < 32; i++)
                {
                    if (i + 3 < listofFreeBlocks.size())
                    {
                        int ind = listofFreeBlocks.get(i + 3);
                        disk.diskData[(tymczasow.inode.bi * 32) + i] = (char) ind;
                    }
                    else {
                        disk.diskData[(tymczasow.inode.bi * 32) + i] = ' ';
                    }
                }
                disk.freeBlocks[tymczasow.inode.bi] = false;
                listofFreeBlocks.remove(2);
                tymczasow.inode.bb1 = listofFreeBlocks.get(0);
                tymczasow.inode.bb2 = listofFreeBlocks.get(1);
            }
            //           disk.display();
            System.out.println();
            System.out.println();

            for (int i = 0; i < sizeChar.length(); i += 32)
            {
                if (i + 31 < sizeChar.length())
                {
                    savefile(sizeChar.substring(i, i + 32), listofFreeBlocks.get((i / 32)), 0);
                }
                else
                {
                    savefile(sizeChar.substring(i, sizeChar.length()), listofFreeBlocks.get((i / 32)), 0);
                }
                disk.freeBlocks[listofFreeBlocks.get((i / 32))] = false;
            }
            disk.display();

            tymczasow.pozZapis = (tymczasow.inode.getSize() % 32); // pozycja do nadpisywania pliku

        }   //kolejny zapis pliku
        else if (tymczasow.inode.size > 0)
        {
            int sizeFile2 = tymczasow.inode.size + sizeChar.length();
            tymczasow.inode.setSize(sizeChar.length());

            if (tymczasow.inode.bi != -1)
            {
                int lastblock = 0;
                int pozIndex = 0;
                for (int i = 0; i < 32; i++)
                {
                    if (disk.diskData[(tymczasow.inode.bi * 32) + i] != ' ')
                    {
                        lastblock = (int) (disk.diskData[(tymczasow.inode.bi * 32) + i]);
                        pozIndex = i;
                    } else break;
                }
                if (sizeChar.length() <= 32 - tymczasow.pozZapis)
                {
                    savefile(sizeChar, lastblock, tymczasow.pozZapis);
                    tymczasow.pozZapis += sizeChar.length();
                }
                else
                {
                    int pom = sizeChar.length() - (32 - tymczasow.pozZapis);
                    if (pom % 32 > 0)
                    {
                        pom = pom / 32 + 1;
                    } else
                    {
                        pom = pom / 32;
                    }

                    listofFreeBlocks = disk.findFreeBlock(pom);
                    if (listofFreeBlocks.size() != pom)
                    {
                        System.out.println("Brak wymaganej ilości wolnych bloków");
                    }

                    savefile(sizeChar.substring(0, 32 - tymczasow.pozZapis), lastblock, tymczasow.pozZapis);
                    int index2 = 32 - tymczasow.pozZapis;
                    for (int i = index2; i < sizeChar.length(); i += 32)
                    {
                        if (i + 31 < sizeChar.length())
                        {
                            savefile(sizeChar.substring(i, i + 32), listofFreeBlocks.get((i / 32)), 0);
                            tymczasow.pozZapis = 0;
                        }
                        else
                        {
                            savefile(sizeChar.substring(i, sizeChar.length()), listofFreeBlocks.get((i / 32)), 0);
                            tymczasow.pozZapis = sizeChar.length() - i;
                        }
                        disk.freeBlocks[listofFreeBlocks.get((i / 32))] = false;
                        int ind = listofFreeBlocks.get(i / 32);
                        disk.diskData[(tymczasow.inode.bi * 32) + ++pozIndex] = (char) ind;
                    }
                }
            }
            else if (tymczasow.inode.bb2 != -1)
            {
                if (sizeChar.length() <= 32 - tymczasow.pozZapis)
                {
                    savefile(sizeChar, tymczasow.inode.bb2, tymczasow.pozZapis);
                    tymczasow.pozZapis += sizeChar.length();
                }
                else
                {
                    int pom = sizeChar.length() - (32 - tymczasow.pozZapis);

                    if (pom % 32 > 0)
                    {
                        pom = pom / 32 + 1;
                        pom++;
                    }

                    listofFreeBlocks = disk.findFreeBlock(pom);
                    if (listofFreeBlocks.size() != pom)
                    {
                        System.out.println("Brak wymaganej ilości wolnych bloków");
                    }
                    tymczasow.inode.bi = listofFreeBlocks.get(0);

                    for (int i = 0; i < 32; i++)
                    {
                        if (i + 1 < listofFreeBlocks.size())
                        {
                            int ind = listofFreeBlocks.get(i + 1);
                            disk.diskData[(tymczasow.inode.bi * 32) + i] = (char) ind;

                        } else {
                            disk.diskData[(tymczasow.inode.bi * 32) + i] = ' ';
                        }
                    }
                    disk.freeBlocks[tymczasow.inode.bi] = false;
                    listofFreeBlocks.remove(0);

                    System.out.println();

                    savefile(sizeChar.substring(0, 32 - tymczasow.pozZapis), tymczasow.inode.bb2, tymczasow.pozZapis);

                    int index2 = 32 - tymczasow.pozZapis;
                    for (int i = index2; i < sizeChar.length(); i += 32)
                    {
                        if (i + 31 < sizeChar.length())
                        {
                            savefile(sizeChar.substring(i, i + 32), listofFreeBlocks.get((i / 32)), 0);
                            tymczasow.pozZapis = 0;
                        } else
                        {
                            savefile(sizeChar.substring(i, sizeChar.length()), listofFreeBlocks.get((i / 32)), 0);
                            tymczasow.pozZapis = sizeChar.length() - i;
                        }
                        disk.freeBlocks[listofFreeBlocks.get((i / 32))] = false;
                    }
                }
            }
            else if (tymczasow.inode.bb1 != -1)
            {
                if (sizeChar.length() <= 32 - tymczasow.pozZapis)
                {
                    savefile(sizeChar, tymczasow.inode.bb1, tymczasow.pozZapis);
                    tymczasow.pozZapis += sizeChar.length();
                    System.out.println();

                } else { //utworzenie dodatkowych blokow: bb2 i bi
                    savefile(sizeChar.substring(0, 32 - tymczasow.pozZapis), tymczasow.inode.bb1, tymczasow.pozZapis);
                    disk.freeBlocks[tymczasow.inode.bb1] = false;

                    int pom = sizeChar.length() - (32 - tymczasow.pozZapis);
                    int ileZnakow = 0;
                    ileZnakow = 32 - tymczasow.pozZapis;

                    tymczasow.pozZapis = 0; //zapisano caly blok bb1

                    if (pom > 32)
                    {
                        if (pom % 32 == 0)
                        {
                            pom = pom / 32 + 1;
                        } else {
                            pom = pom / 32 + 2;
                        }
                    } else {
                        pom = 1;
                    }
                    listofFreeBlocks = disk.findFreeBlock(pom);

                    if (listofFreeBlocks.size() != pom) {
                        System.out.println("Brak wymaganej ilości wolnych bloków");
                        return "";
                    }

                    if (32 >= sizeChar.length() - ileZnakow)
                    {
                        tymczasow.inode.bb2 = listofFreeBlocks.get(0);
                        disk.freeBlocks[listofFreeBlocks.get(0)] = false;

                        savefile(sizeChar.substring(ileZnakow, sizeChar.length()), tymczasow.inode.bb2, tymczasow.pozZapis);
                        tymczasow.pozZapis += sizeChar.length() - ileZnakow;
                    }
                    else { //tworzy blok bb2 i blok indeksowy
                        tymczasow.inode.bb2 = listofFreeBlocks.get(0);
                        disk.freeBlocks[listofFreeBlocks.get(0)] = false;

                        savefile(sizeChar.substring(0, 32 - tymczasow.pozZapis), tymczasow.inode.bb2, tymczasow.pozZapis);
                        tymczasow.inode.bi = listofFreeBlocks.get(1);
                        System.out.println("BI: " + tymczasow.inode.bi);
                        disk.freeBlocks[listofFreeBlocks.get(1)] = false;

                        for (int i = 0; i < 32; i++)
                        {
                            if (i + 2 < listofFreeBlocks.size())
                            {
                                int ind = listofFreeBlocks.get(i + 2);
                                disk.diskData[(tymczasow.inode.bi * 32) + i] = (char) ind;
                            } else
                            {
                                disk.diskData[(tymczasow.inode.bi * 32) + i] = ' ';
                            }
                            disk.freeBlocks[listofFreeBlocks.get((i / 32))] = false; //?
                        }
                        listofFreeBlocks.remove(0);

                        System.out.println();

                        int index2 = 32 - tymczasow.pozZapis;

                        for (int i = index2; i < sizeChar.length(); i += 32)
                        {
                            if (i + 31 < sizeChar.length())
                            {
                                savefile(sizeChar.substring(i, i + 32), listofFreeBlocks.get((i / 32)), 0);
                                tymczasow.pozZapis = 0;
                            }
                            else
                            {
                                savefile(sizeChar.substring(i, sizeChar.length()), listofFreeBlocks.get((i / 32)), 0);
                                tymczasow.pozZapis = sizeChar.length() - i;
                            }
                            disk.freeBlocks[listofFreeBlocks.get((i / 32))] = false;
                        }
                    }
                }
            }
            disk.display();
        }
        return sizeChar;
    }

    public String read(String name, int howmanyChars)
    {

        String readData = "";
        String f = "";
        String s = "";
        String o = "";

        if (!dir.checkIfExist(name))
        {
            System.out.println("Pliku nie znaleziono w katalogu");
            return "";
        }
        Plik tymczasow = dir.getFile(name);
        tymczasow.pozOdczyt=0;
        if (tymczasow.inode.getSize() == 0)
        {
            System.out.println("Plik nie ma danych zapisanych na dysku");
            return "";
        }

        if (disk.blockSize > 32 || tymczasow.pozOdczyt > 31 || tymczasow.pozOdczyt < 0 || howmanyChars > tymczasow.inode.getSize())
        {
            return "Przekroczono zakres";
        } else
        {
            readData = new String();
            List<Integer> listofBlocks = new ArrayList<Integer>();

            if (tymczasow.inode.bb1 != -1)
                listofBlocks.add(tymczasow.inode.bb1);
            if (tymczasow.inode.bb2 != -1)
                listofBlocks.add(tymczasow.inode.bb2);
            if (tymczasow.inode.bi != -1)
                listofBlocks.add(tymczasow.inode.bi);

            int blokow = 0;
            if (howmanyChars > 64)
            {
                blokow = howmanyChars / 32;
                blokow++;
                if (howmanyChars % 64 > 0) blokow++;
            } else if (howmanyChars > 0) {
                blokow = howmanyChars / 32;
                if (howmanyChars % 32 > 0) blokow++;
            } else
            {
                blokow = 0;
            }

            if (blokow == 1)
            {
                for (int i = tymczasow.pozOdczyt; i < 32; i++)
                {
                    tymczasow.inode.bb1 = listofBlocks.get(0);
                    String data = Character.toString(disk.diskData[(tymczasow.inode.bb1 * 32) + i]);

                    s = s + data;
                    tymczasow.pozOdczyt++;
                    if (howmanyChars == tymczasow.pozOdczyt) break;
                }
            }
            if (blokow == 2)
            {
                for (int i = tymczasow.pozOdczyt; i < 32; i++)
                {
                    tymczasow.inode.bb1 = listofBlocks.get(0);
                    String data = Character.toString(disk.diskData[(tymczasow.inode.bb1 * 32) + i]);

                    f = f + data;
                    tymczasow.pozOdczyt++;
                }
                s = f;

                for (int i = 0; i < 32; i++)
                {
                    tymczasow.inode.bb2 = listofBlocks.get(1);
                    String data = Character.toString(disk.diskData[(tymczasow.inode.bb2 * 32) + i]);

                    s = s + data;
                    tymczasow.pozOdczyt++;
                    if (howmanyChars == tymczasow.pozOdczyt) break;
                }
            }
            if (blokow > 3)
            {
                for (int i = tymczasow.pozOdczyt; i < 32; i++)
                {
                    tymczasow.inode.bb1 = listofBlocks.get(0);
                    String data = Character.toString(disk.diskData[(tymczasow.inode.bb1 * 32) + i]);

                    o = o + data;
                    tymczasow.pozOdczyt++;
                }
                f = o;

                for (int i = 0; i < 32; i++)
                {
                    tymczasow.inode.bb2 = listofBlocks.get(1);
                    String data = Character.toString(disk.diskData[(tymczasow.inode.bb2 * 32) + i]);

                    f = f + data;
                    tymczasow.pozOdczyt++;
                }
                s = f;

                tymczasow.inode.bi = listofBlocks.get(2);
                int pozIndex = 0;
                int zostalo = 0;
                zostalo = howmanyChars - s.length();

                int indexblock = 0;
                int licznik = 0;
                for (int i = 0; i < 32; i++)
                {
                    indexblock = (int) (disk.diskData[(tymczasow.inode.bi * 32) + i]);
                    pozIndex = i;
                    if (disk.diskData[(tymczasow.inode.bi * 32) + i] != ' ') break;
                }

                for (int j = 0; j < indexblock; j++)
                {
                    for (int i = 0; i < 32; i++)
                    {
                        String data = Character.toString(disk.diskData[(indexblock * 32) + pozIndex]);
                        s = s + data;
                        licznik++;
                        pozIndex++;
                        if (licznik == zostalo) break;
                    }
                    if (licznik == zostalo) break;
                }
            }
            readData = s;
        }
        return readData;
    }

    // funkcja pomocnicza do sprawdzenia czy poprawnie zwraca Stringa
    public void printString(String name)
    {
        String przeczytanyString = read(name, 50);
        //   System.out.println(przeczytanyString);
    }

    public boolean openFile(String name, PCB proces) {
        for(Plik p : dir.ListOfFiles) {
            if (p.Name().equals(name)) {
                p.inode.semafor.S_wait(proces.PID);
                if(p.inode.semafor.S_get_value()<0)//
                {
                    System.out.println("Plik jest uzywany przez inny proces");
                }
                return true;
            }
        }
        return false;
    }

    public boolean closeFile(String name, PCB proces) {
        for (Plik p : dir.ListOfFiles) {
            if (p.Name().equals(name)) {
                p.inode.semafor.S_signal();
                return true;
            } else if (!openFile(name, proces)) {
                System.out.println("Plik nie jest otwarty");
            }
        }
        return false;
    }


    public boolean delete(String name)
    {
        if (dir.checkIfExist(name)) {
            Plik tymczasow = dir.getFile(name);

            if(tymczasow.inode.semafor.S_get_value() < 1) {
                System.out.println("Plik jest uzywany przez inny proces");
                return false;
            }
            else
            System.out.println("Usuwanie pliku: " + name);

            int indexblock = 0;
            int pozIndex = 0;
            if (tymczasow.inode.bi != -1) {
                for (int i = 0; i < 32; i++) {
                    if (disk.diskData[(tymczasow.inode.bi * 32) + i] != ' ') {
                        indexblock = (int) (disk.diskData[(tymczasow.inode.bi * 32) + i]);

                        disk.emptyBlock(indexblock);
                        disk.freeBlocks[indexblock] = true;
                    }
                }
                disk.emptyBlock(tymczasow.inode.bi);
                disk.freeBlocks[tymczasow.inode.bi] = true;
            }
            if (tymczasow.inode.bb2 != -1) {
                disk.emptyBlock(tymczasow.inode.bb2);
                disk.freeBlocks[tymczasow.inode.bb2] = true;
            }
            if (tymczasow.inode.bb1 != -1) {
                disk.emptyBlock(tymczasow.inode.bb1);
                disk.freeBlocks[tymczasow.inode.bb1] = true;
            }
            dir.deleteFromDirectory(name);
            System.out.println("Wyswietlenie dysku po usunieciu pliku: ");
            disk.display();
            return true;
        }
        else
        {
            System.out.println("Pliku o tej nazwie nie znaleziono w katalogu");
            return false;
        }
    }
}
