package cz.hofmanladislav.TGH_Maze;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Comparator;
import java.util.PriorityQueue;

public class Main {

    public static Generator generator = Generator.getInstance();
    public static int m, n;
    public static int x;
    public static int y;
    public static long b0;
    public static Krizovatka pole[][];//generovani prazdneho pole
    public static Comparator<Pohled> comparator = new PohledComparator();
    public static PriorityQueue<Pohled> queue = new PriorityQueue<Pohled>(10, comparator);

    public static void main(String[] args) throws IOException {
        // odkomentovat/zakomentovat pro zadani v netbeans
        generator.setB0(3);//pocatecni hodnota generatoru
        n = 5;//rozmery bludiste
        m = 5;
        //konec vstupu v netbeans

        // odkomentovat/zakomentovat pro standartni vstup
        /*InputStreamReader isr = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(isr);
        String line = null;
        while ((line = reader.readLine()) != null) {
            String[] values = line.split(" ");
            n = Integer.parseInt(values[0]);
            m = Integer.parseInt(values[1]);
            b0 = Long.parseLong(values[2]);
        }
        generator.setB0(b0);*/
        // konec standartniho vstupu

        x = 0;//startovni bod
        y = 0;

        int smer;
        Pohled hrana = null;
        pole = new Krizovatka[m][n]; //matice krizovatek
        for (int i = 0; i < m; i++){
            for (int j = 0; j < n; j++){
                pole[i][j] = new Krizovatka(i,j); //instance prazdnych krizovatek
            }
        }
        pole[x][y].vygeneruj(); //vygenerovani pohledu do vsech moznych smeru, tedy ne mimo pole ani tam, kde jiz pohledy existuji

        int opacnySmer = 0;
        int px, py;//pomocne souradnice na ukadani souradnic predesle krizovatky
        while (queue.size() != 0)
        {
            hrana = queue.remove();
            x = hrana.x;
            y = hrana.y;//ziskani souradnic krizovatky aktualni hrany
            px = x;
            py = y;//jejich kopie
            smer = hrana.smer;

            if (smer == 0){y = y+1; opacnySmer = 1;}//kdyz je smer vychod, tak opacny je zapad a souradnice y se meni o 1
            else if (smer == 1){y = y-1; opacnySmer = 0;}//atd ve vsech smerech
            else if (smer == 2){x = x-1; opacnySmer = 3;}
            else {x = x+1; opacnySmer = 2;}

            if(!pole[x][y].maPredchudce){//pokud dana krizovatka ma predchudce (tedy je jiz spojena hranou s nejakou predeslou krizovatkou)
                //tak to znamena, ze timto smerem nemuzu vytvorit hranu, protoze by mi vznikla kruznice
                pole[px][py].value += Math.pow(2, smer);//nastaveni hodnoty krizovatce, pouziti pro generovani do hexa
                pole[x][y].value += Math.pow(2, opacnySmer);//nastaveni hodnoty krizovatce ve směru původní hrany

                pole[x][y].vygeneruj();//opet generovani pohledu
                pole[x][y].vzdalenostOdPocatku = pole[px][py].vzdalenostOdPocatku+1;
                pole[x][y].maPredchudce = true;
            }
        }

        for (int i = 0; i < m; i++){
            for (int j = 0; j < n; j++){
                System.out.print(Integer.toHexString(pole[i][j].value).toUpperCase());
            }   //vypis hodnot krizovatek ve velkych pismenech hexa
            System.out.println();
        }

        // Výpis vzdáleností křižovatek od počátku
        /*
        System.out.println();
        for (int i = 0; i < m; i++){
            for (int j = 0; j < n; j++){
                System.out.print(pole[i][j].vzdalenostOdPocatku);
            }
            System.out.println();
        }*/
    }
}

//Navrhovy vzor Singleton
class Generator {
    private static Generator instance;
    private long b0;
    private boolean firstRun = true;

    public void setB0(long b0) {
        this.b0 = b0;
    }

    public long getValue(){
        if (firstRun){
            firstRun = false;
            return (int)b0;
        }
        long bi = (long) ((b0*1664525 + 1013904223) % Math.pow(2, 32));
        long a0 = bi % 256;
        b0 = bi;
        return a0;
    }

