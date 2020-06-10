import java.sql.*;
import java.util.Scanner;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
public class application {
    private static boolean isRunning = true;
    private static final String url = "jdbc:postgresql://comp421.cs.mcgill.ca:5432/cs421";
    private static final String username  = "cs421g60";
    private static final String password = "comp421___";
    private static int userid;
    private static Connection conn = null;
    private static Statement st = null;
    private static ResultSet rs = null;

    public static void main(String args[]) {
        //register a driver
       try {
           Class.forName("org.postgresql.Driver");
       }catch(ClassNotFoundException e){
           System.out.println("class not find");
           return;
       }
       try {
           //initialize the connection object and the statement object
           conn = DriverManager.getConnection(url,username,password);
           st = conn.createStatement();
           if (logInCheck()) {
               while (isRunning) {
                   System.out.println("---------------------Game platform application---------------------");
                   System.out.println(" You have 6 options to play with database:");
                   System.out.println("1 - Add a friend");
                   System.out.println("2 - Search for community and join in it");
                   System.out.println("3 - Check the games bought by one player");
                   System.out.println("4 - Look up whether a particular player attended a particular event");
                   System.out.println("5 - Attend a future activity");
                   System.out.println("6 - Quit");
                   System.out.println("Please enter the option number:");
                   Scanner sc = new Scanner(System.in);
                   int o = sc.nextInt();
                   switch (o) {
                       case 1:
                           addFriend();
                           break;
                       case 2:
                           addCommunity();
                           break;
                       case 3:
                           checkGame();
                           break;
                       case 4:
                           checkEvent();
                           break;
                       case 5:
                           attendActivity();
                           break;
                       case 6:
                           System.out.println("See you next time!");
                           isRunning = false;
                           break;
                   }
               }
           } else {
               System.out.println("See you next time!");
           }
       }catch(SQLException e){
           System.out.println("Warning! Database access error occurs.");
           //e.printStackTrace();
       }finally{
           try { rs.close(); } catch (Exception e) { /* ignored */ }
           try { st.close(); } catch (Exception e) { /* ignored */ }
           try { conn.close(); } catch (Exception e) { /* ignored */ }
       }
    }
    //check if the client is a user in the platform
    private static boolean logInCheck() throws SQLException {
        boolean check = false;
        System.out.println("Hello, welcome to use our awesome application!");
        //give a while loop if the user want to try again
        while(!check){
            System.out.println("Please enter your user id:");
            Scanner sc = new Scanner(System.in);
            String id = sc.nextLine();
            int i = Integer.parseInt(id);
            rs = st.executeQuery("SELECT id FROM users");
            //loop through the result of the query
                while(rs.next()){
                    int uid = rs.getInt(1);
                    if(i==uid){
                        check=true;
                        userid = i;
                        return check;
                    }
                }
                //didn't find the userid in the result
                if(!check){
                    System.out.println("Sorry, the user is not found. Do you wanna try again? Y/N");
                    Scanner cc = new Scanner(System.in);
                    char flag = cc.next().charAt(0);
                    if(flag == 'N'){
                        break;
                    }
                }
            }
        return check;
    }

    //methods for option 1
    private static void addFriend() throws SQLException {
        List<Integer> ls = friendLst();
        HashMap<Integer, Integer> choice = new HashMap<Integer, Integer>();
        Scanner sc= new Scanner(System.in);
        System.out.println("Please enter a name:");
        String nameSearch = sc.nextLine();
        rs = st.executeQuery("SELECT id, username FROM users WHERE username LIKE '%" + nameSearch + "%';");
        int count = 0;
        if(rs!=null){
            while(rs.next()){
                int id = rs.getInt("id");
                String uname = rs.getString("username");
                //check if the user is a friend of the client
                if(!ls.contains(id)){
                    count++;
                    choice.put(count,id);
                    System.out.println(count+ " " + id +" "+uname);
                }
            }
            if(count==0){
                System.out.println("You have already add the user!");
            }else{
                //now client can choose which user to add
                System.out.println("Please enter the label of the user that you want to add:");
                int label = sc.nextInt();
                int friendid = choice.get(label);
                int result = st.executeUpdate("INSERT INTO friend (user1, user2) VALUES (" + userid + "," + friendid + ");");
                if(result==1) {
                    System.out.println("Now you have added the user as your new friend!");
                }else{
                    System.out.println("Oops, something goes wrong with your request.");
                }
            }
        }else{
            System.out.println("Sorry, the user doesn't exists.");
        }
    }
    //the helper method
    private static List<Integer> friendLst() throws SQLException{
        List<Integer> ls = new ArrayList<Integer>();
        rs = st.executeQuery("select user1, user2 from friend where user1 = " + userid + "or user2 = " + userid + ";");
        if(rs!=null) {
            while (rs.next()) {
                int a = rs.getInt("user1");
                int b = rs.getInt("user2");
                if (a != userid) {
                    ls.add(a);
                } else if (b != userid) {
                    ls.add(b);
                }

            }
        }
        return ls;
    }
    //method for second option
    public static void addCommunity() throws SQLException {
        HashMap<Integer, String> choice = new HashMap<Integer, String>();
        int count = 0;
        Scanner sc= new Scanner(System.in);
        System.out.println("Please enter a game name:");
        //find the game
        String gameSearch= sc.nextLine();
        rs = st.executeQuery("SELECT com_name,c.game_id,gname FROM community c INNER JOIN game g ON c.game_id = g.game_id WHERE gname LIKE '%" + gameSearch + "%';");
        while (rs.next()) {
                System.out.println("We have these communities!");
                count++;
                String com_name = rs.getString("com_name");
                choice.put(count,com_name);
                System.out.println(count+"    "+com_name);
        }
        //if the game not in our platform
        if(count==0){
            System.out.println("Sorry, we don't have this game.");
            return;
        }
        //now client select a community
        System.out.println("Please enter the label of the community you want to join:");
        int  n= sc.nextInt();
        String comName_join = choice.get(n);
        rs = st.executeQuery("SELECT game_id FROM community WHERE com_name LIKE '%" + comName_join + "%';");
        //rs = st.executeQuery("SELECT game_id FROM community WHERE com_name=" + comName_join + ";");
        int gameID = 0;
        while(rs.next()) {
            gameID = rs.getInt("game_id");
        }
        //String query = "INSERT INTO joins (game_id, com_name, id) VALUES (" + gameID + "','" + comName_join +"','" + userid + "');";
        comName_join = comName_join.trim();
        //System.out.println(comName_join);
        //int result = st.executeUpdate(query);

        int result = st.executeUpdate("INSERT INTO joins(game_id, com_name, id) " + "VALUES ('" + gameID + "','" + comName_join +"','" + userid + "');");
        if(result==1) {
            System.out.println("Yeah! You have joined the activity successfully.");
        }else{
            System.out.println("Oops, something goes wrong with your request.");
        }
    }

