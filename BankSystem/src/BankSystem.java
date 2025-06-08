import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import system.User;

public class BankSystem {
    static Scanner sc = new Scanner(System.in);
    static List<User> list = new ArrayList<>();
    static User currentUser = null; // 加入一个登录状态，作用是确保登录成功才能进行7个操作
                                    // 退出时再将登录状态清空
    static final String FILE_PATH = "Your Filepath";    //添加一个user.txt存储用户信息
    private static final String JDBC_URL = "Your JDBC_URL ?useSSL=false&serverTimezone=UTC";//？后面的参数可调可不调
    private static final String JDBC_USER = "Your JDBC_USER";
    private static final String JDBC_PASSWORD = "Your JDBC_PASSWORD";
    public static void main(String[] args) {

        try {
            Connection conn = DriverManager.getConnection("url", "user", "password");
            System.out.println("数据库连接成功");
        }catch (SQLException e) {
            System.out.println("数据库连接失败：" + e.getMessage());
        }

        // 在程序启动时加载用户信息
        loadUsersFromFile();

        while (true) {
            System.out.println("···········································");
            System.out.println("··············欢迎进入管理系统················");
            System.out.println("··············1、请注册账号··················");
            System.out.println("··············2、已有账号，请登录·············");
            System.out.println("··············3、关闭系统····················");
            System.out.println("请输入：（1/2/3）");

            switch (sc.next()) {
                case "1":
                    try {               //在调用函数的时候再来处理异常，会简洁很多
                        registerUser();
                    } catch (IOException e) {
                        System.out.println("注册时发生错误：" + e.getMessage());
                    }
                    break;

                case "2":
                    try {
                        currentUser = login();
                        if (currentUser != null) {
                            showOperationMenu();
                        }
                    } catch (IOException e) {
                        System.out.println("登录时发生错误：" + e.getMessage());
                    }
                    break;

                case "3":
                    System.out.println("已关闭系统！");
                    System.exit(0);
                    break;

                default:
                    System.out.println("输入有误，请重新输入！");
                    break;
            }
        }
    }
    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASSWORD);
    }

    // 将用户数据同步到数据库 (将文件和MySQL进行同步更新，sync就是同步synchronize的缩写)
    private static void syncUserToDatabase(User user) {
        String sql = "INSERT INTO account (username, password, telephone, sex, balance) VALUES (?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE password=?, telephone=?, sex=?, balance=?";//尝试插入一条新记录，如果遇到唯一键冲突（例如，username已经存在），则执行更新操作，将冲突的那条记录的密码、电话、性别和余额更新为新的值。
        //? 是占位符（JDBC 的"空盒子"），创建时就发送给数据库预编译，数字序号 1→9 就是盒子的编号，按顺序往盒子里放值，驱动把 ? 替换为实际值，生成最终可执行的 SQL，相同的 SQL 模板只需编译一次，
        //后续执行只需传输参数值（节省网络流量），防止 SQL 注入攻击
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) { //使用PreparedStatement避免SQL注入攻击
            //注入攻击，即用户输入的数据可能被攻击者修改，eg：输入'-- or 1=1 --'由于1=1恒成立，所以这样数据库会误认为条件成立，就会把用户信息全部显示，从而进行盗取。
            //更高效：一条SQL处理两种情况（插入或更新）
            // 设置插入值（不包含id）
            pstmt.setString(1, user.getName());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getPhone());
            pstmt.setString(4, user.getSex());
            pstmt.setDouble(5, Double.parseDouble(user.getBalance()));  //当插入时发生主键冲突（即用户名已存在）时，就会执行更新操作，将已存在的那条记录的各个字段更新为当前user对象的值。

            // 设置更新值
            pstmt.setString(6, user.getPassword());
            pstmt.setString(7, user.getPhone());
            pstmt.setString(8, user.getSex());
            pstmt.setDouble(9, Double.parseDouble(user.getBalance()));  //这些代码的作用是保证当用户名已经存在时，使用新数据更新原有的记录，而不是插入重复记录。
            //在其他地方，当注册一个新用户时，会调用syncUserToDatabase方法，如果用户名已存在，则会触发更新操作（覆盖原有数据）。另外，在修改密码、充值等地方也会调用这个方法，此时用户名已经存在，所以会更新记录。
            pstmt.executeUpdate();  //当调用此句时，上面设置好的值就会被填充到SQL语句的占位符位置，然后整个SQL语句被发送到数据库执行。
        } catch (SQLException e) {
            System.err.println("数据库同步失败: " + e.getMessage());
        } catch (NumberFormatException e) {
            System.err.println("余额格式错误: " + user.getBalance());
        }
    }


    // 从数据库删除用户
    private static void deleteUserFromDatabase(String username) {
        String sql = "DELETE FROM account WHERE username = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("删除数据库记录失败: " + e.getMessage());
        }
    }
    private static void loadUsersFromFile() {
        File file = new File(FILE_PATH);

        //需要管理资源（如文件流、数据库连接等），优先使用 try-with-resources，可以实现自动关流
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] userData = line.split("#");
                if (userData.length == 5) {
                    list.add(new User(userData[0], userData[1], userData[3], userData[2], userData[4]));
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("用户信息文件未找到：" + e.getMessage());
        } catch (IOException e) {
            System.out.println("读取用户信息时发生错误：" + e.getMessage());
        }
    }

    //将刷新重写方法封装
    private static void saveUsersToFile() throws IOException {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(FILE_PATH))) {
            for (User user : list) {
                String data = user.getName() + "#" + user.getPassword() + "#" +
                        user.getSex() + "#" + user.getPhone() + "#" + user.getBalance();
                bw.write(data);
                bw.newLine();
            }
        }
    }

    private static void registerUser() throws IOException {
        System.out.println("请输入注册的账号：");
        String username = sc.next();
        if (isUserExists(username)) {
            System.out.println("账号已存在！");
            return;
        }
        System.out.println("请输入注册的电话号码：");
        String phone = sc.next();
        System.out.println("请输入注册的性别：");
        String sex = sc.next();
        System.out.println("请输入注册的密码：");
        String password = sc.next();
        User newUser = new User(username, password, phone, sex, "0");
        list.add(newUser);
        saveUsersToFile(); 
        System.out.println("注册成功！");
        syncUserToDatabase(newUser);
        System.out.println("已同步到数据库！");
    }

    private static User login() throws IOException {
        System.out.println("请输入账号：");
        String username = sc.next();
        System.out.println("请输入密码：");
        String password = sc.next();

        try (BufferedReader br = new BufferedReader(new FileReader(FILE_PATH))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] userData = line.split("#");
                if (userData.length == 5 && userData[0].equals(username) && userData[1].equals(password)) {
                    System.out.println("登录成功！");
                    return new User(userData[0], userData[1], userData[3], userData[2], userData[4]);
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("用户信息文件未找到：" + e.getMessage());
        } catch (IOException e) {
            System.out.println("读取用户信息时发生错误：" + e.getMessage());
        }
        System.out.println("账号或密码错误！");
        return null;
    }

    private static void showOperationMenu() {
        while (currentUser != null) {
            System.out.println("···············1、查看所有用户·················");
            System.out.println("···············2、删除用户····················");
            System.out.println("···············3、修改用户密码·················");
            System.out.println("···············4、查看某一个用户···············");
            System.out.println("···············5、充值用户余额·················");
            System.out.println("···············6、取出用户余额··················");
            System.out.println("···············7、退出系统····················");
            System.out.println("请输入：（1/2/3/4/5/6/7）");
            switch (sc.next()) {
                case "1":
                    showAll();
                    break;
                case "2":
                    try {
                        deleteUser();
                    } catch (IOException e) {
                        System.out.println("删除用户时发生错误：" + e.getMessage());
                    }
                    break;
                case "3":
                    try {
                        updateUser();
                    } catch (IOException e) {
                        System.out.println("修改用户密码时发生错误：" + e.getMessage());
                    }
                    break;
                case "4":
                    findUser();
                    break;
                case "5":
                    try {
                        chargeUserBalance();
                    } catch (IOException e) {
                        System.out.println("充值用户余额时发生错误：" + e.getMessage());
                    }
                    break;
                case "6":
                    try {
                        withdrawUserBalance();
                    } catch (IOException e) {
                        System.out.println("取出用户余额时发生错误：" + e.getMessage());
                    }
                    break;
                case "7":
                    currentUser = null;
                    System.out.println("已退出系统！");
                    break;
                default:
                    System.out.println("输入有误，请重新输入！");
                    break;
            }
        }
    }

    private static void chargeUserBalance() throws IOException {
        System.out.println("请输入要充值的账号：");
        String username = sc.next();
        for (User user : list) {
            if (username.equals(user.getName())) {
                System.out.println("请输入要充值的金额：");
                String balance = sc.next();
                String newBalance = String.valueOf(Double.parseDouble(user.getBalance()) + Double.parseDouble(balance));
                user.setMoney(newBalance);
                saveUsersToFile();
                System.out.println("充值成功！");
                syncUserToDatabase(user);
                System.out.println("当前余额：" + newBalance);
                return;
            }
        }
        System.out.println("账号不存在！");
    }
    private static void withdrawUserBalance() throws IOException {
        System.out.println("请输入要取钱的账号：");
        String username = sc.next();
        for (User user : list) {
            if (username.equals(user.getName())) {
                System.out.println("请输入取钱的金额：");
                String balance = sc.next();
                if (Double.parseDouble(user.getBalance()) >= Double.parseDouble(balance)) {
                    user.setMoney(String.valueOf(Double.parseDouble(user.getBalance()) - Double.parseDouble(balance)));
                    saveUsersToFile();
                    System.out.println("取钱成功！");
                    syncUserToDatabase(user);
                    System.out.println("当前余额：" + user.getBalance());
                } else {
                    System.out.println("余额不足！");
                }
                return;
            }
        }
        System.out.println("账号不存在！");
    }

    //判断注册用户名是否重复
    private static boolean isUserExists(String username) {
        return list.stream().anyMatch(u -> u.getName().equals(username));
    }

    public static void showAll() {
        System.out.println("\t用户名\t密码\t性别\t电话号码\t余额\t");
        if (list.isEmpty()) {
            System.out.println("系统中没有用户！");
        } else {
            for (User user : list) {
                System.out.println("\t" + user.getName() + "\t" + user.getPassword() +
                        "\t" + user.getSex() + "\t" + user.getPhone() +
                        "\t" + user.getBalance());
            }
        }
    }

    public static void deleteUser() throws IOException {
        System.out.println("请输入要删除的账号：");
        String username = sc.next();
        for (int i = 0; i < list.size(); i++) {
            if (username.equals(list.get(i).getName())) {
                list.remove(i);
                saveUsersToFile();
                System.out.println("删除成功！");
                deleteUserFromDatabase(username);
                System.out.println("已从数据库移除。");
                return;
            }
        }
        System.out.println("用户不存在！");
    }

    public static void updateUser() throws IOException {
        System.out.println("请输入要修改的账号：");
        String username = sc.next();
        for (User user : list) {
            if (username.equals(user.getName())) {
                System.out.println("请输入新的密码：");
                String password = sc.next();
                user.setPassword(password);
                saveUsersToFile();
                System.out.println("修改成功！");
                syncUserToDatabase(user);
                System.out.println("已同步到数据库。");
                return;
            }
        }
        System.out.println("用户不存在！");
    }

    public static void findUser() {
        System.out.println("请输入要查看的账号：");
        String username = sc.next();
        boolean found = false;      //加一个标签，作用是找到了某一个用户，马上跳出循环，避免重复遍历
        System.out.println("\t用户名\t密码\t\t性别\t\t电话号码\t余额");
        for (User user : list) {
            if (username.equals(user.getName())) {
                System.out.println("\t" + user.getName() + "\t" + user.getPassword() +
                        "\t" + user.getSex() + "\t" + user.getPhone() +
                        "\t" + user.getBalance());
                found = true;
                break;
            }
        }
        if (!found) {
            System.out.println("该用户不存在！");
        }
    }
}
