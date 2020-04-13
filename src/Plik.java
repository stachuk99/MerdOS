public class Plik {
    private String name;
    public String pcb;
    private int idInode;
    Inode inode = new Inode();
    public int pozZapis;  //do ktorego kolejnego bajtu tego pliku bede zapisywac kolejne znaczki
    public int pozOdczyt;
    //   public int nr;  //nr zapisu

    public Plik(String name, int idInode) {
        this.name = name;
        this.idInode = idInode;
        this.pozZapis = 0;
        this.pozOdczyt = 0;
        //       this.nr = 0;

    }
    public String Name() { return this.name; }
    public int IdNode() { return this.idInode; }  //numer i-wezla
}

