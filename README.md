# BankSystem
这是一个银行管理系统，可以管理用户信息如账号、密码、电话号码、余额等。


想用JDBC打通Java和数据库，就必须去官网下载一个驱动，也可以直接使用我下载好的驱动。官网地址：https://downloads.mysql.com/archives/c-j/


如果是自己下载，需要把下载好的jar包，作为库文件资料——add as Library


运行程序之前需要先新建用户信息文件（eg：users.txt)和用户信息表（如果您使用的是MySQL进行数据库管理，推荐使用navicat，在这里面创建表，并且将id字段设为自增，username字段设为主键），注意提前考虑主键约束以及唯一约束。


然后将FILE_PATH、JDBC_URL、JDBC_USER、JDBC_PASSWORD以及 Connection conn = DriverManager.getConnection("url", "user", "password")这条语句替换成您个人的信息。


由于此程序设计不算很复杂，不需要下载其他依赖，关键代码也做了注释，只要完成以上步骤设置，使用起来应该不会太难。


祝您使用愉快！

This is a bank management system, which can manage user information such as account number, password, telephone number, balance, etc.


If you want to get through Java and database with JDBC, you must download a driver from the official website, or you can directly use the driver I downloaded. Official website address:https://downloads.mysql.com/archives/c-j/


If you download it yourself, you need to use the downloaded jar package as the library file material——add as Library


Before running the program, you need to create a user information file (eg:users.txt) and a user information table (if you are using MySQL for database management, Navicat is recommended to create a table here, and set the ID field as self increment and the username field as the primary key). Note that the primary key constraint and unique constraint should be considered in advance.


Then replace FILE_PATH、JDBC_URL、JDBC_USER、JDBC_PASSWORD and the statement 'Connection conn=DriveManager. getConnection ("URL", "user", "password")' with your personal information.


Due to the relatively simple design of this program, there is no need to download other dependencies, and the key code has also been commented out. As long as the above steps are completed to set it up, it should not be too difficult to use.


Wishing you a pleasant use!
