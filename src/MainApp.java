import java.util.*;
import java.util.HashMap;
import java.sql.*;

class DBConnection {
    static final String URL = "jdbc:mysql://localhost:3306/examdb";
    static final String USER = "root";
    static final String PASS = "123456";

    public static Connection getConnection() throws Exception {
        return DriverManager.getConnection(URL, USER, PASS);
    }
}

class StudentDAO {

    // Add student to DB
    public void addStudent(Student s) {
        try (Connection con = DBConnection.getConnection()) {

            String query = "INSERT INTO students (rollNo, name, branch, sem, sec) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement ps = con.prepareStatement(query);

            ps.setInt(1, s.getRollNo());
            ps.setString(2, s.getName());
            ps.setString(3, s.getBranch());
            ps.setInt(4, s.getSem());
            ps.setString(5,s.getSec());

            ps.executeUpdate();
            System.out.println("Student added to DB!");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Fetch students from DB
    public ArrayList<Student> getStudents() {
        ArrayList<Student> list = new ArrayList<>();

        try (Connection con = DBConnection.getConnection()) {

            ResultSet rs = con.createStatement().executeQuery("SELECT * FROM students");

            while (rs.next()) {
                list.add(new Student(
                        rs.getInt(1),
                        rs.getString(2),
                        rs.getString(3),
                        rs.getInt(4),
                        rs.getString(5)
                ));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }
}

class Student {
    private int rollNo;
    private String name, branch;
    private int sem;
    private String sec;

    public Student(int rollNo, String name, String branch, int sem, String sec) {
        this.rollNo = rollNo;
        this.name = name;
        this.branch = branch;
        this.sem = sem;
        this.sec=sec;
    }

    public int getRollNo() { return rollNo; }
    public String getName() { return name; }
    public String getBranch() { return branch; }
    public int getSem() { return sem; }
    public String getSec() { return sec; }
}

class Room {
    int roomNo;
    int capacity;
    boolean isBlocked;

    Room(int roomNo, int capacity) {
        this.roomNo = roomNo;
        this.capacity = capacity;
        this.isBlocked = false;
    }
}

class RoomManager {
    ArrayList<Room> rooms = new ArrayList<>();

    void addRoom(int roomNo, int capacity) {
        rooms.add(new Room(roomNo, capacity));
        System.out.println("Room added!");
    }

    void displayRooms() {
        for (Room r : rooms) {
            System.out.println("Room: " + r.roomNo +
                    " Capacity: " + r.capacity +
                    " Status: " + (r.isBlocked ? "Blocked" : "Available"));
        }
    }
}

class Exam {
    String subject, date, time;

    Exam(String subject, String date, String time) {
        this.subject = subject;
        this.date = date;
        this.time = time;
    }
}

class ExamManager {
    HashMap<Integer, Exam> examSchedule = new HashMap<>();

    void addExam(int sem, String subject, String date, String time) {
        examSchedule.put(sem, new Exam(subject, date, time));
        System.out.println("Exam scheduled!");
    }

    void displayExams() {
        for (int sem : examSchedule.keySet()) {
            Exam e = examSchedule.get(sem);
            System.out.println("Sem " + sem + " -> " +
                    e.subject + " " + e.date + " " + e.time);
        }
    }
}

class Seat {
    int roomNo;
    int rollNo;
    String name;
    String sec;

    Seat(int roomNo, int rollNo, String name,String sec) {
        this.roomNo = roomNo;
        this.rollNo = rollNo;
        this.name = name;
        this.sec=sec;
    }
}

class SeatingManager {
    ArrayList<Seat> seating = new ArrayList<>();

    void allocateSeats(ArrayList<Student> students, ArrayList<Room> rooms) {

        if (students.isEmpty() || rooms.isEmpty()) {
            System.out.println("No students or rooms available!");
            return;
        }

        seating.clear(); // important

        // Sort using getter
        students.sort((a, b) -> a.getBranch().compareTo(b.getBranch()));

        int index = 0;

        for (Room r : rooms) {
            if (r.isBlocked) continue;

            for (int i = 0; i < r.capacity && index < students.size(); i++) {
                Student s = students.get(index);

                seating.add(new Seat(
                        r.roomNo,
                        s.getRollNo(),
                        s.getName(),
                        s.getSec()
                ));

                index++;
            }
        }

        System.out.println("Seating allocated!");
    }
    void displaySeating() {
        if (seating.isEmpty()) {
            System.out.println("No seating allocated!");
            return;
        }

        for (Seat s : seating) {
            System.out.println("Room " + s.roomNo +
                    " -> Roll " + s.rollNo +
                    " (" + s.name + ")");
        }
    }
}

class InvigilatorManager {
    ArrayList<String> teachers = new ArrayList<>();

    void addTeacher(String name) {
        teachers.add(name);
    }
    HashMap<Integer, String> invigilatorMap = new HashMap<>();

    void allocateInvigilators(ArrayList<Room> rooms) {

        if (teachers.isEmpty()) {
            System.out.println("No teachers available!");
            return;
        }

        int i = 0;

        for (Room r : rooms) {
            invigilatorMap.put(r.roomNo,
                    teachers.get(i % teachers.size()));
            i++;
        }

        System.out.println("Invigilators assigned!");
    }

    void displayInvigilators() {
        for (int room : invigilatorMap.keySet()) {
            System.out.println("Room " + room +
                    " -> " + invigilatorMap.get(room));
        }
    }
}

class AttendanceManager {
    HashMap<Integer, Boolean> attendance = new HashMap<>();

    void markAttendance(int roll, boolean present) {
        attendance.put(roll, present);
    }

    void showAttendance() {
        for (int roll : attendance.keySet()) {
            System.out.println("Roll " + roll +
                    " -> " + (attendance.get(roll) ? "Present" : "Absent"));
        }
    }
}

public class MainApp {
    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);

        StudentDAO dao = new StudentDAO();
        RoomManager rm = new RoomManager();
        SeatingManager seatM = new SeatingManager();
        InvigilatorManager im = new InvigilatorManager();

        int choice;

        do {
            System.out.println("\n1.Add Student\n2.Show Students\n3.Add Room\n4.Show Rooms");
            System.out.println("5.Allocate Seats\n6.Show Seating\n7.Assign Invigilators");
            System.out.println("8.Show Invigilators\n9. Enter Teacher's name\n0.Exit");

            choice = sc.nextInt();

            switch (choice) {

                case 1:
                    System.out.print("Enter Roll No: ");
                    int roll = sc.nextInt();

                    System.out.print("Enter Name: ");
                    sc.nextLine(); // clear buffer
                    String name = sc.nextLine();

                    System.out.print("Enter Branch: ");
                    String branch = sc.next();

                    System.out.print("Enter Semester: ");
                    int sem = sc.nextInt();

                    System.out.println("Enter Sec: ");
                    String sec=sc.next();

                    dao.addStudent(new Student(roll, name, branch, sem, sec));
                    break;

                case 2:
                    ArrayList<Student> studentsList = dao.getStudents();

                    for (Student s : studentsList) {
                        System.out.println(
                                s.getRollNo() + " " +
                                        s.getName() + " " +
                                        s.getBranch() + " Sem:" + s.getSem()+ " " +
                                        s.getSec() + " "
                        );
                    }
                    break;

                case 3:
                    System.out.print("Enter Room Number: ");
                    int roomNo = sc.nextInt();

                    System.out.print("Enter Capacity: ");
                    int capacity = sc.nextInt();

                    rm.addRoom(roomNo, capacity);
                    break;

                case 4:
                    rm.displayRooms();
                    break;

                case 5:
                    ArrayList<Student> studentsForSeat = dao.getStudents();
                    seatM.allocateSeats(studentsForSeat, rm.rooms);
                    break;

                case 6:
                    seatM.displaySeating();
                    break;

                case 7:
                    if (im.teachers.isEmpty()) {
                        System.out.println("No teachers available!");
                        break;
                    }
                    im.allocateInvigilators(rm.rooms);
                    break;

                case 8:
                    im.displayInvigilators();
                    break;

                case 9:
                    System.out.print("Enter Teacher Name: ");
                    String teacher = sc.next();
                    im.addTeacher(teacher);
                    break;
            }

        } while (choice != 0);

        sc.close();
    }
}