    //Vytvorime soukromy konstruktor
    private Generator() { }

    //Metoda pro vytvoreni objektu jedinacek
    public static Generator getInstance() {
        //Je-li promenna instance null, tak se vytvori objekt
        if (instance == null) {
            instance = new Generator();
        }
        //Vratime jedinacka
        return instance;
    }
}

class Krizovatka {
    Pohled v = null;//0 vychod
    Pohled z = null;//1 zapad
    Pohled s = null;//2 sever
    Pohled j = null;//3 jih
    int x;//souradnice krizovatky
    int y;

    int value;//hodnota krizovatky v hexa pro graficky hexa vypis

    boolean maPredchudce;//tj. jestli uz do ni vede nejaka hrana - 
    //dulezite pro graficky vypis, aby si program nemyslel, ze vede do vsech smeru cesta

    int vzdalenostOdPocatku = 0;

    public Krizovatka(int x, int y) {
        this.x = x;
        this.y = y;
        value = 0;
        maPredchudce = false;
    }

    public void vygeneruj() {
        // 1) kontrola existence Pohledu (hrany)
        // 2) pokud neexistuje Pohled, nasleduje kontrola zda by hrana nebyla mimo pole - nebyla = pokracuji
        // 3) vytvarim novou instanci Pohledu, vkladam ji do fronty a nastavuji hranu se stejným ohodnocením 
        // a opačným směrem křižovatce ve směru vytvořené hrany
        // POZN.: nemuze nastat situace, kdy by existovala u vedlejsi krizovatky hrana ve smeru aktualni krizovatky

        if(v == null){if(y != Main.n-1){v = new Pohled(x, y, 0); Main.queue.add(v); Main.pole[x][y+1].z = new Pohled(x, y+1, 1, v.ohodnoceni);}}
        if(z == null){if(y != 0){z = new Pohled(x, y, 1); Main.queue.add(z); Main.pole[x][y-1].v = new Pohled(x, y-1, 0, z.ohodnoceni);}}
        if(s == null){if(x != 0){s = new Pohled(x, y, 2); Main.queue.add(s); Main.pole[x-1][y].j = new Pohled(x-1, y, 3, s.ohodnoceni);}}
        if(j == null){if(x != Main.m-1){j = new Pohled(x, y, 3); Main.queue.add(j); Main.pole[x+1][y].s = new Pohled(x+1, y, 2, j.ohodnoceni);}}
    }

    @Override
    public String toString() {
        return s + "" + j + v + z + "";
    }
}

class Pohled {//pohled na ohodnocení hrany = sílu stěny
    long ohodnoceni;
    int smer;
    int x;
    int y;

    public Pohled(int x, int y, int smer) {
        this.ohodnoceni = Main.generator.getValue();//generovani generatorem
        this.smer = smer;
        this.x = x;
        this.y = y;
    }

    public Pohled(int x, int y, int smer, long ohodnoceni) {
        this.ohodnoceni = ohodnoceni;//slouzi pro zkopirovani ohodnoceni vedlejsi krizovatce
        this.smer = smer;
        this.x = x;
        this.y = y;
    }

    @Override
    public String toString() {
        return "x"+x+"y"+y+" | "+ohodnoceni + " | " + smer;
    }
}

class PohledComparator implements Comparator<Pohled>
{
    @Override
    public int compare(Pohled x, Pohled y)//prepsani funkce podle ceho bude prioritni fronta tridit Pohledy
    {
        //neresim porovnani s nulou ale asi bych mel...
        if (x.ohodnoceni < y.ohodnoceni)
        {
            return -1;
        }
        if (x.ohodnoceni > y.ohodnoceni)
        {
            return 1;
        }
        if (x.ohodnoceni == y.ohodnoceni){//trochu prasecina - pokud se rovnaji, vitezi Pohled s mensi vzdalenosti od pocatku
            if (Main.pole[x.x][x.y].vzdalenostOdPocatku < Main.pole[y.x][y.y].vzdalenostOdPocatku){
                return -1;
            } else {
                return 1;
            }
        }
        return 0;
    }
}