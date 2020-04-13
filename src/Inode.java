
public class Inode {
    public int size; //rozmiar bloku
    public int bb1, bb2; //bloki bezposrednie
    public int bi = -1;
    public int block_count;
    public Semafor semafor;

    public Inode() {
        this.size = 0;
        this.block_count = 0;  //ile ma mieć blokow
        this.semafor = new Semafor(1);  //semafor dopusci jednoczesnie tylko 1 proces do pliku
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size += size;
    }
}
