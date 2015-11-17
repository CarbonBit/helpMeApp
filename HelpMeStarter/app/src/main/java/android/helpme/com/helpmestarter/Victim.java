package android.helpme.com.helpmestarter;

/**
 * Created by Vitaly on 05/16/2015.
 */
public class Victim {
    private String name;
    private String password;
    private int pinCode;
    private int id;

    //What to track, sqlite doesn't support boolean data type
    //1 - true, 0 - false
    private int gps;
    private int video;
    private int photo;
    private int mic;
    private int phone;



    public Victim(String name, String password, int pinCode) {
       // this.id = id;
        this.name = name;
        this.password = password;
        this.pinCode = pinCode;
        this.gps = 1;
        this.video = 0;
        this.photo = 0;
        this.mic = 0;
        this.phone = 1;

    }
    public Victim() {

    }

    public String getName() {
        return name;
    }

    public void setName(String firstName) {
        this.name = firstName;
    }


    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getPinCode() {
        return pinCode;
    }

    public void setPinCode(int pinCode) {
        this.pinCode = pinCode;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
    public int getPhone() {
        return phone;
    }

    public void setPhone(int phone) {
        this.phone = phone;
    }

    public int getGps() {
        return gps;
    }

    public void setGps(int gps) {
        this.gps = gps;
    }

    public int getVideo() {
        return video;
    }

    public void setVideo(int video) {
        this.video = video;
    }

    public int getPhoto() {
        return photo;
    }

    public void setPhoto(int photo) {
        this.photo = photo;
    }

    public int getMic() {
        return mic;
    }

    public void setMic(int mic) {
        this.mic = mic;
    }
    @Override
    public String toString() {
        return name;
    }
}
