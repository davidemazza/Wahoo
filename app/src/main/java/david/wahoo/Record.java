package david.wahoo;

/**
 * Created by david on 05/05/2016.
 */


        import java.sql.Timestamp;


public class Record {

    private Timestamp timeStamp;
    private int numPassi;
    private int pulsazioni;


    public Record(Timestamp t) {
        timeStamp = t;
        numPassi=0;
        pulsazioni=0;
    }

    public Timestamp getTimeStampRecord() {
        return timeStamp;
    }

    public int getNumeroPassi() {
        return numPassi;
    }

    public int getPulsazioniCardiache() {
        return pulsazioni;
    }

    public void setNumeroPassi(int numPassi) {
        this.numPassi = numPassi;
    }

    public void setPulsazioniCardiache(int pulsazioni) {
        this.pulsazioni = pulsazioni;
    }




}
