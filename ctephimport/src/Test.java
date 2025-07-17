
import org.apache.log4j.chainsaw.Main;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author chaitu
 */
public class Test {
    public static void verify() {
         String name="post_bdhjbjs";
    
    String comparestring="cif_reort";
    String [] comparelist=null;
    if(comparestring.indexOf(",")!=-1){
    comparelist=comparestring.split(",");
    }
    else{
        comparelist=new String[1];
        comparelist[0]=comparestring;
    }
    boolean modifystatus=false;
        for (int i = 0; i < comparelist.length; i++) {
            System.out.println(comparelist[i]);
            if(comparelist[i]!=null&& name.startsWith(comparelist[i])){
                modifystatus=true;
                break;
            }
            
        }
    
    }
    
    public static void  main(String args[]){
        Test t=new Test();
        t.verify();
    }
    
   
    
}
