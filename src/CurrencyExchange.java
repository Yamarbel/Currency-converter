import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.DecimalFormat;

public class CurrencyExchange extends JFrame implements Runnable{
    private JPanel mainWindow;
    private JButton convertButton;
    private JList FromList;
    private JList ToList;
    private JTextField toChange;
    private JLabel result;
    private JLabel lUpdate;
    private JTable ratesTable;
    private Double [] rates;
    private String [] sym;
    private Document localDoc;
    private String date;
    private String webDate;
    private boolean firstTime;

    public CurrencyExchange(){
        add(mainWindow);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("Currency Exchange - Sahar Gezer & Yam Arbel");
        setSize(500,500);

        rates = new Double[15];
        sym = new String[15];
        date = "";
        webDate = "";
        firstTime = true;

        downloadData();
        invokeData();
        firstTime = false;

        convertButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                double fromRate = 0;
                double toRate = 0;
                Double total;

                // Error message if currencies are not selected
                if(FromList.isSelectionEmpty() || ToList.isSelectionEmpty()){
                    JDialog errMess = new JDialog();
                    JLabel er = new JLabel("Do not select Currency");
                    errMess.add(er);
                    errMess.setSize(100,100);
                    errMess.setVisible(true);
                }
                else{
                    for(int i = 0; i<15;i++){
                        if(FromList.getSelectedValue() == sym[i])
                            fromRate = rates[i];
                    }
                    for(int i = 0; i<15;i++){
                        if(ToList.getSelectedValue() == sym[i])
                            toRate = rates[i];
                    }
                    System.out.println(fromRate);
                    System.out.println(toRate);
                }

                //Error message if the amount is empty
                if(toChange.getText().isEmpty() || !toChange.getText().chars().allMatch( Character::isDigit )){
                    JDialog errMess = new JDialog();
                    JLabel er = new JLabel("Do not insert amount to change");
                    errMess.add(er);
                    errMess.setSize(100,100);
                    errMess.setVisible(true);
                }
                else {
                    total = fromRate / toRate * Double.parseDouble(toChange.getText());
                    result.setText(toChange.getText() + " " + FromList.getSelectedValue() + " = " + new DecimalFormat("##.##").format(total) + " " + ToList.getSelectedValue());
                }
            }
        });

    }

    private void invokeData(){
        DefaultListModel SymbolList = new DefaultListModel();

        //Insert NIS to the arrays
        rates[0] = 1.0;
        sym[0] = "NIS";
        SymbolList.addElement("NIS");


        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        //Parsing the xml file in order to update the arrays of rates and symbols with currencies data
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            localDoc = builder.parse("currency.xml");
            NodeList curList = localDoc.getElementsByTagName("CURRENCY");
            Element curr;

            //Get the LAST_UPDATE date from xml file and display it in Gui
            NodeList upDate = localDoc.getElementsByTagName("LAST_UPDATE");
            Node dNode = upDate.item(0);
            curr = (Element) dNode;
            date = curr.getTextContent();
            lUpdate.setText("LAST UPDATE: "+date);

            for (int i = 1; i<curList.getLength()+1;i++){
                Node c = curList.item(i-1);
                if (c.getNodeType() == Node.ELEMENT_NODE){
                    curr = (Element) c;
                    String id =  curr.getAttribute("NAME");
                    NodeList nameList = curr.getChildNodes();
                    for(int j=0;j<nameList.getLength();j++){
                        Node n = nameList.item(j);
                        if(n.getNodeType() == Node.ELEMENT_NODE){
                            Element name = (Element) n;
                            if(name.getTagName() == "RATE") {
                                rates[i] = Double.parseDouble(name.getTextContent());
                            }

                            if (name.getTagName() == "CURRENCYCODE") {
                                SymbolList.addElement(name.getTextContent());
                                sym[i] = name.getTextContent();
                            }
                        }
                    }
                }
            }
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


        ToList.setModel(SymbolList);
        FromList.setModel(SymbolList);
        addToTable();
    }

    private void addToTable(){
        //Insert currencies data to JTable
        DefaultTableModel dtm = new DefaultTableModel(0, 0);
        String header[] = new String[] { "URRENCY CODE", "RATE"};
        dtm.setColumnIdentifiers(header);
        ratesTable.setModel(dtm);
        for (int i = 1; i < 15; i++) {
            dtm.addRow(new Object[]{sym[i], rates[i]});
        }

    }

    private void downloadData(){
        File f = new File("currency.xml");
        if (!f.exists() || !(date.equals(webDate))) {
            InputStream in = null;
            try {
                in = new URL("https://www.boi.org.il/currency.xml").openStream();
                Files.copy(in, Paths.get("currency.xml"), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void run() {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = null;
        Document webDoc = null;
        Element curr;

        while(true) {
            try {
                builder = factory.newDocumentBuilder();
            } catch (ParserConfigurationException e) {
                e.printStackTrace();
            }

            try {
                webDoc = builder.parse("https://www.boi.org.il/currency.xml");
            } catch (SAXException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }


            //Get the LAST_UPDATE date from xml file and display it in Gui
            NodeList upDate = webDoc.getElementsByTagName("LAST_UPDATE");
            Node dNode = upDate.item(0);
            curr = (Element) dNode;
            webDate = curr.getTextContent();

            //Checks id the xml file exist or not up to date and download it from url
            downloadData();

            try {
                //Theard sleep for 1 min and run again every 1 min
                Thread.sleep(100000);
                invokeData();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}


