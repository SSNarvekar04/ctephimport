
import java.io.File;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author venky
 * @version Expression Revision is undefined on line 13, column 15 in
 * Templates/Classes/Class.java.
 */
public class RadVistaDicomExport {

    final public static String query = "select * from radexport where subjectid in('100100009','100100027','100100028','100100039','100100041','100100046','100100203','100100205','100100206','100100211','100100217','100100221','100100226','100100228','100100235','100100239','100100241','100100243','100100244','100100245','100100246','100100249','100100250','100100266','100100267','100100271','100100282','100100288','100100292','100100302','100100314','100100321','100100323','100100325','100100336','100100347','100100350','100100352','100100355','100100359','100100360','100100363','100100364','100100371','100100376','100100383','100100412','100100415','100100418','100100419','100100422','100100430','100100432','100100435','100100436','100100440','100100461','100100475','100100478','100100479','100100488','100100513','100100520','100100549','100100552','100100562','100100573','100100581','100100583','100100592','100100643','100100644','100100645','100100648','100100654','100100658','100100659','100100665','100100687','100100693','100100701','100100702','100100760','100100762','100100763','100100773','100100776','100100784','100100785','100100817','100100819','100100823','100100824','100100825')";
    static Connection connection = null;

    public static String basedir="/home/venky/Desktop/Qc2ReadCompleted-aux";
    public static void main(String[] argv) throws SQLException {

        System.out.println("-------- PostgreSQL "
                + "JDBC Connection Testing ------------");

        try {

            Class.forName("org.postgresql.Driver");

        } catch (ClassNotFoundException e) {

            System.out.println("Where is your PostgreSQL JDBC Driver? "
                    + "Include in your library path!");
            e.printStackTrace();
            return;

        }

        System.out.println("PostgreSQL JDBC Driver Registered!");

        try {

            connection = DriverManager.getConnection(
                    "jdbc:postgresql://localhost:5434/cteph_aws_v1", "postgres",
                    "123456");

        } catch (SQLException e) {

            System.out.println("Connection Failed! Check output console");
            e.printStackTrace();
            return;

        }

        if (connection != null) {
          Statement stmt=connection.createStatement();   
            // Executing SQL query  
            ResultSet rs=stmt.executeQuery(query);    
            while(rs.next()){    
                String subject=rs.getString(1);
                String study=rs.getString(2);
                String series=rs.getString(3);
                String filename=rs.getString(4);
                String relativepath=subject+File.separator+study+File.separator+series;
                File fl=new File(basedir,relativepath);
               // System.out.println(relativepath);
                
               String command= Stream.of(fl.listFiles()).filter(x->{
                    
                    if((x.getName().endsWith("vti")&&!x.getName().endsWith("_seed_mask.vti") && x.getName().indexOf(".")>2)||x.getName().endsWith("json"))
                    {
                       return true;
                    }
                    return false;
                }).map(f->{
                    return f.getName().endsWith(".json")?"-json "+f.getAbsolutePath():"-m "+f.getAbsolutePath();                    
                    
                }).collect(Collectors.joining(" "));
                
               System.out.println("ExportDicom -f "+fl.getAbsolutePath()+File.separator+filename+" -loc /tmp/output"+command);
                
                
                
                
                
                
            }  
            
            
        } else {
            System.out.println("Failed to make connection!");
        }
    }

}