    //method for third option
    private static void checkGame() throws SQLException {
        // ask for user input
        Scanner sc = new Scanner(System.in);
        System.out.println("Please enter player id for the search");
        int idSearch = sc.nextInt();

        String query = "SELECT gname FROM buy INNER JOIN game ON buy.game_id = game.game_id WHERE buy.player_id = " + idSearch + ";";
        rs = st.executeQuery(query);
        if (rs != null) {
            System.out.println("Here are the games:");
            while (rs.next()) {
                String s1 = rs.getString("gname");
                System.out.println(s1);
            }
        }else{
            System.out.println("The user didn't buy any games.");
        }
    }

    //method for the fourth option
    private static void checkEvent() throws SQLException {
        // ask for user input
        Scanner sc = new Scanner(System.in);
        System.out.println("Please enter player id for the search:");
        int idSearch = sc.nextInt();
        System.out.println("Please enter activity name for the search:");
        String gc = sc.nextLine();
        String actSearch = sc.nextLine();

        // database query and display
        rs = st.executeQuery("SELECT ac.activity_name,att.id player_id,ac.host_time FROM activity ac INNER JOIN attend att ON ac.activity_name = att.activity_name "
                + "WHERE att.id = " + idSearch +  "AND " + "att.activity_name ='" + actSearch + "';");
        if (rs != null) {
            while (rs.next()) {
                String s1 = rs.getString("activity_name");
                s1 = s1.trim();
                int player_id = rs.getInt("player_id");
                String host_time = rs.getString("host_time");
                System.out.println("We found the following record");
                System.out.println("Activity name: " + s1 +"     "+ "player_id: " + player_id + "     " +"host_time: " + host_time);
            }
        }else{
            System.out.println("There is no matching record.");
        }
    }

    //method for the fifth option
    public static void attendActivity() throws SQLException {
        //get the current timestamp
        Timestamp currentDate = new Timestamp(System.currentTimeMillis());
        rs = st.executeQuery("SELECT activity_name,host_time FROM activity WHERE host_time > '" + currentDate + "';");
        if(rs!=null) {
            while(rs.next()){
                String actname = rs.getString("activity_name");
                Timestamp t = rs.getTimestamp("host_time");
                System.out.println(actname+ "   "+ t);
            }
            //user enter the activity name
            Scanner sc= new Scanner(System.in);
            System.out.println("Please enter an activity name you want to join:");
            String act_attend= sc.nextLine();
            rs = st.executeQuery("SELECT host_time FROM activity WHERE activity_name = '" + act_attend +"';");
            Timestamp act_time= new Timestamp(System.currentTimeMillis());
            while(rs.next()) {
                act_time = rs.getTimestamp("host_time");
            }
            rs = st.executeQuery("SELECT num_attend FROM attend WHERE activity_name = '" + act_attend + "';");
            int act_cap = 0;
            while(rs.next()) {
                act_cap = rs.getInt("num_attend");
            }
            int result = st.executeUpdate("INSERT INTO attend (id,activity_name,host_time,num_attend) VALUES ("+userid+",'"+act_attend+"','"+act_time+"',"+act_cap+");");
            if(result==1) {
                System.out.println("You have joined the activity!");
            }else{
                System.out.println("Oops, something goes wrong with your request.");
            }
        }else{
            System.out.println("Sorry,there is no activity yet.");
        }
    }
}
