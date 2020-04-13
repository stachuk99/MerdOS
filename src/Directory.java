import java.util.LinkedList;

public class Directory {
    public LinkedList<Plik> ListOfFiles;

    public Directory() {
        this.ListOfFiles = new LinkedList<Plik>();
    }

    public boolean checkIfExist(String fileName) {
        for (Plik p : ListOfFiles) {
            if (p.Name().equals(fileName)) return true;
        }
        return false;
    }

    public boolean addToDirectory(Plik plik) {
        for (Plik p : ListOfFiles)
            if (p.equals(plik)) {
                System.out.println("Nie mozna utworzyc pliku. Plik o tej nazwie juz istnieje!");
                return false;
            }
        ListOfFiles.add(plik);
        System.out.println("Plik o nazwie " + plik.Name() + " dodano do katalogu");
        return true;
    }

    public void deleteFromDirectory(String fileName) {
        for (Plik p : ListOfFiles) {
            if (checkIfExist(fileName) == true && p.Name().equals(fileName)) {
                ListOfFiles.remove(p);
                System.out.println("Plik usunieto z katalogu.");
                return;
            }
            System.out.println("Nie znaleziono pliku.");
        }
    }

    public Plik getFile(String fileName){
        if(checkIfExist(fileName)){
            for(Plik p : ListOfFiles){
                if (p.Name().equals(fileName)) return p;  //zwraca żądany plik
            }
        }
        return null;
    }

    public void printFiles(){
        for(int i = 0; i < ListOfFiles.size(); i++){
            System.out.print(ListOfFiles.get(i).Name() + " " + ListOfFiles.get(i).IdNode()+ " ");
            System.out.println();
        }
        if(ListOfFiles.size()==0)
        {
            System.out.println("Brak plików");
        }
    }
}
