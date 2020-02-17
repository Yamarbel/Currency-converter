import javax.swing.*;
import java.util.logging.*;

public class Main {
    public static void main(String [] args){
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(new Runnable(){
            @Override
            public void run(){
                CurrencyExchange myCE = new CurrencyExchange();
                myCE.setVisible(true);

                Thread t1 = new Thread(myCE);
                t1.start();
            }
        } );

    }

}


