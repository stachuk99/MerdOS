import java.util.LinkedList;
import java.util.ArrayList;
import java.util.List;

public class Disk {
    private int diskSize = 32;
    protected int blockSize = 32;
    private int vectorSize = 32;   
    public char[] diskData;   
    boolean[] freeBlocks;

    LinkedList listofFreeBlocks = new LinkedList();     
    public Disk() {
        diskData = new char[diskSize * blockSize];  // 32 bloki po 32 B = 1024 B
        freeBlocks = new boolean[vectorSize];

        for (int i = 0; i < 32; i++) {
            this.freeBlocks[i] = true;
            listofFreeBlocks.add(i);
        }
        for(int i=0;i<32*32;i++)
        {
             this.diskData[i] = ' ';
        }
    }

 //funkcja do znalezienia wolnych bloków
    public List<Integer> findFreeBlock (int n) {
        {
            List<Integer> fblocks = new ArrayList<Integer>();
            for(int i=0;i<32;i++)
            {
                if(this.freeBlocks[i]==true)
                {
                    fblocks.add(i);
                    System.out.println(i);
                    if(fblocks.size()==n)
                    {
                        break;
                    }
                }
            }
            return fblocks;
        }
    }

        public void emptyBlock (int blockNumber){
            for (int i = blockNumber * 32; i < (blockNumber + 1) * 32; i++) {
                diskData[i] = ' ';
            }
        }

    public void display()
    {
        for(int i=0;i<32;i++)
        {
            System.out.print(i+": ");
            for(int j=0;j<32;j++) {
                if ((int) diskData[(i * 32) + j] < 32) {
                    System.out.print("[" + (int)diskData[(i * 32) + j] + "]");
                }
                else {
                    System.out.print("[" + diskData[(i * 32) + j] + "]");
                }
            }
            System.out.println();
        }
    }
}




