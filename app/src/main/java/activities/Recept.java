package activities;

import android.os.Parcelable;

import java.io.Serializable;
import java.util.List;

public class Recept implements Serializable {
    private String Név;
    private List<String> hozzavalok;
    private String kategoria;
    private String leiras;
    private String imageBase64;

    public Recept(){}


    public String getNév() {
        return Név;
    }

    public String getKategoria() {
        return kategoria;
    }
    public List<String> getHozzavalok() {
        return hozzavalok;
    }

    public String getLeiras() {
        return leiras;
    }

    public String getImageBase64() {
        return imageBase64;
    }

    public void setNév(String név) {
        Név = név;
    }

    public void setImageBase64(String imageBase64) {
        this.imageBase64 = imageBase64;
    }

    public void setKategoria(String kategória) {
        this.kategoria = kategória;
    }

    public void setHozzavalok(List<String> hozzavalok) {
        this.hozzavalok = hozzavalok;
    }

    public void setLeiras(String leiras) {
        this.leiras = leiras;
    }
}
