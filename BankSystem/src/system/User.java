package system;

public class User {
    private String name;
    private String password;
    private String phone;
    private String sex;
    private String balance;

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setMoney(String balance){
        this.balance = balance;
    }

    public String getBalance() {
        return balance;
    }
    public User() {
    }
    public User(String name, String password, String phone, String sex, String balance) {
        this.name = name;
        this.password = password;
        this.phone = phone;
        this.sex = sex;
        this.balance = balance;
    }
}
